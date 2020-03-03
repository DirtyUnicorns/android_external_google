package com.google.android.systemui.dreamliner;

import android.os.IBinder;
import android.os.SystemClock;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.content.ServiceConnection;
import android.os.UserHandle;
import com.google.android.systemui.elmyra.gates.KeyguardVisibility;
import android.content.ComponentName;
import com.android.keyguard.KeyguardUpdateMonitor;
import android.view.View;
import java.util.concurrent.Executors;
import android.service.dreams.DreamService;
import android.os.RemoteException;
import android.service.dreams.IDreamManager.Stub;
import android.os.ServiceManager;
import android.service.dreams.IDreamManager;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import java.util.ArrayList;
import android.os.Looper;
import android.content.Context;
import com.android.systemui.settings.CurrentUserTracker;
import android.os.Handler;
import android.widget.ImageView;
import java.util.List;
import java.util.concurrent.ExecutorService;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.dock.DockManager;
import android.content.BroadcastReceiver;

public class DockObserver extends BroadcastReceiver implements DockManager {
    private static final String TAG = "Dreamliner-DockObserver";

    @VisibleForTesting
    static final String ACTION_CHALLENGE = "com.google.android.systemui.dreamliner.ACTION_CHALLENGE";
    @VisibleForTesting
    static final String ACTION_DOCK_UI_ACTIVE = "com.google.android.systemui.dreamliner.ACTION_DOCK_UI_ACTIVE";
    @VisibleForTesting
    static final String ACTION_DOCK_UI_IDLE = "com.google.android.systemui.dreamliner.ACTION_DOCK_UI_IDLE";
    @VisibleForTesting
    static final String ACTION_GET_DOCK_INFO = "com.google.android.systemui.dreamliner.ACTION_GET_DOCK_INFO";
    @VisibleForTesting
    static final String ACTION_KEY_EXCHANGE = "com.google.android.systemui.dreamliner.ACTION_KEY_EXCHANGE";
    @VisibleForTesting
    static final String ACTION_REBIND_DOCK_SERVICE = "com.google.android.systemui.dreamliner.ACTION_REBIND_DOCK_SERVICE";
    @VisibleForTesting
    static final String ACTION_START_DREAMLINER_CONTROL_SERVICE = "com.google.android.apps.dreamliner.START";
    @VisibleForTesting
    static final String COMPONENTNAME_DREAMLINER_CONTROL_SERVICE = "com.google.android.apps.dreamliner/.DreamlinerControlService";
    @VisibleForTesting
    static final String EXTRA_CHALLENGE_DATA = "challenge_data";
    @VisibleForTesting
    static final String EXTRA_CHALLENGE_DOCK_ID = "challenge_dock_id";
    @VisibleForTesting
    static final String EXTRA_PUBLIC_KEY = "public_key";
    @VisibleForTesting
    static final String KEY_SHOWING = "showing";
    @VisibleForTesting
    static final int RESULT_NOT_FOUND = 1;
    @VisibleForTesting
    static final int RESULT_OK = 0;
    @VisibleForTesting
    static volatile ExecutorService mSingleThreadExecutor;
    private static boolean sIsDockingUiShowing = false;
    private final List<DockEventListener> mClients;
    @VisibleForTesting
    int mDockState;
    private ImageView mDreamlinerGear;
    @VisibleForTesting
    final DreamlinerBroadcastReceiver mDreamlinerReceiver;
    @VisibleForTesting
    DreamlinerServiceConn mDreamlinerServiceConn;
    private final Handler mHandler;
    private DockIndicationController mIndicationController;
    @VisibleForTesting
    SettingsGearController mSettingsGearController;
    private final CurrentUserTracker mUserTracker;
    private final WirelessCharger mWirelessCharger;
    AlignmentStateListener mAlignmentListener;

    public DockObserver(Context context, WirelessCharger wirelessCharger) {
        mDreamlinerReceiver = new DreamlinerBroadcastReceiver();
        mDockState = 0;
        mHandler = new Handler(Looper.getMainLooper());
        mClients = new ArrayList<DockEventListener>();
        mUserTracker = new CurrentUserTracker(context) {
            @Override
            public void onUserSwitched(int newUserId) {
                stopDreamlinerService(context);
                updateCurrentDockingStatus(context);
            }
        };
        mWirelessCharger = wirelessCharger;
        context.registerReceiver(this, getPowerConnectedIntentFilter());
    }

    private boolean assertNotNull(Object o, String str) {
        if (o == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" is null");
            Log.w(TAG, sb.toString());
            return false;
        }
        return true;
    }

    private byte[] convertArrayListToPrimitiveArray(ArrayList<Byte> list) {
        if (list != null && !list.isEmpty()) {
            final byte[] array = new byte[list.size()];
            for (int i = 0; i < array.length; ++i) {
                array[i] = list.get(i);
            }
            return array;
        }
        return null;
    }

    private Bundle createChallengeResponseBundle(ArrayList<Byte> list) {
        if (list != null && !list.isEmpty()) {
            final byte[] convertArrayListToPrimitiveArray = convertArrayListToPrimitiveArray(list);
            final Bundle bundle = new Bundle();
            bundle.putByteArray("challenge_response", convertArrayListToPrimitiveArray);
            return bundle;
        }
        return null;
    }

    private Bundle createKeyExchangeResponseBundle(byte b, ArrayList<Byte> list) {
        if (list != null && !list.isEmpty()) {
            final byte[] convertArrayListToPrimitiveArray = convertArrayListToPrimitiveArray(list);
            final Bundle bundle = new Bundle();
            bundle.putByte("dock_id", b);
            bundle.putByteArray("dock_public_key", convertArrayListToPrimitiveArray);
            return bundle;
        }
        return null;
    }

    private void dispatchDockEvent(DockEventListener dockEventListener) {
        dockEventListener.onEvent(mDockState);
    }

    private final Intent getBatteryStatus(Context context) {
        return context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private IDreamManager getDreamManager() {
        return IDreamManager.Stub.asInterface(
                ServiceManager.checkService(DreamService.DREAM_SERVICE));
    }

    private IntentFilter getPowerConnectedIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction(ACTION_REBIND_DOCK_SERVICE);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        return intentFilter;
    }

    private boolean isChargingOrFull(Intent intent) {
        final int intExtra = intent.getIntExtra("status", -1);
        return intExtra == 2 || intExtra == 5;
    }

    public static boolean isDockingUiShowing() {
        return sIsDockingUiShowing;
    }

    private void notifyForceEnabledAmbientDisplay(boolean b) {
        IDreamManager dreamManagerInstance = getDreamManager();
        if (dreamManagerInstance != null) {
            try {
                dreamManagerInstance.forceAmbientDisplayEnabled(b);
                return;
            }
            catch (RemoteException ex) {
                Log.e("Dreamliner-DockObserver", ex.toString());
            }
        } else {
            Log.e(TAG, "DreamManager not found");
        }
    }

    private static void runOnBackgroundThread(Runnable runnable) {
        if (mSingleThreadExecutor == null) {
            mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        }
        mSingleThreadExecutor.execute(runnable);
    }

    private void sendDockActiveIntent(Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_DOCK_ACTIVE).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    private void sendDockIdleIntent(Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_DOCK_IDLE).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    @Override
    public void addAlignmentStateListener(AlignmentStateListener listener) {
        mAlignmentListener = listener;
    }

    @Override
    public void removeAlignmentStateListener(AlignmentStateListener listener) {
        mAlignmentListener = listener;
    }

    private void startDreamlinerService(Context context, int n, int n2, int n3) {
        synchronized (this) {
            notifyForceEnabledAmbientDisplay(true);
            if (mDreamlinerServiceConn == null) {
                mDreamlinerReceiver.registerReceiver(context);
                if (mDreamlinerGear != null) {
                    Log.d(TAG, "SettingsGearController setup.");
                    mSettingsGearController = new SettingsGearController(context, mDreamlinerGear,
                        (View)mDreamlinerGear.getParent(), KeyguardUpdateMonitor.getInstance(context));
                }
                final Intent obj = new Intent(ACTION_START_DREAMLINER_CONTROL_SERVICE);
                obj.setComponent(ComponentName.unflattenFromString(COMPONENTNAME_DREAMLINER_CONTROL_SERVICE));
                obj.putExtra("type", n);
                obj.putExtra("orientation", n2);
                obj.putExtra("id", n3);
                obj.putExtra("occluded", new KeyguardVisibility(context).isKeyguardOccluded());
                try {
                    mDreamlinerServiceConn = new DreamlinerServiceConn(context);
                    if (context.bindServiceAsUser(obj, mDreamlinerServiceConn, 1,
                            new UserHandle(mUserTracker.getCurrentUserId()))) {
                        mUserTracker.startTracking();
                        return;
                    }
                }
                catch (SecurityException ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                }
                mDreamlinerServiceConn = null;
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to bind Dreamliner service: ");
                sb.append(obj);
                Log.w(TAG, sb.toString());
            }
        }
    }

    private void stopDreamlinerService(final Context context) {
        notifyForceEnabledAmbientDisplay(false);
        onDockStateChanged(0);
        try {
            if (mDreamlinerServiceConn != null) {
                if (assertNotNull(mSettingsGearController, SettingsGearController.class.getSimpleName())) {
                    mSettingsGearController.stopMonitoring();
                    mSettingsGearController = null;
                }
                mUserTracker.stopTracking();
                mDreamlinerReceiver.unregisterReceiver(context);
                context.unbindService(mDreamlinerServiceConn);
                mDreamlinerServiceConn = null;
            }
        }
        catch (IllegalArgumentException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    private void triggerChallengeWithDock(Intent intent) {
        if (intent == null) {
            return;
        }
        ResultReceiver resultReceiver = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);
        if (resultReceiver != null) {
            final byte byteExtra = intent.getByteExtra(EXTRA_CHALLENGE_DOCK_ID, (byte)(-1));
            final byte[] byteArrayExtra = intent.getByteArrayExtra(EXTRA_CHALLENGE_DATA);
            if (byteArrayExtra != null && byteArrayExtra.length > 0 && byteExtra >= 0) {
                runOnBackgroundThread(new ChallengeWithDock(resultReceiver, byteExtra, byteArrayExtra));
            }
            else {
                resultReceiver.send(1, null);
            }
        }
    }

    private void triggerKeyExchangeWithDock(Intent intent) {
        if (intent == null) {
            return;
        }
        ResultReceiver resultReceiver = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);
        if (resultReceiver != null) {
            final byte[] byteArrayExtra = intent.getByteArrayExtra(EXTRA_PUBLIC_KEY);
            if (byteArrayExtra != null && byteArrayExtra.length > 0) {
                runOnBackgroundThread(new KeyExchangeWithDock(resultReceiver, byteArrayExtra));
            }
            else {
                resultReceiver.send(1, (Bundle)null);
            }
        }
    }

    private void tryTurnScreenOff(Context context) {
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        if (powerManager.isScreenOn()) {
            powerManager.goToSleep(SystemClock.uptimeMillis());
        }
    }

    public void addListener(DockEventListener dockEventListener) {
        if (!mClients.contains(dockEventListener)) {
            mClients.add(dockEventListener);
        }
        mHandler.post(() -> dispatchDockEvent(dockEventListener));
    }

    public boolean isDocked() {
        return mDockState == 1 || mDockState == 2;
    }

    @VisibleForTesting
    void onDockStateChanged(int i) {
        if (mDockState == i) {
            return;
        }
        mDockState = i;
        mClients.forEach(e -> dispatchDockEvent(e));
        if (mIndicationController != null) {
            mIndicationController.setDocking(isDocked());
        }
    }

    public void onReceive(final Context context, final Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        int n = -1;
        switch (action.hashCode()) {
            case 1318602046: {
                if (action.equals(ACTION_REBIND_DOCK_SERVICE)) {
                    n = 2;
                    break;
                }
                break;
            }
            case 1019184907: {
                if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                    n = 0;
                    break;
                }
                break;
            }
            case 798292259: {
                if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                    n = 3;
                    break;
                }
                break;
            }
            case -1886648615: {
                if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    n = 1;
                    break;
                }
                break;
            }
        }
        if (n != 0) {
            if (n != 1) {
                if (n == 2 || n == 3) {
                    updateCurrentDockingStatus(context);
                }
            }
            else {
                stopDreamlinerService(context);
                sIsDockingUiShowing = false;
            }
        }
        else if (mWirelessCharger != null) {
            runOnBackgroundThread(new IsDockPresent(context));
        }
    }

    public void removeListener(DockEventListener dockEventListener) {
        mClients.remove(dockEventListener);
    }

    public void setDreamlinerGear(ImageView dreamlinerGear) {
        mDreamlinerGear = dreamlinerGear;
    }

    public void setIndicationController(DockIndicationController indicationController) {
        mIndicationController = indicationController;
    }

    @VisibleForTesting
    final void updateCurrentDockingStatus(Context context) {
        notifyForceEnabledAmbientDisplay(false);
        if (isChargingOrFull(getBatteryStatus(context)) && mWirelessCharger != null) {
            runOnBackgroundThread(new IsDockPresent(context));
        }
    }

    @VisibleForTesting
    final class ChallengeCallback implements WirelessCharger.ChallengeCallback
    {
        private final ResultReceiver mResultReceiver;

        ChallengeCallback(ResultReceiver resultReceiver) {
            mResultReceiver = resultReceiver;
        }

        @Override
        public void onCallback(int n, ArrayList<Byte> list) {
            if (n == 0) {
                mResultReceiver.send(0, createChallengeResponseBundle(list));
            }
            else {
                mResultReceiver.send(1, null);
            }
        }
    }

    private class ChallengeWithDock implements Runnable
    {
        final byte[] mChallengeData;
        final byte mDockId;
        final ResultReceiver mResultReceiver;

        public ChallengeWithDock(ResultReceiver resultReceiver, byte dockId, byte[] challengeData) {
            mDockId = dockId;
            mChallengeData = challengeData;
            mResultReceiver = resultReceiver;
        }

        @Override
        public void run() {
            if (mWirelessCharger == null) {
                return;
            }
            mWirelessCharger.challenge(mDockId, mChallengeData, new ChallengeCallback(mResultReceiver));
        }
    }

    @VisibleForTesting
    class DreamlinerBroadcastReceiver extends BroadcastReceiver
    {
        private boolean mListening;

        private IntentFilter getIntentFilter() {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_GET_DOCK_INFO);
            intentFilter.addAction(ACTION_DOCK_UI_IDLE);
            intentFilter.addAction(ACTION_DOCK_UI_ACTIVE );
            intentFilter.addAction(ACTION_KEY_EXCHANGE);
            intentFilter.addAction(ACTION_CHALLENGE);
            intentFilter.addAction("com.google.android.systemui.dreamliner.dream");
            intentFilter.addAction("com.google.android.systemui.dreamliner.paired");
            intentFilter.addAction("com.google.android.systemui.dreamliner.pause");
            intentFilter.addAction("com.google.android.systemui.dreamliner.resume");
            intentFilter.addAction("com.google.android.systemui.dreamliner.undock");
            intentFilter.addAction("com.google.android.systemui.dreamliner.home_control");
            intentFilter.addAction("com.google.android.systemui.dreamliner.assistant_poodle");
            return intentFilter;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            final String action = intent.getAction();
            switch (action) {
                case "com.google.android.systemui.dreamliner.assistant_poodle": {
                    if (mIndicationController != null) {
                        mIndicationController.setShowing(1, intent.getBooleanExtra("showing", false));
                        break;
                    }
                    break;
                }
                case "com.google.android.systemui.dreamliner.home_control": {
                    if (mIndicationController != null) {
                        mIndicationController.setShowing(0, intent.getBooleanExtra("showing", false));
                        break;
                    }
                    break;
                }
                case "com.google.android.systemui.dreamliner.undock": {
                    onDockStateChanged(0);
                    if (assertNotNull(mSettingsGearController, SettingsGearController.class.getSimpleName())) {
                        mSettingsGearController.stopMonitoring();
                        break;
                    }
                    break;
                }
                case "com.google.android.systemui.dreamliner.pause": {
                    onDockStateChanged(2);
                    if (assertNotNull(mSettingsGearController, SettingsGearController.class.getSimpleName())) {
                        mSettingsGearController.stopMonitoring();
                        break;
                    }
                    break;
                }
                case "com.google.android.systemui.dreamliner.paired":
                case "com.google.android.systemui.dreamliner.resume": {
                    onDockStateChanged(1);
                    if (assertNotNull(mSettingsGearController, SettingsGearController.class.getSimpleName())) {
                        mSettingsGearController.startMonitoring();
                        break;
                    }
                    break;
                }
                case "com.google.android.systemui.dreamliner.dream": {
                    tryTurnScreenOff(context);
                    break;
                }
                case ACTION_CHALLENGE: {
                    triggerChallengeWithDock(intent);
                    break;
                }
                case ACTION_KEY_EXCHANGE: {
                    triggerKeyExchangeWithDock(intent);
                    break;
                }
                case ACTION_DOCK_UI_ACTIVE: {
                    sendDockActiveIntent(context);
                    sIsDockingUiShowing = false;
                    break;
                }
                case ACTION_DOCK_UI_IDLE: {
                    sendDockIdleIntent(context);
                    sIsDockingUiShowing = true;
                    break;
                }
                case ACTION_GET_DOCK_INFO: {
                    ResultReceiver resultReceiver = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);
                    if (resultReceiver != null) {
                        runOnBackgroundThread(new GetDockInfo(resultReceiver, context));
                        break;
                    }
                    break;
                }
            }
        }

        public void registerReceiver(Context context) {
            if (!mListening) {
                context.registerReceiverAsUser(this, UserHandle.ALL, getIntentFilter(),
                    "com.google.android.systemui.permission.WIRELESS_CHARGER_STATUS", null);
                mListening = true;
            }
        }

        public void unregisterReceiver(Context context) {
            if (mListening) {
                context.unregisterReceiver(this);
                mListening = false;
            }
        }
    }

    @VisibleForTesting
    final class DreamlinerServiceConn implements ServiceConnection
    {
        private Context mContext;

        public DreamlinerServiceConn(Context context) {
            mContext = context;
        }

        public void onBindingDied(ComponentName componentName) {
            stopDreamlinerService(mContext);
            sIsDockingUiShowing = false;
        }

        public void onServiceConnected(ComponentName componentName, IBinder binder) {
        }

        public void onServiceDisconnected(ComponentName componentName) {
            sendDockActiveIntent(mContext);
        }
    }

    private class GetDockInfo implements Runnable
    {
        private Context mContext;
        private ResultReceiver mResultReceiver;

        public GetDockInfo(ResultReceiver resultReceiver, Context context) {
            mResultReceiver = resultReceiver;
            mContext = context;
        }

        @Override
        public void run() {
            if (mWirelessCharger == null) {
                return;
            }
            mWirelessCharger.getInformation(new GetInformationCallback(mResultReceiver));
        }
    }

    @VisibleForTesting
    final class GetInformationCallback implements WirelessCharger.GetInformationCallback
    {
        private ResultReceiver mResultReceiver;

        GetInformationCallback(ResultReceiver resultReceiver) {
            mResultReceiver = resultReceiver;
        }

        @Override
        public void onCallback(int n, DockInfo dockInfo) {
            if (n == 0) {
                mResultReceiver.send(0, dockInfo.toBundle());
            } else if (n != 1) {
                mResultReceiver.send(1, null);
            }
        }
    }

    private class IsDockPresent implements Runnable
    {
        private Context mContext;

        public IsDockPresent(Context context) {
            mContext = context;
        }

        @Override
        public void run() {
            if (mWirelessCharger == null) {
                return;
            }
            mWirelessCharger.asyncIsDockPresent(new IsDockPresentCallback(mContext));
        }
    }

    @VisibleForTesting
    final class IsDockPresentCallback implements WirelessCharger.IsDockPresentCallback
    {
        private Context mContext;

        IsDockPresentCallback(Context context) {
            mContext = context;
        }

        @Override
        public void onCallback(boolean b, byte b2, byte b3, boolean b4, int n) {
            if (b) {
                startDreamlinerService(mContext, b2, b3, n);
            }
        }
    }

    @VisibleForTesting
    final class KeyExchangeCallback implements WirelessCharger.KeyExchangeCallback
    {
        private ResultReceiver mResultReceiver;

        KeyExchangeCallback(ResultReceiver resultReceiver) {
            mResultReceiver = resultReceiver;
        }

        @Override
        public void onCallback(int n, byte b, ArrayList<Byte> list) {
            if (n == 0) {
                mResultReceiver.send(0, createKeyExchangeResponseBundle(b, list));
            }
            else {
                mResultReceiver.send(1, null);
            }
        }
    }

    private class KeyExchangeWithDock implements Runnable
    {
        private byte[] mPublicKey;
        private ResultReceiver mResultReceiver;

        public KeyExchangeWithDock(ResultReceiver resultReceiver, byte[] publicKey) {
            mPublicKey = publicKey;
            mResultReceiver = resultReceiver;
        }

        @Override
        public void run() {
            if (mWirelessCharger == null) {
                return;
            }
            mWirelessCharger.keyExchange(mPublicKey, new KeyExchangeCallback(mResultReceiver));
        }
    }
}

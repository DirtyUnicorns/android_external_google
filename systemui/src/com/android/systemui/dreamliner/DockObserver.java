package com.google.android.systemui.dreamliner;

import android.os.IBinder;
import android.util.Log;
import android.content.ServiceConnection;
import android.os.UserHandle;
import com.google.android.systemui.elmyra.gates.KeyguardVisibility;
import android.content.ComponentName;
import android.os.Handler;
import java.util.concurrent.Executors;
import android.content.IntentFilter;
import android.os.Bundle;
import java.util.ArrayList;
import android.os.ResultReceiver;
import android.content.Intent;
import android.content.Context;
import com.android.systemui.settings.CurrentUserTracker;
import java.util.concurrent.ExecutorService;
import com.android.internal.annotations.VisibleForTesting;
import android.content.BroadcastReceiver;

public class DockObserver extends BroadcastReceiver {
    @VisibleForTesting
    static final String ACTION_CHALLENGE =
            "com.google.android.systemui.dreamliner.ACTION_CHALLENGE";
    @VisibleForTesting
    static final String ACTION_DOCK_UI_ACTIVE =
            "com.google.android.systemui.dreamliner.ACTION_DOCK_UI_ACTIVE";
    @VisibleForTesting
    static final String ACTION_DOCK_UI_IDLE =
            "com.google.android.systemui.dreamliner.ACTION_DOCK_UI_IDLE";
    @VisibleForTesting
    static final String ACTION_GET_DOCK_INFO =
            "com.google.android.systemui.dreamliner.ACTION_GET_DOCK_INFO";
    @VisibleForTesting
    static final String ACTION_KEY_EXCHANGE =
            "com.google.android.systemui.dreamliner.ACTION_KEY_EXCHANGE";
    @VisibleForTesting
    static final String ACTION_START_DREAMLINER_CONTROL_SERVICE =
            "com.google.android.apps.dreamliner.START";
    @VisibleForTesting
    static final String COMPONENTNAME_DREAMLINER_CONTROL_SERVICE =
            "com.google.android.apps.dreamliner/.DreamlinerControlService";
    @VisibleForTesting
    static final String EXTRA_CHALLENGE_DATA = "challenge_data";
    @VisibleForTesting
    static final String EXTRA_CHALLENGE_DOCK_ID = "challenge_dock_id";
    @VisibleForTesting
    static final String EXTRA_PUBLIC_KEY = "public_key";
    @VisibleForTesting
    static final int RESULT_NOT_FOUND = 1;
    @VisibleForTesting
    static final int RESULT_OK = 0;
    @VisibleForTesting
    static volatile ExecutorService mSingleThreadExecutor;
    private static boolean sIsDockingUiShowing = false;
    @VisibleForTesting
    final BroadcastReceiver dreamlinerHandler;
    @VisibleForTesting
    DreamlinerServiceConn mDreamlinerServiceConn;
    private final CurrentUserTracker mUserTracker;
    @VisibleForTesting
    WirelessCharger mWirelessCharger;

    public DockObserver(final Context context) {
        dreamlinerHandler = new BroadcastReceiver() {
            public void onReceive(final Context context, final Intent intent) {
                if (intent == null) {
                    return;
                }
                final String action = intent.getAction();
                if (action != null) {
                    switch (action) {
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
                            final ResultReceiver resultReceiver = intent.getParcelableExtra(
                                    "android.intent.extra.RESULT_RECEIVER");
                            if (resultReceiver != null) {
                                runOnBackgroundThread(new GetDockInfo(resultReceiver, context));
                                break;
                            }
                            break;
                        }
                    }
                }
            }
        };
        mWirelessCharger = DreamlinerUtils.getInstance(context);
        context.registerReceiver((BroadcastReceiver)this, getPowerConnectedIntentFilter());
        mUserTracker = new CurrentUserTracker(context) {
            @Override
            public void onUserSwitched(final int n) {
                stopDreamlinerService(context);
                updateCurrentDockingStatus(context);
            }
        };
    }

    private byte[] convertArrayListToPrimitiveArray(final ArrayList<Byte> list) {
        if (list != null && !list.isEmpty()) {
            final byte[] array = new byte[list.size()];
            for (int i = 0; i < array.length; ++i) {
                array[i] = list.get(i);
            }
            return array;
        }
        return null;
    }

    private Bundle createChallengeResponseBundle(final ArrayList<Byte> list) {
        if (list != null && !list.isEmpty()) {
            final byte[] convertArrayListToPrimitiveArray =
                    convertArrayListToPrimitiveArray(list);
            final Bundle bundle = new Bundle();
            bundle.putByteArray("challenge_response", convertArrayListToPrimitiveArray);
            return bundle;
        }
        return null;
    }

    private Bundle createKeyExchangeResponseBundle(final byte b, final ArrayList<Byte> list) {
        if (list != null && !list.isEmpty()) {
            final byte[] convertArrayListToPrimitiveArray =
                    convertArrayListToPrimitiveArray(list);
            final Bundle bundle = new Bundle();
            bundle.putByte("dock_id", b);
            bundle.putByteArray("dock_public_key", convertArrayListToPrimitiveArray);
            return bundle;
        }
        return null;
    }

    private final Intent getBatteryStatus(final Context context) {
        return context.registerReceiver(null, new IntentFilter(
                "android.intent.action.BATTERY_CHANGED"));
    }

    private final IntentFilter getDreamlinerIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GET_DOCK_INFO);
        intentFilter.addAction(ACTION_DOCK_UI_IDLE);
        intentFilter.addAction(ACTION_DOCK_UI_ACTIVE);
        intentFilter.addAction(ACTION_KEY_EXCHANGE);
        intentFilter.addAction(ACTION_CHALLENGE);
        return intentFilter;
    }

    private final IntentFilter getPowerConnectedIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.setPriority(1000);
        return intentFilter;
    }

    private boolean isChargingOrFull(final Intent intent) {
        final int intExtra = intent.getIntExtra("status", -1);
        return intExtra == 2 || intExtra == 5;
    }

    public static boolean isDockingUiShowing() {
        return sIsDockingUiShowing;
    }

    private static final void runOnBackgroundThread(final Runnable runnable) {
        if (mSingleThreadExecutor == null) {
            mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        }
        mSingleThreadExecutor.execute(runnable);
    }

    private void sendDockActiveIntent(final Context context) {
        context.sendBroadcast(new Intent("android.intent.action.DOCK_ACTIVE").addFlags(1073741824));
    }

    private void sendDockIdleIntent(final Context context) {
        context.sendBroadcast(new Intent("android.intent.action.DOCK_IDLE").addFlags(1073741824));
    }

    private void startDreamlinerService(final Context context, final int n, final int n2, final int n3) {
        if (mDreamlinerServiceConn == null) {
            context.registerReceiver(dreamlinerHandler, getDreamlinerIntentFilter(),
                    "com.google.android.systemui.permission.WIRELESS_CHARGER_STATUS", (Handler)null);
            final Intent intent = new Intent("com.google.android.apps.dreamliner.START");
            intent.setComponent(ComponentName.unflattenFromString(
                    "com.google.android.apps.dreamliner/.DreamlinerControlService"));
            intent.putExtra("type", n);
            intent.putExtra("orientation", n2);
            intent.putExtra("id", n3);
            intent.putExtra("occluded", new KeyguardVisibility(context).isKeyguardOccluded());
            try {
                mDreamlinerServiceConn = new DreamlinerServiceConn(context);
                if (context.bindServiceAsUser(intent, (ServiceConnection)mDreamlinerServiceConn,
                        1, new UserHandle(mUserTracker.getCurrentUserId()))) {
                    mUserTracker.startTracking();
                    return;
                }
            }
            catch (SecurityException ex) {
                Log.e("Dreamliner-DockObserver", ex.getMessage(), (Throwable)ex);
            }
            mDreamlinerServiceConn = null;
            final StringBuilder sb = new StringBuilder();
            sb.append("Unable to bind Dreamliner service: ");
            sb.append(intent);
            Log.w("Dreamliner-DockObserver", sb.toString());
        }
    }

    private void stopDreamlinerService(final Context context) {
        try {
            if (mDreamlinerServiceConn != null) {
                mUserTracker.stopTracking();
                context.unregisterReceiver(dreamlinerHandler);
                context.unbindService((ServiceConnection)mDreamlinerServiceConn);
                mDreamlinerServiceConn = null;
            }
        }
        catch (IllegalArgumentException ex) {
            Log.e("Dreamliner-DockObserver", ex.getMessage(), (Throwable)ex);
        }
    }

    private void triggerChallengeWithDock(final Intent intent) {
        if (intent == null) {
            return;
        }
        final ResultReceiver resultReceiver = (ResultReceiver)intent.getParcelableExtra(
                "android.intent.extra.RESULT_RECEIVER");
        if (resultReceiver != null) {
            final byte byteExtra = intent.getByteExtra("challenge_dock_id", (byte)(-1));
            final byte[] byteArrayExtra = intent.getByteArrayExtra("challenge_data");
            if (byteArrayExtra != null && byteArrayExtra.length > 0 && byteExtra >= 0) {
                runOnBackgroundThread(new ChallengeWithDock(resultReceiver, byteExtra,
                        byteArrayExtra));
            }
            else {
                resultReceiver.send(1, (Bundle)null);
            }
        }
    }

    private void triggerKeyExchangeWithDock(final Intent intent) {
        if (intent == null) {
            return;
        }
        final ResultReceiver resultReceiver = (ResultReceiver)intent.getParcelableExtra(
                "android.intent.extra.RESULT_RECEIVER");
        if (resultReceiver != null) {
            final byte[] byteArrayExtra = intent.getByteArrayExtra("public_key");
            if (byteArrayExtra != null && byteArrayExtra.length > 0) {
                runOnBackgroundThread(new KeyExchangeWithDock(resultReceiver, byteArrayExtra));
            }
            else {
                resultReceiver.send(1, (Bundle)null);
            }
        }
    }

    public void onReceive(final Context context, final Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        int n = -1;
        final int hashCode;
        if (action != null) {
            hashCode = action.hashCode();
            if (hashCode != -1886648615) {
                if (hashCode != 798292259) {
                    if (hashCode == 1019184907) {
                        if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                            n = 0;
                        }
                    }
                } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    n = 2;
                }
            } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                n = 1;
            }
        }
        switch (n) {
            case 2: {
                updateCurrentDockingStatus(context);
                break;
            }
            case 1: {
                stopDreamlinerService(context);
                sIsDockingUiShowing = false;
                break;
            }
            case 0: {
                if (mWirelessCharger == null) {
                    break;
                }
                mWirelessCharger.asyncIsDockPresent(new IsDockPresentCallback(context));
                break;
            }
        }
    }

    @VisibleForTesting
    public final void updateCurrentDockingStatus(Context context) {
        if (isChargingOrFull(getBatteryStatus(context)) && mWirelessCharger != null) {
            mWirelessCharger.asyncIsDockPresent(new IsDockPresentCallback(context));
        }
    }

    @VisibleForTesting
    final class ChallengeCallback implements WirelessCharger.ChallengeCallback {
        private final ResultReceiver mResultReceiver;

        ChallengeCallback(ResultReceiver resultReceiver) {
            mResultReceiver = resultReceiver;
        }

        public void onCallback(int result, ArrayList<Byte> response) {
            if (result == 0) {
                mResultReceiver.send(0, createChallengeResponseBundle(response));
            } else {
                mResultReceiver.send(1, null);
            }
        }
    }

    private final class ChallengeWithDock implements Runnable {
        final byte[] mChallengeData;
        final byte mDockId;
        final ResultReceiver mResultReceiver;

        public ChallengeWithDock(final ResultReceiver resultReceiver, final byte dockId,
                                 final byte[] challengeData) {
            mDockId = dockId;
            mChallengeData = challengeData;
            mResultReceiver = resultReceiver;
        }

        @Override
        public void run() {
            if (mWirelessCharger == null) {
                return;
            }
            mWirelessCharger.challenge(mDockId, mChallengeData,
                    (WirelessCharger.ChallengeCallback)new ChallengeCallback(mResultReceiver));
        }
    }

    @VisibleForTesting
    final class DreamlinerServiceConn implements ServiceConnection {
        final Context mContext;

        public DreamlinerServiceConn(final Context context) {
            mContext = context;
        }

        public void onServiceConnected(final ComponentName componentName, final IBinder binder) {
        }

        public void onServiceDisconnected(final ComponentName componentName) {
            sendDockActiveIntent(mContext);
        }
    }

    private final class GetDockInfo implements Runnable {
        final Context mContext;
        final ResultReceiver mResultReceiver;

        public GetDockInfo(final ResultReceiver resultReceiver, final Context context) {
            mResultReceiver = resultReceiver;
            mContext = context;
        }

        @Override
        public void run() {
            if (mWirelessCharger == null) {
                return;
            }
            mWirelessCharger.getInformation(
                    (WirelessCharger.GetInformationCallback)new GetInformationCallback(
                            mResultReceiver));
        }
    }

    @VisibleForTesting
    final class GetInformationCallback implements WirelessCharger.GetInformationCallback {
        private final ResultReceiver mResultReceiver;

        GetInformationCallback(final ResultReceiver resultReceiver) {
            mResultReceiver = resultReceiver;
        }

        @Override
        public void onCallback(final int n, final DockInfo dockInfo) {
            if (n == 0) {
                mResultReceiver.send(0, dockInfo.toBundle());
            }
            else if (n != 1) {
                mResultReceiver.send(1, (Bundle)null);
            }
        }
    }

    @VisibleForTesting
    final class IsDockPresentCallback implements WirelessCharger.IsDockPresentCallback {
        private final Context mContext;

        IsDockPresentCallback(Context context) {
            mContext = context;
        }

        public void onCallback(boolean docked, byte type, byte orientation,
                               boolean isGetInfoSupported, int id) {
            if (docked) {
                startDreamlinerService(mContext, type, orientation, id);
            }
        }
    }

    @VisibleForTesting
    final class KeyExchangeCallback implements WirelessCharger.KeyExchangeCallback {
        private final ResultReceiver mResultReceiver;

        KeyExchangeCallback(ResultReceiver resultReceiver) {
            mResultReceiver = resultReceiver;
        }

        public void onCallback(int result, byte dockId, ArrayList<Byte> dockPublicKey) {
            if (result == 0) {
                mResultReceiver.send(0, createKeyExchangeResponseBundle(dockId,
                        dockPublicKey));
            } else {
                mResultReceiver.send(1, null);
            }
        }
    }

    private final class KeyExchangeWithDock implements Runnable {
        final byte[] mPublicKey;
        final ResultReceiver mResultReceiver;

        public KeyExchangeWithDock(final ResultReceiver resultReceiver, final byte[] publicKey) {
            mPublicKey = publicKey;
            mResultReceiver = resultReceiver;
        }

        @Override
        public void run() {
            if (mWirelessCharger == null) {
                return;
            }
            mWirelessCharger.keyExchange(mPublicKey,
                    (WirelessCharger.KeyExchangeCallback)new KeyExchangeCallback(mResultReceiver));
        }
    }
}

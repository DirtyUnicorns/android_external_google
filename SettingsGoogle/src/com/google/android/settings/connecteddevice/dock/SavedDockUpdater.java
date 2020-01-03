package com.google.android.settings.connecteddevice.dock;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.connecteddevice.dock.DockUpdater;
import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;
import com.google.android.settings.connecteddevice.dock.DockAsyncQueryHandler.OnQueryListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SavedDockUpdater implements DockUpdater, OnGearClickListener, OnQueryListener {
    private final DockAsyncQueryHandler mAsyncQueryHandler;
    private String mConnectedDockId = null;
    private final DockObserver mConnectedDockObserver;
    private final Context mContext;
    private final DevicePreferenceCallback mDevicePreferenceCallback;
    @VisibleForTesting
    boolean mIsObserverRegistered;
    @VisibleForTesting
    final Map<String, GearPreference> mPreferenceMap;
    private Map<String, String> mSavedDevices = null;
    private final DockObserver mSavedDockObserver;

    private class DockObserver extends ContentObserver {
        private final int mToken;
        private final Uri mUri;

        DockObserver(Handler handler, int token, Uri uri) {
            super(handler);
            mToken = token;
            mUri = uri;
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            startQuery(mToken, mUri);
        }
    }

    public SavedDockUpdater(Context context, DevicePreferenceCallback devicePreferenceCallback) {
        mContext = context;
        mDevicePreferenceCallback = devicePreferenceCallback;
        mPreferenceMap = new ArrayMap();
        Handler handler = new Handler(Looper.getMainLooper());
        mConnectedDockObserver = new DockObserver(handler, 1,
                DockContract.DOCK_PROVIDER_CONNECTED_URI);
        mSavedDockObserver = new DockObserver(handler, 2,
                DockContract.DOCK_PROVIDER_SAVED_URI);
        if (isRunningOnMainThread()) {
            mAsyncQueryHandler = new DockAsyncQueryHandler(
                    mContext.getContentResolver());
            mAsyncQueryHandler.setOnQueryListener(this);
            return;
        }
        mAsyncQueryHandler = null;
    }

    public void registerCallback() {
        ContentProviderClient client =
                mContext.getContentResolver().acquireContentProviderClient(
                        DockContract.DOCK_PROVIDER_SAVED_URI);
        if (client != null) {
            client.release();
            mContext.getContentResolver().registerContentObserver(
                    DockContract.DOCK_PROVIDER_CONNECTED_URI, false,
                    mConnectedDockObserver);
            mContext.getContentResolver().registerContentObserver(
                    DockContract.DOCK_PROVIDER_SAVED_URI, false,
                    mSavedDockObserver);
            mIsObserverRegistered = true;
            forceUpdate();
        }
    }

    public void unregisterCallback() {
        if (mIsObserverRegistered) {
            mContext.getContentResolver().unregisterContentObserver(
                    mConnectedDockObserver);
            mContext.getContentResolver().unregisterContentObserver(
                    mSavedDockObserver);
            mIsObserverRegistered = false;
        }
    }

    public void forceUpdate() {
        startQuery(1, DockContract.DOCK_PROVIDER_CONNECTED_URI);
        startQuery(2, DockContract.DOCK_PROVIDER_SAVED_URI);
    }

    public void onQueryComplete(int token, List<DockDevice> devices) {
        if (devices == null) {
            return;
        }
        if (token == 2) {
            updateSavedDevicesList(devices);
        } else if (token == 1) {
            updateConnectedDevice(devices);
        }
    }

    public void onGearClick(GearPreference p) {
        mContext.startActivity(DockContract.buildDockSettingIntent(p.getKey()));
    }

    private GearPreference initPreference(String id, String name) {
        GearPreference preference = new GearPreference(mContext, null);
        preference.setIcon((int) R.drawable.ic_dock_24dp);
        preference.setSelectable(false);
        preference.setTitle((CharSequence) name);
        if (!TextUtils.isEmpty(id)) {
            preference.setOnGearClickListener(this);
            preference.setKey(id);
        }
        return preference;
    }

    private boolean isRunningOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void startQuery(int token, Uri dockProviderUri) {
        if (isRunningOnMainThread()) {
            mAsyncQueryHandler.startQuery(token, mContext, dockProviderUri,
                    DockContract.DOCK_PROJECTION, null, null, null);
            return;
        }
        Cursor cursor;
        try {
            cursor = mContext.getApplicationContext().getContentResolver().query(
                    dockProviderUri, DockContract.DOCK_PROJECTION, null, null, null);
            onQueryComplete(token, DockAsyncQueryHandler.parseCursorToDockDevice(cursor));
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            Log.w("SavedDockUpdater", "Query dockProvider fail", ex);
        } catch (Throwable th) {
        }
    }

    private void updateConnectedDevice(List<DockDevice> devices) {
        if (devices.isEmpty()) {
            mConnectedDockId = null;
            updateDevices();
            return;
        }
        mConnectedDockId = ((DockDevice) devices.get(0)).getId();
        if (mPreferenceMap.containsKey(mConnectedDockId)) {
            mDevicePreferenceCallback.onDeviceRemoved((Preference) mPreferenceMap.get(
                    mConnectedDockId));
            mPreferenceMap.remove(mConnectedDockId);
        }
    }

    private void updateSavedDevicesList(List<DockDevice> devices) {
        if (mSavedDevices == null) {
            mSavedDevices = new ArrayMap();
        }
        mSavedDevices.clear();
        for (DockDevice device : devices) {
            String name = device.getName();
            if (!TextUtils.isEmpty(name)) {
                mSavedDevices.put(device.getId(), name);
            }
        }
        updateDevices();
    }

    private void updateDevices() {
        if (mSavedDevices != null) {
            for (String id : mSavedDevices.keySet()) {
                if (!TextUtils.equals(id, mConnectedDockId)) {
                    String name = (String) mSavedDevices.get(id);
                    if (mPreferenceMap.containsKey(id)) {
                        ((GearPreference) Objects.requireNonNull(
                                mPreferenceMap.get(id))).setTitle((CharSequence) name);
                    } else {
                        mPreferenceMap.put(id, initPreference(id, name));
                        mDevicePreferenceCallback.onDeviceAdded((
                                Preference) mPreferenceMap.get(id));
                    }
                }
            };
            mPreferenceMap.keySet().removeIf(p -> hasDeviceBeenRemoved(p));
        }
    }

    public boolean hasDeviceBeenRemoved(String id) {
        if (mSavedDevices.containsKey(id)) {
            return false;
        }
        mDevicePreferenceCallback.onDeviceRemoved((Preference) mPreferenceMap.get(id));
        return true;
    }
}

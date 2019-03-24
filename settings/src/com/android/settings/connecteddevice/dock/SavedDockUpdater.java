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
            this.mToken = token;
            this.mUri = uri;
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            SavedDockUpdater.this.startQuery(this.mToken, this.mUri);
        }
    }

    public SavedDockUpdater(Context context, DevicePreferenceCallback devicePreferenceCallback) {
        this.mContext = context;
        this.mDevicePreferenceCallback = devicePreferenceCallback;
        this.mPreferenceMap = new ArrayMap();
        Handler handler = new Handler(Looper.getMainLooper());
        this.mConnectedDockObserver = new DockObserver(handler, 1, DockContract.DOCK_PROVIDER_CONNECTED_URI);
        this.mSavedDockObserver = new DockObserver(handler, 2, DockContract.DOCK_PROVIDER_SAVED_URI);
        if (isRunningOnMainThread()) {
            this.mAsyncQueryHandler = new DockAsyncQueryHandler(this.mContext.getContentResolver());
            this.mAsyncQueryHandler.setOnQueryListener(this);
            return;
        }
        this.mAsyncQueryHandler = null;
    }

    public void registerCallback() {
        ContentProviderClient client = this.mContext.getContentResolver().acquireContentProviderClient(DockContract.DOCK_PROVIDER_SAVED_URI);
        if (client != null) {
            client.release();
            this.mContext.getContentResolver().registerContentObserver(DockContract.DOCK_PROVIDER_CONNECTED_URI, false, this.mConnectedDockObserver);
            this.mContext.getContentResolver().registerContentObserver(DockContract.DOCK_PROVIDER_SAVED_URI, false, this.mSavedDockObserver);
            this.mIsObserverRegistered = true;
            forceUpdate();
        }
    }

    public void unregisterCallback() {
        if (this.mIsObserverRegistered) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mConnectedDockObserver);
            this.mContext.getContentResolver().unregisterContentObserver(this.mSavedDockObserver);
            this.mIsObserverRegistered = false;
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
        this.mContext.startActivity(DockContract.buildDockSettingIntent(p.getKey()));
    }

    private GearPreference initPreference(String id, String name) {
        GearPreference preference = new GearPreference(this.mContext, null);
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
            this.mAsyncQueryHandler.startQuery(token, this.mContext, dockProviderUri, DockContract.DOCK_PROJECTION, null, null, null);
            return;
        }
        Cursor cursor;
        try {
            cursor = this.mContext.getApplicationContext().getContentResolver().query(dockProviderUri, DockContract.DOCK_PROJECTION, null, null, null);
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
            this.mConnectedDockId = null;
            updateDevices();
            return;
        }
        this.mConnectedDockId = ((DockDevice) devices.get(0)).getId();
        if (this.mPreferenceMap.containsKey(this.mConnectedDockId)) {
            this.mDevicePreferenceCallback.onDeviceRemoved((Preference) this.mPreferenceMap.get(this.mConnectedDockId));
            this.mPreferenceMap.remove(this.mConnectedDockId);
        }
    }

    private void updateSavedDevicesList(List<DockDevice> devices) {
        if (this.mSavedDevices == null) {
            this.mSavedDevices = new ArrayMap();
        }
        this.mSavedDevices.clear();
        for (DockDevice device : devices) {
            String name = device.getName();
            if (!TextUtils.isEmpty(name)) {
                this.mSavedDevices.put(device.getId(), name);
            }
        }
        updateDevices();
    }

    private void updateDevices() {
        if (this.mSavedDevices != null) {
            for (String id : this.mSavedDevices.keySet()) {
                if (!TextUtils.equals(id, this.mConnectedDockId)) {
                    String name = (String) this.mSavedDevices.get(id);
                    if (this.mPreferenceMap.containsKey(id)) {
                        ((GearPreference) this.mPreferenceMap.get(id)).setTitle((CharSequence) name);
                    } else {
                        this.mPreferenceMap.put(id, initPreference(id, name));
                        this.mDevicePreferenceCallback.onDeviceAdded((Preference) this.mPreferenceMap.get(id));
                    }
                }
            }
            this.mPreferenceMap.keySet().removeIf(new Lambda$SavedDockUpdater$J8Vme1TBFB2Oj8doX2QnYETFs1E(this));
        }
    }

    public boolean hasDeviceBeenRemoved(String id) {
        if (this.mSavedDevices.containsKey(id)) {
            return false;
        }
        this.mDevicePreferenceCallback.onDeviceRemoved((Preference) this.mPreferenceMap.get(id));
        return true;
    }
}
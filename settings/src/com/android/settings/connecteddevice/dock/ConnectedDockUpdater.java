package com.google.android.settings.connecteddevice.dock;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.connecteddevice.dock.DockUpdater;
import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;
import com.google.android.settings.connecteddevice.dock.DockAsyncQueryHandler.OnQueryListener;
import java.util.List;

public class ConnectedDockUpdater implements DockUpdater, OnGearClickListener, OnQueryListener {
    private final DockAsyncQueryHandler mAsyncQueryHandler;
    private final ConnectedDockObserver mConnectedDockObserver;
    private final Context mContext;
    private final DevicePreferenceCallback mDevicePreferenceCallback;
    private String mDockId = null;
    private String mDockName = null;
    @VisibleForTesting
    GearPreference mDockPreference = null;
    private final Uri mDockProviderUri;
    @VisibleForTesting
    boolean mIsObserverRegistered;

    private class ConnectedDockObserver extends ContentObserver {
        ConnectedDockObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ConnectedDockUpdater.this.forceUpdate();
        }
    }

    public ConnectedDockUpdater(Context context, DevicePreferenceCallback devicePreferenceCallback) {
        this.mContext = context;
        this.mDevicePreferenceCallback = devicePreferenceCallback;
        this.mDockProviderUri = DockContract.DOCK_PROVIDER_CONNECTED_URI;
        this.mConnectedDockObserver = new ConnectedDockObserver(new Handler(Looper.getMainLooper()));
        this.mAsyncQueryHandler = new DockAsyncQueryHandler(this.mContext.getContentResolver());
        this.mAsyncQueryHandler.setOnQueryListener(this);
    }

    public void registerCallback() {
        ContentProviderClient client = this.mContext.getContentResolver().acquireContentProviderClient(this.mDockProviderUri);
        if (client != null) {
            client.release();
            this.mContext.getContentResolver().registerContentObserver(this.mDockProviderUri, false, this.mConnectedDockObserver);
            this.mIsObserverRegistered = true;
            forceUpdate();
        }
    }

    public void unregisterCallback() {
        if (this.mIsObserverRegistered) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mConnectedDockObserver);
            this.mIsObserverRegistered = false;
        }
    }

    public void forceUpdate() {
        this.mAsyncQueryHandler.startQuery(1, this.mContext, this.mDockProviderUri, DockContract.DOCK_PROJECTION, null, null, null);
    }

    public void onQueryComplete(int token, List<DockDevice> devices) {
        if (devices != null && !devices.isEmpty()) {
            DockDevice device = (DockDevice) devices.get(0);
            this.mDockId = device.getId();
            this.mDockName = device.getName();
            updatePreference();
        } else if (this.mDockPreference != null && this.mDockPreference.isVisible()) {
            this.mDockPreference.setVisible(false);
            this.mDevicePreferenceCallback.onDeviceRemoved(this.mDockPreference);
        }
    }

    public void onGearClick(GearPreference p) {
        this.mContext.startActivity(DockContract.buildDockSettingIntent(this.mDockId));
    }

    private void updatePreference() {
        if (this.mDockPreference == null) {
            initPreference();
        }
        if (TextUtils.isEmpty(this.mDockName)) {
            if (this.mDockPreference.isVisible()) {
                this.mDockPreference.setVisible(false);
                this.mDevicePreferenceCallback.onDeviceRemoved(this.mDockPreference);
            }
            return;
        }
        this.mDockPreference.setTitle((CharSequence) this.mDockName);
        if (TextUtils.isEmpty(this.mDockId)) {
            this.mDockPreference.setOnGearClickListener(null);
        } else {
            this.mDockPreference.setOnGearClickListener(this);
        }
        if (!this.mDockPreference.isVisible()) {
            this.mDockPreference.setVisible(true);
            this.mDevicePreferenceCallback.onDeviceAdded(this.mDockPreference);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initPreference() {
        if (this.mDockPreference == null) {
            this.mDockPreference = new GearPreference(this.mContext, null);
            this.mDockPreference.setIcon((int) R.drawable.ic_dock_24dp);
            this.mDockPreference.setSummary((CharSequence) this.mContext.getString(R.string.dock_summary_charging_phone));
            this.mDockPreference.setSelectable(false);
            this.mDockPreference.setVisible(false);
        }
    }
}
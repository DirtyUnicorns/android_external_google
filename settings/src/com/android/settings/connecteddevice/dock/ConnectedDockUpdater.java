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
            forceUpdate();
        }
    }

    public ConnectedDockUpdater(Context context,
                                DevicePreferenceCallback devicePreferenceCallback) {
        mContext = context;
        mDevicePreferenceCallback = devicePreferenceCallback;
        mDockProviderUri = DockContract.DOCK_PROVIDER_CONNECTED_URI;
        mConnectedDockObserver = new ConnectedDockObserver(new Handler(Looper.getMainLooper()));
        mAsyncQueryHandler = new DockAsyncQueryHandler(mContext.getContentResolver());
        mAsyncQueryHandler.setOnQueryListener(this);
    }

    public void registerCallback() {
        ContentProviderClient client =
                mContext.getContentResolver().acquireContentProviderClient(
                        mDockProviderUri);
        if (client != null) {
            client.release();
            mContext.getContentResolver().registerContentObserver(
                    mDockProviderUri, false,
                    mConnectedDockObserver);
            mIsObserverRegistered = true;
            forceUpdate();
        }
    }

    public void unregisterCallback() {
        if (mIsObserverRegistered) {
            mContext.getContentResolver().unregisterContentObserver(
                    mConnectedDockObserver);
            mIsObserverRegistered = false;
        }
    }

    public void forceUpdate() {
        mAsyncQueryHandler.startQuery(1, mContext, mDockProviderUri,
                DockContract.DOCK_PROJECTION, null, null, null);
    }

    public void onQueryComplete(int token, List<DockDevice> devices) {
        if (devices != null && !devices.isEmpty()) {
            DockDevice device = (DockDevice) devices.get(0);
            mDockId = device.getId();
            mDockName = device.getName();
            updatePreference();
        } else if (mDockPreference != null && mDockPreference.isVisible()) {
            mDockPreference.setVisible(false);
            mDevicePreferenceCallback.onDeviceRemoved(mDockPreference);
        }
    }

    public void onGearClick(GearPreference p) {
        mContext.startActivity(DockContract.buildDockSettingIntent(mDockId));
    }

    private void updatePreference() {
        if (mDockPreference == null) {
            initPreference();
        }
        if (TextUtils.isEmpty(mDockName)) {
            if (mDockPreference.isVisible()) {
                mDockPreference.setVisible(false);
                mDevicePreferenceCallback.onDeviceRemoved(mDockPreference);
            }
            return;
        }
        mDockPreference.setTitle((CharSequence) mDockName);
        if (TextUtils.isEmpty(mDockId)) {
            mDockPreference.setOnGearClickListener(null);
        } else {
            mDockPreference.setOnGearClickListener(this);
        }
        if (!mDockPreference.isVisible()) {
            mDockPreference.setVisible(true);
            mDevicePreferenceCallback.onDeviceAdded(mDockPreference);
        }
    }

    @VisibleForTesting
    public void initPreference() {
        if (mDockPreference == null) {
            mDockPreference = new GearPreference(mContext, null);
            mDockPreference.setIcon((int) R.drawable.ic_dock_24dp);
            mDockPreference.setSummary((CharSequence) mContext.getString(
                            R.string.battery_stats_charging_label) + " " +
                            mContext.getString(R.string.usage_type_phone));
            mDockPreference.setSelectable(false);
            mDockPreference.setVisible(false);
        }
    }
}

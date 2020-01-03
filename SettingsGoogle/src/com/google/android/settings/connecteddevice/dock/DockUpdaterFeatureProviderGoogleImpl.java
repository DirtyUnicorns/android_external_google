package com.google.android.settings.connecteddevice.dock;

import android.content.Context;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.connecteddevice.dock.DockUpdater;
import com.android.settings.overlay.DockUpdaterFeatureProvider;

public class DockUpdaterFeatureProviderGoogleImpl implements DockUpdaterFeatureProvider {

    public DockUpdater getConnectedDockUpdater(Context context,
                                               DevicePreferenceCallback devicePreferenceCallback) {
        return new ConnectedDockUpdater(context, devicePreferenceCallback);
    }

    public DockUpdater getSavedDockUpdater(Context context,
                                           DevicePreferenceCallback devicePreferenceCallback) {
        return new SavedDockUpdater(context, devicePreferenceCallback);
    }
}

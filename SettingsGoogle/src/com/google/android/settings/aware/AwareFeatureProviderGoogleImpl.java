package com.google.android.settings.aware;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import com.android.settings.aware.AwareFeatureProviderImpl;

public class AwareFeatureProviderGoogleImpl extends AwareFeatureProviderImpl {
    public boolean isSupported(Context context) {
        return hasAwareSensor() && isAllowed(context);
    }

    public boolean isEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "aware_enabled", 0) == 1;
    }

    public void showRestrictionDialog(Fragment fragment) {
        AwareEnabledDialogFragment.show(fragment, false);
    }

    private static boolean isAllowed(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "aware_allowed", 0) == 1;
    }

    private static boolean hasAwareSensor() {
        return SystemProperties.getBoolean("ro.vendor.aware_available", false);
    }
}

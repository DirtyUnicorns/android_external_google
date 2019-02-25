package com.google.android.systemui.elmyra.actions;

import android.content.pm.ActivityInfo;
import android.provider.Settings;

public class LongSqueezeCustomApp extends ShortSqueezeCustomApp {

    @Override
    protected void setPackage(String packageName, String friendlyAppString) {
        Settings.Secure.putString(
                getContentResolver(), Settings.Secure.LONG_SQUEEZE_CUSTOM_APP, packageName);
        Settings.Secure.putString(
                getContentResolver(), Settings.Secure.LONG_SQUEEZE_CUSTOM_APP_FR_NAME,
                friendlyAppString);
    }

    @Override
    protected void setPackageActivity(ActivityInfo ai) {
        Settings.Secure.putString(
                getContentResolver(), Settings.Secure.LONG_SQUEEZE_CUSTOM_ACTIVITY,
                ai != null ? ai.name : "NONE");
    }
}

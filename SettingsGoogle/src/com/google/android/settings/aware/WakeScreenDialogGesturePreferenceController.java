package com.google.android.settings.aware;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;

public class WakeScreenDialogGesturePreferenceController extends AwareGesturesCategoryPreferenceController {

    public WakeScreenDialogGesturePreferenceController(Context context, String str) {
        super(context, str);
    }

    public CharSequence getSummary() {
        return mContext.getText(isGestureEnabled()
                ? R.string.ambient_display_wake_screen_summary_on : R.string.gesture_setting_off);
    }

    private boolean isGestureEnabled() {
        return mFeatureProvider.isEnabled(mContext)
                && Settings.Secure.getInt(mContext.getContentResolver(), "doze_wake_screen_gesture", 1) == 1;
    }
}

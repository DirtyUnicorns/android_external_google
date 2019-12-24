package com.google.android.settings.aware;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;

public class SkipDialogGesturePreferenceController extends AwareGesturesCategoryPreferenceController {

    public SkipDialogGesturePreferenceController(Context context, String str) {
        super(context, str);
    }

    private boolean isSkipGestureEnabled() {
        return mFeatureProvider.isEnabled(mContext)
                && Settings.Secure.getInt(mContext.getContentResolver(), "skip_gesture", 1) == 1;
    }

    public CharSequence getSummary() {
        return mContext.getText(isSkipGestureEnabled()
                ? R.string.gesture_skip_on_summary : R.string.gesture_setting_off);
    }
}

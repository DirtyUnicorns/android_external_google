package com.google.android.settings.aware;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;

public class AwareSilencePreferenceController extends AwareBasePreferenceController {

    public AwareSilencePreferenceController(Context context, String str) {
        super(context, str);
    }

    public CharSequence getSummary() {
        return mContext.getText(isSilenceGestureEnabled()
                ? R.string.gesture_silence_on_summary : R.string.gesture_setting_off);
    }

    private boolean isSilenceGestureEnabled() {
        return mFeatureProvider.isEnabled(mContext)
                && Settings.Secure.getInt(mContext.getContentResolver(), "silence_gesture", 1) == 1;
    }
}

package com.google.android.settings.aware;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;

public class AwareTapPreferenceController extends AwareBasePreferenceController {
  
    public AwareTapPreferenceController(Context context, String str) {
        super(context, str);
    }

    public CharSequence getSummary() {
        return mContext.getText(isTapGestureEnabled() ? R.string.gesture_tap_on_summary : R.string.gesture_setting_off);
    }

    public int getAvailabilityStatus() {
        if (AwareHelper.isTapAvailableOnTheDevice()) {
            return super.getAvailabilityStatus();
        }
        return 3;
    }

    private boolean isTapGestureEnabled() {
        return mFeatureProvider.isEnabled(mContext) && Settings.Secure.getInt(mContext.getContentResolver(), "tap_gesture", 0) == 1;
    }
}
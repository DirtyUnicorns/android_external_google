package com.google.android.settings.aware;

import android.content.Context;
import android.provider.Settings;
import android.util.FeatureFlagUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class SkipGestureDirectionPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public SkipGestureDirectionPreferenceController(Context context, String str) {
        super(context, str);
    }

    public int getAvailabilityStatus() {
        return FeatureFlagUtils.isEnabled(mContext, "settings_skip_direction_mutable") ? 0 : 4;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        ListPreference listPreference = (ListPreference) preference;
        listPreference.setValue(isDirectionRTL() ? "0" : "1");
        listPreference.setSummary(getSummary());
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                "skip_gesture_direction", Integer.parseInt((String) obj));
        updateState(preference);
        return true;
    }

    public CharSequence getSummary() {
        if (isDirectionRTL()) {
            return mContext.getResources().getString(R.string.gesture_skip_direction_rtl);
        }
        return mContext.getResources().getString(R.string.gesture_skip_direction_ltr);
    }

    private boolean isDirectionRTL() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
                "skip_gesture_direction", 0, -2) == 0;
    }
}

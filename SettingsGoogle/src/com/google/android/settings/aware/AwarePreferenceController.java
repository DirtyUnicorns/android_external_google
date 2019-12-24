package com.google.android.settings.aware;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.google.android.settings.aware.AwareHelper;

public class AwarePreferenceController extends TogglePreferenceController implements
        DialogInterface.OnClickListener, LifecycleObserver, OnStart, OnStop, AwareHelper.Callback {

    private static final int OFF = 0;
    private static final int ON = 1;
    private final AwareHelper mHelper;
    private Fragment mParent;
    private SwitchPreference mPref;

    public AwarePreferenceController(Context context, String str) {
        super(context, str);
        mHelper = new AwareHelper(context);
    }

    public void init(Fragment fragment) {
        mParent = fragment;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        mPref = (SwitchPreference) preferenceScreen.findPreference(getPreferenceKey());
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        int availabilityStatus = getAvailabilityStatus();
        boolean z = true;
        if (!(availabilityStatus == 0 || availabilityStatus == 1)) {
            z = false;
        }
        preference.setEnabled(z);
    }

    public int getAvailabilityStatus() {
        return (!mHelper.isSupported() || mHelper.isAirplaneModeOn()) ? 5 : 0;
    }

    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), "aware_enabled", 0) == 1;
    }

    public boolean setChecked(boolean z) {
        if (mPref.isChecked()) {
            AwareSettingsDialogFragment.show(mParent, this);
            return false;
        }
        Settings.Secure.putInt(mContext.getContentResolver(), "aware_enabled", 1);
        enableAllFeatures();
        return true;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            Settings.Secure.putInt(mContext.getContentResolver(), "aware_enabled", 0);
            mPref.setChecked(false);
        } else if (i == -2) {
            mPref.setChecked(true);
        }
    }

    public void onStart() {
        mHelper.register(this);
    }

    public void onStop() {
        mHelper.unregister();
    }

    public void onChange(Uri uri) {
        updateState(mPref);
    }

    private void enableAllFeatures() {
        ContentResolver contentResolver = mContext.getContentResolver();
        if (mHelper.readFeatureEnabled("silence_gesture")) {
            Settings.Secure.putInt(contentResolver, "silence_gesture", 1);
        }
        if (mHelper.readFeatureEnabled("skip_gesture")) {
            Settings.Secure.putInt(contentResolver, "skip_gesture", 1);
        }
        if (mHelper.readFeatureEnabled("doze_wake_display_gesture")) {
            Settings.Secure.putInt(contentResolver, "doze_wake_display_gesture", 1);
        }
        if (mHelper.readFeatureEnabled("doze_wake_screen_gesture")) {
            Settings.Secure.putInt(contentResolver, "doze_wake_screen_gesture", 1);
        }
        if (mHelper.readFeatureEnabled("aware_lock_enabled")) {
            Settings.Secure.putInt(contentResolver, "aware_lock_enabled", 1);
        }
    }
}

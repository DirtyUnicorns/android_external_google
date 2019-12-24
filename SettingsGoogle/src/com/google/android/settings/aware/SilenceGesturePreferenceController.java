package com.google.android.settings.aware;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.aware.AwareFeatureProvider;
import com.android.settings.gestures.GesturePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.VideoPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.google.android.settings.aware.AwareHelper;

public class SilenceGesturePreferenceController extends GesturePreferenceController
        implements LifecycleObserver, OnStart, OnStop, AwareHelper.Callback {

    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String PREF_KEY_VIDEO = "gesture_silence_video";
    public static final float VIDEO_HEIGHT_DP = 310.0f;
    private final AwareFeatureProvider mFeatureProvider;
    private AwareHelper mHelper;
    private Preference mPreference;

    public SilenceGesturePreferenceController(Context context, String str) {
        super(context, str);
        mFeatureProvider = FeatureFactory.getFactory(context).getAwareFeatureProvider();
        mHelper = new AwareHelper(context);
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        ((VideoPreference) preferenceScreen.findPreference(PREF_KEY_VIDEO)).setHeight(VIDEO_HEIGHT_DP);
        mPreference = preferenceScreen.findPreference(getPreferenceKey());
    }

    @Override
    protected String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public boolean isSliceable() {
        return true;
    }

    public int getAvailabilityStatus() {
        if (!mFeatureProvider.isSupported(mContext)) {
            return 3;
        }
        return !mHelper.isGestureConfigurable() ? 5 : 0;
    }

    public boolean canHandleClicks() {
        return mHelper.isGestureConfigurable();
    }

    public boolean isChecked() {
        return mFeatureProvider.isEnabled(mContext)
                && Settings.Secure.getInt(mContext.getContentResolver(), "silence_gesture", 1) == 1;
    }

    public boolean setChecked(boolean z) {
        mHelper.writeFeatureEnabled("silence_gesture", z);
        return Settings.Secure.putInt(mContext.getContentResolver(), "silence_gesture", z ? 1 : 0);
    }

    public void onStart() {
        mHelper.register(this);
    }

    public void onStop() {
        mHelper.unregister();
    }

    public void onChange(Uri uri) {
        updateState(mPreference);
    }
}

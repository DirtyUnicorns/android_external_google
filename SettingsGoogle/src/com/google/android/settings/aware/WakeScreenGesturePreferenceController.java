package com.google.android.settings.aware;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.aware.AwareFeatureProvider;
import com.android.settings.gestures.GesturePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.VideoPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.google.android.settings.aware.AwareHelper;

public class WakeScreenGesturePreferenceController extends GesturePreferenceController
        implements LifecycleObserver, OnStart, OnStop, AwareHelper.Callback {

    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String PREF_KEY_VIDEO = "gesture_wake_screen_video";
    public static final float VIDEO_HEIGHT_DP = 310.0f;
    private AmbientDisplayConfiguration mAmbientConfig;
    private final AwareFeatureProvider mFeatureProvider;
    private AwareHelper mHelper;
    private Preference mPreference;
    private final int mUserId = UserHandle.myUserId();

    public WakeScreenGesturePreferenceController(Context context, String str) {
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
        return TextUtils.equals(getPreferenceKey(), "gesture_wake_screen");
    }

    public int getAvailabilityStatus() {
        if (!getAmbientConfig().wakeScreenGestureAvailable() || !mHelper.isSupported()) {
            return 3;
        }
        return !mHelper.isGestureConfigurable() ? 5 : 0;
    }

    public boolean isChecked() {
        return getAmbientConfig().wakeLockScreenGestureEnabled(mUserId) && mFeatureProvider.isEnabled(mContext);
    }

    public boolean setChecked(boolean z) {
        mHelper.writeFeatureEnabled("doze_wake_screen_gesture", z);
        return Settings.Secure.putInt(mContext.getContentResolver(), "doze_wake_screen_gesture", z ? 1 : 0);
    }

    @VisibleForTesting
    public void setConfig(AmbientDisplayConfiguration ambientDisplayConfiguration) {
        mAmbientConfig = ambientDisplayConfiguration;
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

    public boolean canHandleClicks() {
        return mHelper.isGestureConfigurable();
    }

    private AmbientDisplayConfiguration getAmbientConfig() {
        if (mAmbientConfig == null) {
            mAmbientConfig = new AmbientDisplayConfiguration(mContext);
        }
        return mAmbientConfig;
    }
}

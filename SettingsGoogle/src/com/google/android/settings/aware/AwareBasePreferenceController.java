package com.google.android.settings.aware;

import android.content.Context;
import android.net.Uri;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.aware.AwareFeatureProvider;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.google.android.settings.aware.AwareHelper;

public class AwareBasePreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop, AwareHelper.Callback {

    protected final AwareFeatureProvider mFeatureProvider;
    protected final AwareHelper mHelper;
    private Preference mPreference;

    public AwareBasePreferenceController(Context context, String str) {
        super(context, str);
        mHelper = new AwareHelper(context);
        mFeatureProvider = FeatureFactory.getFactory(context).getAwareFeatureProvider();
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        mPreference = preferenceScreen.findPreference(getPreferenceKey());
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
        return mHelper.isGestureConfigurable() ? 0 : 5;
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

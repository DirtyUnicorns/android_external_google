package com.google.android.settings.aware;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.utils.CandidateInfoExtra;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settingslib.widget.CandidateInfo;
import com.google.android.settings.aware.AwareHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AwareDisplaySettings extends RadioButtonPickerFragment {

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER;
    @VisibleForTesting
    static final String KEY_ALWAYS_ON = "aware_always_on";
    @VisibleForTesting
    static final String KEY_OFF = "aware_wake_off";
    @VisibleForTesting
    static final String KEY_WAKE_DISPLAY = "aware_wake_display";
    private static final int MY_USER;

    static {
        MY_USER = UserHandle.myUserId();
        SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(final Context context, final boolean b) {
                final SearchIndexableResource searchIndexableResource = new SearchIndexableResource( context );
                searchIndexableResource.xmlResId = R.xml.aware_wake_display_settings;
                return Arrays.asList(searchIndexableResource);
            }

            @Override
            protected boolean isPageSearchEnabled(final Context context) {
                return SystemProperties.getBoolean("ro.vendor.aware_available", false);
            }
        };
    }

    private AmbientDisplayConfiguration mConfig;
    private AwareHelper mHelper;

    public void onAttach(Context context) {
        super.onAttach(context);
        mHelper = new AwareHelper(context);
        mConfig = new AmbientDisplayConfiguration(context);
        setIllustration(R.raw.aware_display, R.drawable.aware_display);
    }

    public RadioButtonPreference bindPreference(RadioButtonPreference radioButtonPreference,
            String str, CandidateInfo candidateInfo, String str2) {
        if (candidateInfo instanceof CandidateInfoExtra) {
            radioButtonPreference.setSummary(((CandidateInfoExtra) candidateInfo).loadSummary());
            radioButtonPreference.setAppendixVisibility(View.GONE);
        }
        super.bindPreference(radioButtonPreference, str, candidateInfo, str2);
        return radioButtonPreference;
    }

    public List<? extends CandidateInfo> getCandidates() {
        Context context = getContext();
        ArrayList arrayList = new ArrayList();
        if (mHelper.isSupported()) {
            arrayList.add(new CandidateInfoExtra(context.getText(R.string.aware_wake_display_title),
                    context.getText(R.string.aware_wake_display_summary), KEY_WAKE_DISPLAY,
                    mHelper.isGestureConfigurable()));
        }
        if (mConfig.alwaysOnAvailableForUser(MY_USER)) {
            arrayList.add(new CandidateInfoExtra(context.getText(R.string.doze_always_on_title),
                    context.getText(R.string.doze_always_on_summary), KEY_ALWAYS_ON, true));
        }
        arrayList.add(new CandidateInfoExtra(context.getText(R.string.switch_off_text),
                (CharSequence) null, KEY_OFF, true));
        return arrayList;
    }

    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.aware_wake_display_settings;
    }

    @Override
    protected String getDefaultKey() {
        final boolean wakeDisplayGestureEnabled =
                mConfig.wakeDisplayGestureEnabled(AwareDisplaySettings.MY_USER);
        final boolean alwaysOnEnabled = mConfig.alwaysOnEnabled(AwareDisplaySettings.MY_USER);
        if (wakeDisplayGestureEnabled && mHelper.isGestureConfigurable() && alwaysOnEnabled) {
            return KEY_WAKE_DISPLAY;
        }
        if (alwaysOnEnabled) {
            return KEY_ALWAYS_ON;
        }
        return KEY_OFF;
    }

    @Override
    protected boolean setDefaultKey(final String s) {
        final ContentResolver contentResolver = getContext().getContentResolver();
        final int hashCode = s.hashCode();
        int n = 0;
        Label_0078:
        {
            if (hashCode != -2133849746) {
                if (hashCode != -1652972184) {
                    if (hashCode == 598899989) {
                        if (s.equals(KEY_OFF)) {
                            n = 2;
                            break Label_0078;
                        }
                    }
                } else if (s.equals(KEY_WAKE_DISPLAY)) {
                    n = 0;
                    break Label_0078;
                }
            } else if (s.equals(KEY_ALWAYS_ON)) {
                n = 1;
                break Label_0078;
            }
            n = -1;
        }
        if (n != 0) {
            if (n != 1) {
                if (n == 2) {
                    mHelper.writeFeatureEnabled("doze_always_on", false);
                    Settings.Secure.putInt(contentResolver, "doze_always_on", 0);
                    mHelper.writeFeatureEnabled("doze_wake_display_gesture", false);
                    Settings.Secure.putInt(contentResolver, "doze_wake_display_gesture", 0);
                }
            } else {
                mHelper.writeFeatureEnabled("doze_always_on", true);
                Settings.Secure.putInt(contentResolver, "doze_always_on", 1);
                mHelper.writeFeatureEnabled("doze_wake_display_gesture", false);
                Settings.Secure.putInt(contentResolver, "doze_wake_display_gesture", 0);
            }
        } else {
            mHelper.writeFeatureEnabled("doze_always_on", true );
            Settings.Secure.putInt(contentResolver, "doze_always_on", 1);
            mHelper.writeFeatureEnabled("doze_wake_display_gesture", true);
            Settings.Secure.putInt(contentResolver, "doze_wake_display_gesture", 1);
        }
        return true;
    }
}


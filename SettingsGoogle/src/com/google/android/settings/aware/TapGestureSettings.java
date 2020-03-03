package com.google.android.settings.aware;

import android.content.Context;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class TapGestureSettings extends DashboardFragment {

    private static final String TAG = "TapGestureSettings";

    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }

    public int getPreferenceScreenResId() {
        return R.xml.tap_gesture_settings;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.tap_gesture_settings;
                    return Arrays.asList(sir);
                }
            };
}
package com.google.android.settings.aware;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.Arrays;
import java.util.List;

public class AwareSettings extends DashboardFragment {

    private static final String TAG = "AwareSettings";

    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }

    public int getPreferenceScreenResId() {
        return R.xml.aware_settings;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((AwarePreferenceController) use(AwarePreferenceController.class)).init(this);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean("show_aware_dialog_enabled", false)) {
            AwareEnabledDialogFragment.show(this, true);
        }
    }
}


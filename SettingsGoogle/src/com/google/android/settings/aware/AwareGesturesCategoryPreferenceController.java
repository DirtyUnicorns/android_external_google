package com.google.android.settings.aware;

import android.content.Context;
import android.os.SystemProperties;

import com.android.settings.aware.AwareFeatureProvider;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;

public class AwareGesturesCategoryPreferenceController extends BasePreferenceController {

    final Context mContext;
    final AwareFeatureProvider mFeatureProvider;

    public AwareGesturesCategoryPreferenceController(Context context, String str) {
        super(context, str);
        mContext = context;
        mFeatureProvider = FeatureFactory.getFactory(context).getAwareFeatureProvider();
    }

    public int getAvailabilityStatus() {
        return SystemProperties.getBoolean("ro.vendor.aware_available", false) ? 0 : 3;
    }
}

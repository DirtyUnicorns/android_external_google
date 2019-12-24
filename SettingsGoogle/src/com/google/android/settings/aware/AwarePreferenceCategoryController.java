package com.google.android.settings.aware;

import android.content.Context;

import com.android.settings.aware.AwareFeatureProvider;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.PreferenceCategoryController;

public class AwarePreferenceCategoryController extends PreferenceCategoryController {

    private final AwareFeatureProvider mFeatureProvider;

    public AwarePreferenceCategoryController(Context context, String str) {
        super(context, str);
        mFeatureProvider = FeatureFactory.getFactory(context).getAwareFeatureProvider();
    }

    public int getAvailabilityStatus() {
        return mFeatureProvider.isSupported(mContext) ? 0 : 3;
    }
}

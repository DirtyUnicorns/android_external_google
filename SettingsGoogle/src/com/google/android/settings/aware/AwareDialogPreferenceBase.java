package com.google.android.settings.aware;

import androidx.preference.PreferenceViewHolder;
import android.net.Uri;
import android.util.AttributeSet;
import android.content.Context;
import android.view.View;

import com.android.settingslib.CustomDialogPreferenceCompat;
import com.android.settings.R;
import com.google.android.settings.aware.AwareHelper;

public class AwareDialogPreferenceBase extends CustomDialogPreferenceCompat {

    protected AwareHelper mHelper;
    private View mInfoIcon;
    private View mSummary;
    private View mTitle;

    public AwareDialogPreferenceBase(Context context) {
        super(context);
        init();
    }

    public AwareDialogPreferenceBase(Context context, AttributeSet set) {
        super(context, set);
        init();
    }

    public AwareDialogPreferenceBase(Context context, AttributeSet set, int n) {
        super(context, set, n);
        init();
    }

    public AwareDialogPreferenceBase(Context context, AttributeSet set, int n, int n2) {
        super(context, set, n, n2);
        init();
    }

    private void init() {
        setWidgetLayoutResource(R.layout.preference_widget_info);
        (mHelper = new AwareHelper(getContext())).register( uri ->
                updatePreference() );
    }

    protected boolean isAvailable() {
        return false;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        mTitle = preferenceViewHolder.findViewById(16908310);
        mSummary = preferenceViewHolder.findViewById(16908304);
        mInfoIcon = preferenceViewHolder.findViewById(R.id.info_button);
        updatePreference();
    }

    @Override
    public void performClick() {
        if (isAvailable()) {
            performEnabledClick();
        }
        else if (!mHelper.isAirplaneModeOn()) {
            super.performClick();
        }
    }

    protected void performEnabledClick() {
    }

    protected void updatePreference() {
        View view = mTitle;
        if (view != null) {
            view.setEnabled(isAvailable());
        }
        View summaryView = mSummary;
        if (summaryView != null) {
            summaryView.setEnabled(isAvailable());
        }
        View infoIconView = mInfoIcon;
        if (infoIconView != null) {
            int visibility;
            if (!isAvailable() && !mHelper.isAirplaneModeOn()) {
                visibility = View.VISIBLE;
            }
            else {
                visibility = View.GONE;
            }
            infoIconView.setVisibility(visibility);
        }
    }
}

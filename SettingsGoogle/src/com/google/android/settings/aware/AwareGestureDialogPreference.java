package com.google.android.settings.aware;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import androidx.appcompat.app.AlertDialog;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

abstract class AwareGestureDialogPreference extends AwareDialogPreferenceBase
        implements DialogInterface.OnClickListener {

    public abstract String getDestination();
    public abstract int getDialogDisabledMessage();
    public abstract int getGestureDialogMessage();
    public abstract int getGestureDialogTitle();
    public abstract int getSourceMetricsCategory();

    public AwareGestureDialogPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public boolean isAvailable() {
        return mHelper.isGestureConfigurable();
    }

    public void performEnabledClick() {
        new SubSettingLauncher(getContext()).setDestination(getDestination())
                .setSourceMetricsCategory(getSourceMetricsCategory()).launch();
    }

    public void onPrepareDialogBuilder(AlertDialog.Builder builder,
            DialogInterface.OnClickListener onClickListener) {
        super.onPrepareDialogBuilder(builder, onClickListener);
        if (!mHelper.isSupported()) {
            builder.setTitle(getGestureDialogTitle());
            builder.setMessage(getDialogDisabledMessage());
            builder.setPositiveButton((int) R.string.gesture_aware_confirmation_action_button,
                    (DialogInterface.OnClickListener) null);
            builder.setNegativeButton((CharSequence) "", (DialogInterface.OnClickListener) null);
            return;
        }
        builder.setTitle(getGestureDialogTitle());
        builder.setMessage(getGestureDialogMessage());
        builder.setPositiveButton((int) R.string.aware_disabled_preference_action,
                (DialogInterface.OnClickListener) this);
        builder.setNegativeButton((int) R.string.aware_disabled_preference_neutral,
                (DialogInterface.OnClickListener) null);
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        new SubSettingLauncher(getContext()).setDestination(AwareSettings.class.getName())
                .setSourceMetricsCategory(getSourceMetricsCategory()).launch();
    }
}

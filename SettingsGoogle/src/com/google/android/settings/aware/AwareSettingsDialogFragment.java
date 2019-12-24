package com.google.android.settings.aware;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class AwareSettingsDialogFragment extends InstrumentedDialogFragment {

    private static DialogInterface.OnClickListener mClickListener;

    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }

    public static void show(Fragment fragment, DialogInterface.OnClickListener onClickListener) {
        mClickListener = onClickListener;
        AwareSettingsDialogFragment awareSettingsDialogFragment = new AwareSettingsDialogFragment();
        awareSettingsDialogFragment.setTargetFragment(fragment, 0);
        awareSettingsDialogFragment.show(fragment.getFragmentManager(), "AwareSettingsDialog");
    }

    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle((int) R.string.dialog_aware_settings_title);
        builder.setMessage((int) R.string.dialog_aware_settings_message);
        builder.setPositiveButton((int) R.string.condition_turn_off, mClickListener);
        builder.setNegativeButton((int) R.string.cancel, mClickListener);
        return builder.create();
    }
}

package com.google.android.settings.aware;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.om.IOverlayManager;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.overlay.FeatureFactory;

public class AwareSettingsDialogPreference extends AwareDialogPreferenceBase {

    private final View.OnClickListener mClickListener = v -> performClick(v);
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
    private IOverlayManager mOverlayManager;
    private int mCurrentUserId;

    public AwareSettingsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        mCurrentUserId = ActivityManager.getCurrentUser();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);

        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove,
                R.styleable.Preference_allowDividerAbove, false);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow,
                R.styleable.Preference_allowDividerBelow, false);
        a.recycle();

        setLayoutResource(R.layout.category_preference);
    }

    public boolean isAvailable() {
        return FeatureFactory.getFactory(getContext()).getAwareFeatureProvider()
                .isSupported(getContext()) && !mHelper.isAirplaneModeOn();
    }

    public void performEnabledClick() {
        new SubSettingLauncher(getContext()).setDestination(AwareSettings.class.getName())
                .setSourceMetricsCategory(744).launch();
    }

    public void onPrepareDialogBuilder(AlertDialog.Builder builder,
            DialogInterface.OnClickListener onClickListener) {
        super.onPrepareDialogBuilder(builder, onClickListener);
        builder.setTitle((int) R.string.aware_settings_disabled_info_dialog_title);
        builder.setMessage((int) R.string.aware_settings_disabled_info_dialog_content);
        builder.setPositiveButton((int) R.string.nfc_how_it_works_got_it,
                (DialogInterface.OnClickListener) null);
        builder.setNegativeButton((CharSequence) "", (DialogInterface.OnClickListener) null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnClickListener(mClickListener);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        holder.setDividerAllowedAbove(mAllowDividerAbove);
        holder.setDividerAllowedBelow(mAllowDividerBelow);

        ImageView imageview = (ImageView) holder.findViewById(android.R.id.icon);
    }
}

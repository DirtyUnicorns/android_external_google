package com.google.android.systemui.assist.uihints;

import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;
import com.google.android.systemui.assist.uihints.OverlayUiHost;

public class ZeroStateIconView extends FrameLayout implements OverlayUiHost.BottomMarginListener {
    private final int COLOR_DARK_BACKGROUND;
    private final int COLOR_LIGHT_BACKGROUND;
    private final int mMargin;
    private ImageView mZeroStateIcon;

    public ZeroStateIconView(Context context) {
        this(context, null);
    }

    public ZeroStateIconView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ZeroStateIconView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ZeroStateIconView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        COLOR_DARK_BACKGROUND = getResources().getColor(R.color.transcription_icon_dark);
        COLOR_LIGHT_BACKGROUND = getResources().getColor(R.color.transcription_icon_light);
        mMargin = getResources().getDimensionPixelSize(R.dimen.zerostate_icon_bottom_margin);
    }

    public void onBottomMarginChanged(int i) {
        ((ViewGroup.MarginLayoutParams) getLayoutParams()).bottomMargin = i + mMargin;
        requestLayout();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mZeroStateIcon = findViewById(R.id.zerostate_icon_image);
    }

    public void setHasDarkBackground(boolean z) {
        mZeroStateIcon.setImageTintList(ColorStateList.valueOf(z ? COLOR_DARK_BACKGROUND : COLOR_LIGHT_BACKGROUND));
    }

    public void show(PendingIntent pendingIntent) {
        if (pendingIntent != null) {
            mZeroStateIcon.setOnClickListener(view -> lambda$show$00(pendingIntent, view));
            setVisibility(0);
            return;
        }
        mZeroStateIcon.setOnClickListener(null);
        setVisibility(8);
        Log.w("ZeroStateIconView", "No zerostate intent specified");
    }

    static /* synthetic */ void lambda$show$00(PendingIntent pendingIntent, View view) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.w("ZeroStateIconView", "Pending intent cancelled", e);
        }
    }

    public void hide() {
        setVisibility(8);
    }

    void onDensityChanged() {
        mZeroStateIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_explore));
    }
}

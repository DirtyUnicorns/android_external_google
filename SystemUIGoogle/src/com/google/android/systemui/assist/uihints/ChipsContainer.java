package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.google.android.systemui.assist.uihints.TranscriptionController;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;

public class ChipsContainer extends LinearLayout implements TranscriptionController.TranscriptionSpaceView {
    private final Drawable BACKGROUND_DARK;
    private final Drawable BACKGROUND_LIGHT;
    private final int TEXT_COLOR_DARK;
    private final int TEXT_COLOR_LIGHT;
    private boolean mDarkBackground;

    public ChipsContainer(Context context) {
        this(context, null);
    }

    public ChipsContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ChipsContainer(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ChipsContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.BACKGROUND_DARK = context.getDrawable(R.drawable.assist_chip_background_dark);
        this.BACKGROUND_LIGHT = context.getDrawable(R.drawable.assist_chip_background_light);
        this.TEXT_COLOR_DARK = context.getColor(R.color.assist_chip_text_dark);
        this.TEXT_COLOR_LIGHT = context.getColor(R.color.assist_chip_text_light);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, com.google.android.systemui.assist.uihints.ChipsContainer, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public void setChips(List<Bundle> list) {
        removeAllViews();
        for (Bundle next : list) {
            ChipView chipView = (ChipView) LayoutInflater.from(getContext()).inflate(R.layout.assist_chip, (ViewGroup) this, false);
            super.setBackground(this.mDarkBackground ? this.BACKGROUND_DARK : this.BACKGROUND_LIGHT);
            chipView.setLabelColor(this.mDarkBackground ? this.TEXT_COLOR_DARK : this.TEXT_COLOR_LIGHT);
            if (chipView.setChip(next)) {
                addView(chipView);
            }
        }
    }

    public void show() {
        setVisibility(0);
    }

    public ListenableFuture<Void> hide(boolean z) {
        removeAllViews();
        setVisibility(8);
        return Futures.immediateFuture(null);
    }

    public void setHasDarkBackground(boolean z) {
        if (this.mDarkBackground != z) {
            this.mDarkBackground = z;
            for (int i = 0; i < getChildCount(); i++) {
                ChipView chipView = (ChipView) getChildAt(i);
                super.setBackground(z ? this.BACKGROUND_DARK : this.BACKGROUND_LIGHT);
                chipView.setLabelColor(z ? this.TEXT_COLOR_DARK : this.TEXT_COLOR_LIGHT);
            }
        }
    }

    public void onFontSizeChanged() {
        float dimension = super.mContext.getResources().getDimension(R.dimen.assist_chip_text_size);
        for (int i = 0; i < getChildCount(); i++) {
            ((ChipView) getChildAt(i)).updateTextSize(dimension);
        }
        requestLayout();
    }
}

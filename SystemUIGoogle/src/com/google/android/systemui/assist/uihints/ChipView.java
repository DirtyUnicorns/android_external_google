package com.google.android.systemui.assist.uihints;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import com.android.systemui.R;
import com.google.common.base.Preconditions;

public class ChipView extends LinearLayout {
    private ImageView mIconView;
    private TextView mLabelView;
    private Space mSpaceView;

    public ChipView(Context context) {
        super(context);
    }

    public ChipView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ChipView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public ChipView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ImageView imageView = findViewById(R.id.chip_icon);
        Preconditions.checkNotNull(imageView);
        mIconView = imageView;
        TextView textView = findViewById(R.id.chip_label);
        Preconditions.checkNotNull(textView);
        mLabelView = textView;
        Space space = findViewById(R.id.chip_element_padding);
        Preconditions.checkNotNull(space);
        mSpaceView = space;
    }

    boolean setChip(Bundle bundle) {
        Icon icon = bundle.getParcelable("icon");
        String string = bundle.getString("label");
        if (icon == null && (string == null || string.length() == 0)) {
            Log.w("ChipView", "Neither icon nor label provided");
            return false;
        }
        if (icon == null) {
            mIconView.setVisibility(8);
            mSpaceView.setVisibility(8);
            mLabelView.setText(string);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mLabelView.getLayoutParams();
            layoutParams.setMargins(layoutParams.rightMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
        } else if (string == null || string.length() == 0) {
            mLabelView.setVisibility(8);
            mSpaceView.setVisibility(8);
            mIconView.setImageIcon(icon);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) mIconView.getLayoutParams();
            layoutParams2.setMargins(layoutParams2.leftMargin, layoutParams2.topMargin, layoutParams2.leftMargin, layoutParams2.bottomMargin);
        } else {
            mIconView.setImageIcon(icon);
            mLabelView.setText(string);
        }
        setTapAction(bundle.getParcelable("tap_action"));
        return true;
    }

    void setLabelColor(int i) {
        mLabelView.setTextColor(i);
    }

    void updateTextSize(float f) {
        mLabelView.setTextSize(0, f);
    }

    private void setTapAction(PendingIntent pendingIntent) {
        if (pendingIntent != null) {
            setOnClickListener(view -> lambda$setTapAction$00(pendingIntent, view));
        }
    }

    static /* synthetic */ void lambda$setTapAction$00(PendingIntent pendingIntent, View view) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.w("ChipView", "Pending intent cancelled", e);
        }
    }
}

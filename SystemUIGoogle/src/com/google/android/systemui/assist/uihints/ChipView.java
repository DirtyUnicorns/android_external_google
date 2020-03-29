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

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        ImageView imageView = (ImageView) findViewById(R.id.chip_icon);
        Preconditions.checkNotNull(imageView);
        this.mIconView = imageView;
        TextView textView = (TextView) findViewById(R.id.chip_label);
        Preconditions.checkNotNull(textView);
        this.mLabelView = textView;
        Space space = (Space) findViewById(R.id.chip_element_padding);
        Preconditions.checkNotNull(space);
        this.mSpaceView = space;
    }

    /* access modifiers changed from: package-private */
    public boolean setChip(Bundle bundle) {
        Icon icon = (Icon) bundle.getParcelable("icon");
        String string = bundle.getString("label");
        if (icon == null && (string == null || string.length() == 0)) {
            Log.w("ChipView", "Neither icon nor label provided");
            return false;
        }
        if (icon == null) {
            this.mIconView.setVisibility(8);
            this.mSpaceView.setVisibility(8);
            this.mLabelView.setText(string);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mLabelView.getLayoutParams();
            layoutParams.setMargins(layoutParams.rightMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
        } else if (string == null || string.length() == 0) {
            this.mLabelView.setVisibility(8);
            this.mSpaceView.setVisibility(8);
            this.mIconView.setImageIcon(icon);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mIconView.getLayoutParams();
            layoutParams2.setMargins(layoutParams2.leftMargin, layoutParams2.topMargin, layoutParams2.leftMargin, layoutParams2.bottomMargin);
        } else {
            this.mIconView.setImageIcon(icon);
            this.mLabelView.setText(string);
        }
        setTapAction((PendingIntent) bundle.getParcelable("tap_action"));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setLabelColor(int i) {
        this.mLabelView.setTextColor(i);
    }

    /* access modifiers changed from: package-private */
    public void updateTextSize(float f) {
        this.mLabelView.setTextSize(0, f);
    }

    private void setTapAction(PendingIntent pendingIntent) {
        if (pendingIntent != null) {
            setOnClickListener(new View.OnClickListener(pendingIntent) {
                /* class com.google.android.systemui.assist.uihints.$$Lambda$ChipView$rZsGRy3pJJ_MHCfVghF5de5ZRg */
                private final /* synthetic */ PendingIntent f$0;

                {
                    this.f$0 = r1;
                }

                public final void onClick(View view) {
                    ChipView.lambda$setTapAction$0(this.f$0, view);
                }
            });
        }
    }

    static /* synthetic */ void lambda$setTapAction$0(PendingIntent pendingIntent, View view) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.w("ChipView", "Pending intent cancelled", e);
        }
    }
}

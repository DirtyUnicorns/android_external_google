package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.google.android.systemui.assist.uihints.OverlayUiHost;

public class PromptView extends TextView implements ConfigurationController.ConfigurationListener,
        OverlayUiHost.BottomMarginListener {
    private final DecelerateInterpolator mDecelerateInterpolator;
    private boolean mEnabled;
    private final String mHandleString;
    private boolean mHasDarkBackground;
    private int mLastInvocationType;
    private final int mMargin;
    private final float mRiseDistance;
    private final String mSqueezeString;
    private final int mTextColorDark;
    private final int mTextColorLight;

    public PromptView(Context context) {
        this(context, null);
    }

    public PromptView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PromptView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PromptView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        mDecelerateInterpolator = new DecelerateInterpolator(2.0f);
        mHasDarkBackground = false;
        mEnabled = false;
        mLastInvocationType = 0;
        mTextColorDark = getResources().getColor(R.color.transcription_text_dark);
        mTextColorLight = getResources().getColor(R.color.transcription_text_light);
        mRiseDistance = getResources().getDimension(R.dimen.assist_prompt_rise_distance);
        mHandleString = getResources().getString(R.string.handle_invocation_prompt);
        mSqueezeString = getResources().getString(R.string.squeeze_invocation_prompt);
        mMargin = getResources().getDimensionPixelSize(R.dimen.assist_prompt_start_height);
        Dependency.get(ConfigurationController.class).addCallback(this);
        setHasDarkBackground(!mHasDarkBackground);
    }

    @Override
    public void onDensityOrFontScaleChanged() {
        setTextSize(0, mContext.getResources().getDimension(R.dimen.transcription_text_size));
        updateViewHeight();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateViewHeight();
    }

    public void setHasDarkBackground(boolean z) {
        if (z != mHasDarkBackground) {
            setTextColor(z ? mTextColorDark : mTextColorLight);
            mHasDarkBackground = z;
        }
    }

    public void enable() {
        mEnabled = true;
    }

    public void disable() {
        mEnabled = false;
        setVisibility(8);
    }

    public void onInvocationProgress(int i, float f) {
        if (f <= 1.0f) {
            if (f == 0.0f) {
                setVisibility(8);
                setAlpha(0.0f);
                setTranslationY(0.0f);
                mLastInvocationType = 0;
            } else if (mEnabled) {
                if (i != 1) {
                    if (i != 2) {
                        mLastInvocationType = 0;
                        setText("");
                    } else if (mLastInvocationType != i) {
                        mLastInvocationType = i;
                        setText(mSqueezeString);
                        announceForAccessibility(mSqueezeString);
                    }
                } else if (mLastInvocationType != i) {
                    mLastInvocationType = i;
                    setText(mHandleString);
                    announceForAccessibility(mHandleString);
                }
                setVisibility(0);
                setTranslationYProgress(f);
                setAlphaProgress(i, f);
            }
        }
    }

    private void setTranslationYProgress(float f) {
        setTranslationY((-mRiseDistance) * f);
    }

    private void setAlphaProgress(int i, float f) {
        if (i != 2 && f > 0.8f) {
            setAlpha(0.0f);
        } else if (f > 0.32000002f) {
            setAlpha(1.0f);
        } else {
            setAlpha(mDecelerateInterpolator.getInterpolation(f / 0.32000002f));
        }
    }

    private void updateViewHeight() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = (int) (getResources().getDimension(R.dimen.assist_prompt_start_height)
                    + mRiseDistance + mContext.getResources().getDimension(R.dimen.transcription_text_size));
        }
        requestLayout();
    }

    public void onBottomMarginChanged(int i) {
        ((ViewGroup.MarginLayoutParams) getLayoutParams()).bottomMargin = i + mMargin;
        requestLayout();
    }
}

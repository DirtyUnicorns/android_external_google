package com.google.android.systemui.assist.uihints;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import com.android.systemui.R;
import com.google.android.systemui.assist.uihints.GreetingView;
import com.google.android.systemui.assist.uihints.TranscriptionController;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Iterator;

public class GreetingView extends TextView implements TranscriptionController.TranscriptionSpaceView {
    private final int START_DELTA;
    private final int TEXT_COLOR_DARK;
    private final int TEXT_COLOR_LIGHT;
    private AnimatorSet mAnimatorSet;
    private final SpannableStringBuilder mGreetingBuilder;
    private float mMaxAlpha;
    private final ArrayList<StaggeredSpan> mSpans;

    public void show() {
    }

    public GreetingView(Context context) {
        this(context, null);
    }

    public GreetingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GreetingView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public GreetingView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        mGreetingBuilder = new SpannableStringBuilder();
        mSpans = new ArrayList<>();
        mAnimatorSet = new AnimatorSet();
        TEXT_COLOR_DARK = getResources().getColor(R.color.transcription_text_dark);
        TEXT_COLOR_LIGHT = getResources().getColor(R.color.transcription_text_light);
        START_DELTA = (int) getResources().getDimension(R.dimen.assist_greeting_start_delta);
        mMaxAlpha = Color.alpha(getCurrentTextColor());
    }

    public void onFontSizeChanged() {
        setTextSize(0, mContext.getResources().getDimension(R.dimen.transcription_text_size));
    }

    public void setGreeting(String str, float f) {
        if (getVisibility() != 0) {
            setPadding(0, 0, 0, -START_DELTA);
            setUpTextSpans(str);
            setText(mGreetingBuilder);
            animateIn(Math.abs(f));
            return;
        }
        setPadding(0, 0, 0, 0);
        setText(str);
    }

    public ListenableFuture<Void> hide(boolean z) {
        if (mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
        setVisibility(8);
        return Futures.immediateFuture(null);
    }

    public void setHasDarkBackground(boolean z) {
        setTextColor(z ? TEXT_COLOR_DARK : TEXT_COLOR_LIGHT);
        mMaxAlpha = (float) Color.alpha(getCurrentTextColor());
    }

    private void animateIn(float velocity) {
        if (mAnimatorSet.isRunning()) {
            Log.w("GreetingView", "Already animating in greeting view; ignoring");
            return;
        }
        mAnimatorSet = new AnimatorSet();
        float min = Math.min(10.0f, (velocity / 1.2f) + 3.0f);
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(min);
        long j = 0;
        for (StaggeredSpan mSpan : mSpans) {
            mSpan.initAnimator(j, overshootInterpolator, mAnimatorSet);
            j += 8;
        }
        setLayoutParams(min, overshootInterpolator);
        mAnimatorSet.start();
    }

    private void setLayoutParams(float f, OvershootInterpolator overshootInterpolator) {
        float convertSpToPx = (float) DisplayUtils.convertSpToPx(getResources().getDimension(R.dimen.transcription_text_size), mContext);
        float interpolation = ((float) START_DELTA) * overshootInterpolator.getInterpolation(((2.0f * f) + 6.0f) / ((f * 6.0f) + 6.0f));
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = (int) (interpolation + convertSpToPx);
        }
        setVisibility(0);
        requestLayout();
    }

    private void setUpTextSpans(String str) {
        String[] split = str.split("\\s+");
        mGreetingBuilder.clear();
        mSpans.clear();
        mGreetingBuilder.append(str);
        int length = split.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            String str2 = split[i];
            StaggeredSpan staggeredSpan = new StaggeredSpan();
            int indexOf = str.indexOf(str2, i2);
            int length2 = str2.length() + indexOf;
            mGreetingBuilder.setSpan(staggeredSpan, indexOf, length2, 33);
            mSpans.add(staggeredSpan);
            i++;
            i2 = length2;
        }
    }

    private class StaggeredSpan extends CharacterStyle {
        private int mAlpha;
        private int mShift;

        private StaggeredSpan() {
            mShift = 0;
            mAlpha = 0;
        }

        public void updateDrawState(TextPaint textPaint) {
            textPaint.baselineShift -= mShift;
            textPaint.setAlpha(mAlpha);
            invalidate();
        }

        void initAnimator(long j, OvershootInterpolator overshootInterpolator, AnimatorSet animatorSet) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setInterpolator(overshootInterpolator);
            ofFloat.addUpdateListener(valueAnimator -> mShift = (int) (((float) START_DELTA) * (Float) valueAnimator.getAnimatedValue()));
            ofFloat.setDuration(400);
            ofFloat.setStartDelay(j);
            animatorSet.play(ofFloat);
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat2.addUpdateListener(valueAnimator -> mAlpha = (int) ((Float) valueAnimator.getAnimatedValue() * mMaxAlpha));
            ofFloat2.setDuration(100);
            ofFloat2.setStartDelay(j);
            animatorSet.play(ofFloat2);
        }
    }
}

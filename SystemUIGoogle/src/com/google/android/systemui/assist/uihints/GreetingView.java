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
    /* access modifiers changed from: private */
    public final int START_DELTA;
    private final int TEXT_COLOR_DARK;
    private final int TEXT_COLOR_LIGHT;
    private AnimatorSet mAnimatorSet;
    private final SpannableStringBuilder mGreetingBuilder;
    /* access modifiers changed from: private */
    public float mMaxAlpha;
    private final ArrayList<StaggeredSpan> mSpans;
    private float mVelocity;

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
        this.mGreetingBuilder = new SpannableStringBuilder();
        this.mSpans = new ArrayList<>();
        this.mAnimatorSet = new AnimatorSet();
        this.TEXT_COLOR_DARK = getResources().getColor(R.color.transcription_text_dark);
        this.TEXT_COLOR_LIGHT = getResources().getColor(R.color.transcription_text_light);
        this.START_DELTA = (int) getResources().getDimension(R.dimen.assist_greeting_start_delta);
        this.mMaxAlpha = (float) Color.alpha(getCurrentTextColor());
    }

    public void onFontSizeChanged() {
        setTextSize(0, super.mContext.getResources().getDimension(R.dimen.transcription_text_size));
    }

    public void setGreeting(String str, float f) {
        this.mVelocity = Math.abs(f);
        if (getVisibility() != 0) {
            setPadding(0, 0, 0, -this.START_DELTA);
            setUpTextSpans(str);
            setText(this.mGreetingBuilder);
            animateIn();
            return;
        }
        setPadding(0, 0, 0, 0);
        setText(str);
    }

    public ListenableFuture<Void> hide(boolean z) {
        if (this.mAnimatorSet.isRunning()) {
            this.mAnimatorSet.cancel();
        }
        setVisibility(8);
        return Futures.immediateFuture(null);
    }

    public void setHasDarkBackground(boolean z) {
        setTextColor(z ? this.TEXT_COLOR_DARK : this.TEXT_COLOR_LIGHT);
        this.mMaxAlpha = (float) Color.alpha(getCurrentTextColor());
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.min(float, float):float}
     arg types: [int, float]
     candidates:
      ClspMth{java.lang.Math.min(double, double):double}
      ClspMth{java.lang.Math.min(long, long):long}
      ClspMth{java.lang.Math.min(int, int):int}
      ClspMth{java.lang.Math.min(float, float):float} */
    private void animateIn() {
        if (this.mAnimatorSet.isRunning()) {
            Log.w("GreetingView", "Already animating in greeting view; ignoring");
            return;
        }
        this.mAnimatorSet = new AnimatorSet();
        float min = Math.min(10.0f, (this.mVelocity / 1.2f) + 3.0f);
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(min);
        long j = 0;
        Iterator<StaggeredSpan> it = this.mSpans.iterator();
        while (it.hasNext()) {
            it.next().initAnimator(j, overshootInterpolator, this.mAnimatorSet);
            j += 8;
        }
        setLayoutParams(min, overshootInterpolator);
        this.mAnimatorSet.start();
    }

    private void setLayoutParams(float f, OvershootInterpolator overshootInterpolator) {
        float convertSpToPx = (float) DisplayUtils.convertSpToPx(getResources().getDimension(R.dimen.transcription_text_size), super.mContext);
        float interpolation = ((float) this.START_DELTA) * overshootInterpolator.getInterpolation(((2.0f * f) + 6.0f) / ((f * 6.0f) + 6.0f));
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = (int) (interpolation + convertSpToPx);
        }
        setVisibility(0);
        requestLayout();
    }

    private void setUpTextSpans(String str) {
        String[] split = str.split("\\s+");
        this.mGreetingBuilder.clear();
        this.mSpans.clear();
        this.mGreetingBuilder.append((CharSequence) str);
        int length = split.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            String str2 = split[i];
            StaggeredSpan staggeredSpan = new StaggeredSpan();
            int indexOf = str.indexOf(str2, i2);
            int length2 = str2.length() + indexOf;
            this.mGreetingBuilder.setSpan(staggeredSpan, indexOf, length2, 33);
            this.mSpans.add(staggeredSpan);
            i++;
            i2 = length2;
        }
    }

    private class StaggeredSpan extends CharacterStyle {
        private int mAlpha;
        private int mShift;

        private StaggeredSpan() {
            this.mShift = 0;
            this.mAlpha = 0;
        }

        public void updateDrawState(TextPaint textPaint) {
            textPaint.baselineShift -= this.mShift;
            textPaint.setAlpha(this.mAlpha);
            GreetingView.this.invalidate();
        }

        /* access modifiers changed from: package-private */
        public void initAnimator(long j, OvershootInterpolator overshootInterpolator, AnimatorSet animatorSet) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setInterpolator(overshootInterpolator);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.google.android.systemui.assist.uihints.$$Lambda$GreetingView$StaggeredSpan$22yc2yUxbFF2pvbaLdFyZnZnGH4 */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    GreetingView.StaggeredSpan.this.lambda$initAnimator$0$GreetingView$StaggeredSpan(valueAnimator);
                }
            });
            ofFloat.setDuration(400L);
            ofFloat.setStartDelay(j);
            animatorSet.play(ofFloat);
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.google.android.systemui.assist.uihints.$$Lambda$GreetingView$StaggeredSpan$EPImxE3HXNOzfVNqeE9slHPh5Ys */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    GreetingView.StaggeredSpan.this.lambda$initAnimator$1$GreetingView$StaggeredSpan(valueAnimator);
                }
            });
            ofFloat2.setDuration(100L);
            ofFloat2.setStartDelay(j);
            animatorSet.play(ofFloat2);
        }

        public /* synthetic */ void lambda$initAnimator$0$GreetingView$StaggeredSpan(ValueAnimator valueAnimator) {
            this.mShift = (int) (((float) GreetingView.this.START_DELTA) * ((Float) valueAnimator.getAnimatedValue()).floatValue());
        }

        public /* synthetic */ void lambda$initAnimator$1$GreetingView$StaggeredSpan(ValueAnimator valueAnimator) {
            this.mAlpha = (int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * GreetingView.this.mMaxAlpha);
        }
    }
}

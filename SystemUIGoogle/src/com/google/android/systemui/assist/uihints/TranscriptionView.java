package com.google.android.systemui.assist.uihints;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.text.SpannableStringBuilder;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.TextView;
import androidx.core.math.MathUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.assist.PhenotypeHelper;
import com.google.android.systemui.assist.uihints.StringUtils;
import com.google.android.systemui.assist.uihints.TranscriptionController;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TranscriptionView extends TextView implements TranscriptionController.TranscriptionSpaceView {
    private static final PathInterpolator INTERPOLATOR_SCROLL;
    private final float BUMPER_DISTANCE_END_PX;
    private final float BUMPER_DISTANCE_START_PX;
    private final float FADE_DISTANCE_END_PX;
    private final float FADE_DISTANCE_START_PX;
    private final int TEXT_COLOR_DARK;
    private final int TEXT_COLOR_LIGHT;
    private boolean mCardVisible;
    private int mDisplayWidthPx;
    private boolean mHasDarkBackground;
    private SettableFuture<Void> mHideFuture;
    private Matrix mMatrix;
    private PhenotypeHelper mPhenotypeHelper;
    private int mRequestedTextColor;
    private float[] mStops;
    private ValueAnimator mTranscriptionAnimation;
    private TranscriptionAnimator mTranscriptionAnimator;
    private SpannableStringBuilder mTranscriptionBuilder;
    private AnimatorSet mVisibilityAnimators;

    static {
        INTERPOLATOR_SCROLL = new PathInterpolator(0.17f, 0.17f, 0.67f, 1.0f);
    }

    public TranscriptionView(Context context) {
        this(context, null);
    }

    public TranscriptionView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TranscriptionView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public TranscriptionView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        mTranscriptionBuilder = new SpannableStringBuilder();
        mVisibilityAnimators = new AnimatorSet();
        mHideFuture = null;
        mHasDarkBackground = false;
        mCardVisible = false;
        mRequestedTextColor = 0;
        mMatrix = new Matrix();
        mDisplayWidthPx = 0;
        mTranscriptionAnimator = new TranscriptionAnimator();
        initializePhenotypeHelper(new PhenotypeHelper());
        BUMPER_DISTANCE_START_PX = context.getResources().getDimension(R.dimen.zerostate_icon_left_margin) + context.getResources().getDimension(R.dimen.zerostate_icon_tap_padding);
        BUMPER_DISTANCE_END_PX = context.getResources().getDimension(R.dimen.keyboard_icon_right_margin) + context.getResources().getDimension(R.dimen.keyboard_icon_tap_padding);
        FADE_DISTANCE_START_PX = context.getResources().getDimension(R.dimen.zerostate_icon_size);
        FADE_DISTANCE_END_PX = context.getResources().getDimension(R.dimen.keyboard_icon_size) / 2.0f;
        TEXT_COLOR_DARK = context.getResources().getColor(R.color.transcription_text_dark);
        TEXT_COLOR_LIGHT = context.getResources().getColor(R.color.transcription_text_light);
        updateDisplayWidth();
        setHasDarkBackground(!mHasDarkBackground);
    }

    @VisibleForTesting
    static float interpolate(long j, long j2, float f) {
        return (((float) (j2 - j)) * f) + ((float) j);
    }

    @VisibleForTesting
    long getAdaptiveDuration(float f, float f2) {
        return Math.min(getDurationMaxMs(), Math.max(getDurationMinMs(), (long) (f * interpolate(getDurationRegularMs(), getDurationFastMs(), f / f2))));
    }

    @VisibleForTesting
    void initializePhenotypeHelper(PhenotypeHelper phenotypeHelper) {
        mPhenotypeHelper = phenotypeHelper;
    }

    private void updateDisplayWidth() {
        mDisplayWidthPx = DisplayUtils.getRotatedWidth(mContext);
        float f = BUMPER_DISTANCE_START_PX;
        int i = mDisplayWidthPx;
        mStops = new float[]{f / ((float) i), (f + FADE_DISTANCE_START_PX) / ((float) i), ((((float) i) - FADE_DISTANCE_END_PX) - BUMPER_DISTANCE_END_PX) / ((float) i), 1.0f};
        updateColor();
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        String string = mTranscriptionBuilder.toString();
        resetTranscription();
        setTranscription(string);
    }

    public ListenableFuture<Void> hide(boolean z) {
        SettableFuture<Void> settableFuture = mHideFuture;
        if (settableFuture != null && !settableFuture.isDone()) {
            return mHideFuture;
        }
        mHideFuture = SettableFuture.create();
        Runnable r0 = () -> {
            setVisibility(8);
            setAlpha(0.0f);
            resetTranscription();
            mVisibilityAnimators = null;
            mHideFuture.set(null);
        };
        if (!z) {
            if (mVisibilityAnimators != null) {
                mVisibilityAnimators.end();
            } else {
                r0.run();
            }
            return Futures.immediateFuture(null);
        }
        mVisibilityAnimators = new AnimatorSet();
        mVisibilityAnimators.play(ObjectAnimator.ofFloat(this, View.ALPHA, getAlpha(), 0.0f).setDuration(400));
        if (!mCardVisible) {
            mVisibilityAnimators.play(ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, getTranslationY(), (float) (getHeight() * -1)).setDuration(700));
        }
        mVisibilityAnimators.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                r0.run();
            }
        });
        mVisibilityAnimators.start();
        return mHideFuture;
    }

    public void setHasDarkBackground(boolean z) {
        if (z != mHasDarkBackground) {
            mHasDarkBackground = z;
            updateColor();
        }
    }

    public void setCardVisible(boolean z) {
        mCardVisible = z;
    }

    public void onFontSizeChanged() {
        setTextSize(0, mContext.getResources().getDimension(R.dimen.transcription_text_size));
    }

    void setTranscription(String str) {
        updateDisplayWidth();
        boolean isAnimationRunning = mTranscriptionAnimation != null && mTranscriptionAnimation.isRunning();
        if (isAnimationRunning) {
            mTranscriptionAnimation.cancel();
        }
        boolean empty = mTranscriptionBuilder.toString().isEmpty();
        StringUtils.StringStabilityInfo calculateStringStabilityInfo = StringUtils.calculateStringStabilityInfo(mTranscriptionBuilder.toString(), str);
        mTranscriptionBuilder.clear();
        mTranscriptionBuilder.append(calculateStringStabilityInfo.stable);
        mTranscriptionBuilder.append(calculateStringStabilityInfo.unstable);
        int width = (int) Math.ceil(getPaint().measureText(mTranscriptionBuilder.toString()));
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = width;
            setLayoutParams(layoutParams);
        }
        updateColor();
        if (!empty && !calculateStringStabilityInfo.stable.isEmpty()) {
            int length = calculateStringStabilityInfo.stable.length();
            TranscriptionSpan transcriptionSpan = null;
            if (isAnimationRunning) {
                if (!calculateStringStabilityInfo.stable.endsWith(" ")) {
                    if (!calculateStringStabilityInfo.unstable.startsWith(" ")) {
                        final String[] split = calculateStringStabilityInfo.stable.split("\\s+");
                        if (split.length > 0) {
                            length = length - split[split.length - 1].length();
                        }
                        final List<TranscriptionSpan> spans = mTranscriptionAnimator.getSpans();
                        if (!spans.isEmpty()) {
                            transcriptionSpan = spans.get(spans.size() - 1);
                        }
                    }
                }
            }
            setUpSpans(length, transcriptionSpan);
            (mTranscriptionAnimation = mTranscriptionAnimator.createAnimator()).start();
        } else {
            setUpSpans(calculateStringStabilityInfo.stable.length() + calculateStringStabilityInfo.unstable.length(), null);
            setX(getFullyVisibleDistance((float) width));
            updateColor();
        }
    }

    private void setUpSpans(int length, TranscriptionSpan transcriptionSpan) {
        mTranscriptionAnimator.clearSpans();
        final String string = mTranscriptionBuilder.toString();
        final String substring = string.substring(length);
        if (substring.length() > 0) {
            final int index = string.indexOf(substring, length);
            length = substring.length();
            if (transcriptionSpan == null) {
                transcriptionSpan = new TranscriptionSpan();
            } else {
                transcriptionSpan = new TranscriptionSpan(transcriptionSpan);
            }
            mTranscriptionBuilder.setSpan(transcriptionSpan, index, length + index, 33);
            mTranscriptionAnimator.addSpan(transcriptionSpan);
        }
        setText(mTranscriptionBuilder, TextView.BufferType.SPANNABLE);
        updateColor();
    }

    void setTranscriptionColor(int i) {
        mRequestedTextColor = i;
        updateColor();
    }

    public void show() {
        if (mVisibilityAnimators != null) {
            mVisibilityAnimators.cancel();
            mVisibilityAnimators = null;
        }
        updateDisplayWidth();
        setAlpha(1.0f);
        setTranslationY(0.0f);
        setVisibility(0);
    }

    private float getFullyVisibleDistance(float f) {
        final float n2 = (float) mDisplayWidthPx;
        final float bumper_DISTANCE_END_PX = BUMPER_DISTANCE_END_PX;
        final float fade_DISTANCE_END_PX = FADE_DISTANCE_END_PX;
        if (f < n2 - (BUMPER_DISTANCE_START_PX + bumper_DISTANCE_END_PX + fade_DISTANCE_END_PX + FADE_DISTANCE_START_PX)) {
            return (mDisplayWidthPx - f) / 2.0f;
        }
        return mDisplayWidthPx - f - fade_DISTANCE_END_PX - bumper_DISTANCE_END_PX;
    }

    public void updateColor() {
        int i = mRequestedTextColor;
        if (i == 0) {
            i = mHasDarkBackground ? TEXT_COLOR_DARK : TEXT_COLOR_LIGHT;
        }
        LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, (float) mDisplayWidthPx, 0.0f, new int[]{0, i, i, 0}, mStops, Shader.TileMode.CLAMP);
        mMatrix.setTranslate(-getTranslationX(), 0.0f);
        linearGradient.setLocalMatrix(mMatrix);
        getPaint().setShader(linearGradient);
        invalidate();
    }

    private void resetTranscription() {
        setTranscription("");
        mTranscriptionAnimator = new TranscriptionAnimator();
    }

    private long getDurationRegularMs() {
        return mPhenotypeHelper.getLong("assist_transcription_duration_per_px_regular", 4);
    }

    private long getDurationFastMs() {
        return mPhenotypeHelper.getLong("assist_transcription_duration_per_px_fast", 3);
    }

    private long getDurationMaxMs() {
        return mPhenotypeHelper.getLong("assist_transcription_max_duration", 400);
    }

    private long getDurationMinMs() {
        return mPhenotypeHelper.getLong("assist_transcription_min_duration", 20);
    }

    private long getFadeInDurationMs() {
        return mPhenotypeHelper.getLong("assist_transcription_fade_in_duration", 50);
    }

    private class TranscriptionAnimator implements ValueAnimator.AnimatorUpdateListener {
        private float mDistance;
        private List<TranscriptionSpan> mSpans;
        private float mStartX;

        private TranscriptionAnimator() {
            mSpans = new ArrayList<>();
        }

        void addSpan(TranscriptionSpan transcriptionSpan) {
            mSpans.add(transcriptionSpan);
        }

        List<TranscriptionSpan> getSpans() {
            return mSpans;
        }

        void clearSpans() {
            mSpans.clear();
        }

        ValueAnimator createAnimator() {
            float measureText = getPaint().measureText(mTranscriptionBuilder.toString());
            mStartX = getX();
            mDistance = getFullyVisibleDistance(measureText) - mStartX;
            updateColor();
            long adaptiveDuration = getAdaptiveDuration(Math.abs(mDistance), (float) mDisplayWidthPx);
            long duration;
            if (measureText > mDisplayWidthPx - getX()) {
                duration = getFadeInDurationMs() + adaptiveDuration;
            } else {
                duration = adaptiveDuration;
            }
            final float n = duration / (float) adaptiveDuration;
            final ValueAnimator setDuration = ValueAnimator.ofFloat(mStartX, mStartX + mDistance * n).setDuration(duration);
            setDuration.setInterpolator(INTERPOLATOR_SCROLL);
            setDuration.addUpdateListener(this);
            return setDuration;
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float floatValue = (Float) valueAnimator.getAnimatedValue();
            if (Math.abs(floatValue - mStartX) < Math.abs(mDistance)) {
                setX(floatValue);
                updateColor();
            }
            mSpans.forEach(span -> span.setCurrentFraction(valueAnimator.getAnimatedFraction()));
            invalidate();
        }
    }

    private class TranscriptionSpan extends ReplacementSpan {
        private float mCurrentFraction = 0.0f;
        private float mStartFraction = 0.0f;

        TranscriptionSpan() {
        }

        TranscriptionSpan(final TranscriptionSpan transcriptionSpan) {
            mStartFraction = MathUtils.clamp(transcriptionSpan.getCurrentFraction(), 0.0f, 1.0f);
        }

        private float getAlpha() {
            if (mStartFraction == 1.0f) {
                return 1.0f;
            }
            return MathUtils.clamp((1.0f - mStartFraction) / 1.0f * mCurrentFraction + mStartFraction, 0.0f, 1.0f);
        }

        public void draw(Canvas canvas, CharSequence charSequence, int n, int n2, float n3, int n4, int n5, int n6, Paint paint) {
            paint.setAlpha((int)Math.ceil(getAlpha() * 255.0f));
            canvas.drawText(charSequence, n, n2, n3, (float)n5, paint);
        }

        float getCurrentFraction() {
            return mCurrentFraction;
        }

        public int getSize(Paint paint, CharSequence charSequence, int n, int n2, Paint.FontMetricsInt fontMetricsInt) {
            return (int)Math.ceil(getPaint().measureText(charSequence, 0, charSequence.length()));
        }

        void setCurrentFraction(float currentFraction) {
            mCurrentFraction = currentFraction;
        }
    }
}

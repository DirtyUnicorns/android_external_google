package com.google.android.systemui.assist.uihints.edgelights.mode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.view.animation.PathInterpolator;
import com.android.systemui.R;
import com.android.systemui.assist.ui.EdgeLight;
import com.android.systemui.assist.ui.PerimeterPathGuide;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightUpdateListener;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;

public final class HalfListening implements EdgeLightsView.Mode {
    private static final PathInterpolator INTERPOLATOR;
    private AnimatorSet mAnimatorSet;
    private PerimeterPathGuide mGuide;
    private int mLightLengthPx;
    private final EdgeLight[] mLights;
    private final long mTimeoutMs;
    private final long mTimeoutTimestampMs;

    static {
        INTERPOLATOR = new PathInterpolator(0.02f, 0.0f, 0.02f, 1.0f);
    }

    public HalfListening(Context context, long j) {
        mLights = new EdgeLight[] {new EdgeLight(0, 0.0f, 0.0f), new EdgeLight(0, 0.0f, 0.0f),
                new EdgeLight(0, 0.0f, 0.0f), new EdgeLight(0, 0.0f, 0.0f)};
        mLightLengthPx = context.getResources().getDimensionPixelSize(R.dimen.navigation_home_handle_width) / mLights.length;
        mTimeoutMs = j;
        mTimeoutTimestampMs = mTimeoutMs > 0 ? SystemClock.uptimeMillis() + mTimeoutMs : -1;
    }

    @Override
    public int getSubType() {
        return 2;
    }

    @Override
    public void onNewModeRequest(final EdgeLightsView edgeLightsView, final EdgeLightsView.Mode mode) {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        if (mode instanceof Gone) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setDuration(300L);
            ofFloat.setInterpolator(INTERPOLATOR);
            ofFloat.addUpdateListener(new EdgeLightUpdateListener(getFinalLights(), getInitialLights(), mLights, edgeLightsView));
            ofFloat.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (edgeLightsView.getMode() == HalfListening.this) {
                        edgeLightsView.commitModeTransition(mode);
                    }
                }
            });
            ofFloat.start();
            return;
        }
        edgeLightsView.commitModeTransition(mode);
    }

    @Override
    public void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        mGuide = perimeterPathGuide;
        if (!(mode instanceof FulfillBottom)) {
            edgeLightsView.setVisibility(0);
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            long j = mTimeoutMs <= 0 ? 300 : mTimeoutMs;
            ofFloat.setDuration(j);
            ofFloat.setInterpolator(INTERPOLATOR);
            ofFloat.addUpdateListener(new EdgeLightUpdateListener(getInitialLights(), getFinalLights(), mLights, edgeLightsView));
            animatorSet.play(ofFloat);
            animatorSet.start();
            mAnimatorSet = animatorSet;
        }
    }

    private EdgeLight[] getInitialLights() {
        EdgeLight[] copy = EdgeLight.copy(mLights);
        float regionWidth = mTimeoutMs <= 0 ? 0.0f : mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM) / ((float) copy.length);
        int i = -(mLights.length / 2);
        for (EdgeLight edgeLight : copy) {
            edgeLight.setOffset(mGuide.getRegionCenter(PerimeterPathGuide.Region.BOTTOM) + (((float) i) * regionWidth));
            i++;
            edgeLight.setLength(regionWidth);
        }
        return copy;
    }

    private EdgeLight[] getFinalLights() {
        EdgeLight[] copy = EdgeLight.copy(mLights);
        float perimeterPx = ((float) mLightLengthPx) / mGuide.getPerimeterPx();
        for (int i = 0; i < mLights.length; i++) {
            copy[i].setLength(perimeterPx);
            copy[i].setOffset(getLightOffset(i, mGuide));
        }
        return copy;
    }

    private float getLightOffset(int i, PerimeterPathGuide perimeterPathGuide) {
        return (((float) ((i - (mLights.length / 2)) * mLightLengthPx)) / perimeterPathGuide.getPerimeterPx()) + mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
    }

    @Override
    public void onAudioLevelUpdate(float f, float f2) {
    }

    @Override
    public void onConfigurationChanged() {
    }
}

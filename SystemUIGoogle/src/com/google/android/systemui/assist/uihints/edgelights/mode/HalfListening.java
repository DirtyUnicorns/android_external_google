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
    private static final PathInterpolator INTERPOLATOR = new PathInterpolator(0.02f, 0.0f, 0.02f, 1.0f);
    private AnimatorSet mAnimatorSet;
    private PerimeterPathGuide mGuide;
    private int mLightLengthPx;
    private final EdgeLight[] mLights = {new EdgeLight(0, 0.0f, 0.0f), new EdgeLight(0, 0.0f, 0.0f), new EdgeLight(0, 0.0f, 0.0f), new EdgeLight(0, 0.0f, 0.0f)};
    private final long mTimeoutMs;
    private final long mTimeoutTimestampMs;

    public int getSubType() {
        return 2;
    }

    public HalfListening(Context context, long j) {
        this.mLightLengthPx = context.getResources().getDimensionPixelSize(R.dimen.navigation_home_handle_width) / this.mLights.length;
        this.mTimeoutMs = j;
        this.mTimeoutTimestampMs = this.mTimeoutMs > 0 ? SystemClock.uptimeMillis() + this.mTimeoutMs : -1;
    }

    public void onNewModeRequest(final EdgeLightsView edgeLightsView, final EdgeLightsView.Mode mode) {
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        if (mode instanceof Gone) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setDuration(300L);
            ofFloat.setInterpolator(INTERPOLATOR);
            ofFloat.addUpdateListener(new EdgeLightUpdateListener(getFinalLights(), getInitialLights(), this.mLights, edgeLightsView));
            ofFloat.addListener(new AnimatorListenerAdapter() {
                /* class com.google.android.systemui.assist.uihints.edgelights.mode.HalfListening.C15771 */

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

    public void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        this.mGuide = perimeterPathGuide;
        if (!(mode instanceof FulfillBottom)) {
            edgeLightsView.setVisibility(0);
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            long j = this.mTimeoutMs;
            if (j <= 0) {
                j = 300;
            }
            ofFloat.setDuration(j);
            ofFloat.setInterpolator(INTERPOLATOR);
            ofFloat.addUpdateListener(new EdgeLightUpdateListener(getInitialLights(), getFinalLights(), this.mLights, edgeLightsView));
            animatorSet.play(ofFloat);
            animatorSet.start();
            this.mAnimatorSet = animatorSet;
        }
    }

    private EdgeLight[] getInitialLights() {
        EdgeLight[] copy = EdgeLight.copy(this.mLights);
        float regionWidth = this.mTimeoutMs <= 0 ? 0.0f : this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM) / ((float) copy.length);
        int i = -(this.mLights.length / 2);
        for (EdgeLight edgeLight : copy) {
            edgeLight.setOffset(this.mGuide.getRegionCenter(PerimeterPathGuide.Region.BOTTOM) + (((float) i) * regionWidth));
            i++;
            edgeLight.setLength(regionWidth);
        }
        return copy;
    }

    private EdgeLight[] getFinalLights() {
        EdgeLight[] copy = EdgeLight.copy(this.mLights);
        float perimeterPx = ((float) this.mLightLengthPx) / this.mGuide.getPerimeterPx();
        for (int i = 0; i < this.mLights.length; i++) {
            copy[i].setLength(perimeterPx);
            copy[i].setOffset(getLightOffset(i, this.mGuide));
        }
        return copy;
    }

    private float getLightOffset(int i, PerimeterPathGuide perimeterPathGuide) {
        return (((float) ((i - (this.mLights.length / 2)) * this.mLightLengthPx)) / perimeterPathGuide.getPerimeterPx()) + this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
    }

    @Override
    public void onAudioLevelUpdate(float f, float f2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConfigurationChanged() {
        // TODO Auto-generated method stub

    }
}

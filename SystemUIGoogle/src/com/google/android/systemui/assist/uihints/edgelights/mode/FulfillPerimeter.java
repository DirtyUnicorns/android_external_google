package com.google.android.systemui.assist.uihints.edgelights.mode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.animation.PathInterpolator;
import com.android.systemui.R;
import com.android.systemui.assist.ui.EdgeLight;
import com.android.systemui.assist.ui.PerimeterPathGuide;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;

public final class FulfillPerimeter implements EdgeLightsView.Mode {
    private static final PathInterpolator FULFILL_PERIMETER_INTERPOLATOR;
    private final EdgeLight mBlueLight;
    private boolean mDisappearing;
    private final EdgeLight mGreenLight;
    private final EdgeLight[] mLights;
    private EdgeLightsView.Mode mNextMode;
    private final EdgeLight mRedLight;
    private final EdgeLight mYellowLight;

    static {
        FULFILL_PERIMETER_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
    }

    public FulfillPerimeter(Context context) {
        mDisappearing = false;
        mBlueLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_blue, null), 0.0f, 0.0f);
        mRedLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_red, null), 0.0f, 0.0f);
        mYellowLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_yellow, null), 0.0f, 0.0f);
        mGreenLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_green, null), 0.0f, 0.0f);
        mLights = new EdgeLight[]{mBlueLight, mRedLight, mGreenLight, mYellowLight};
    }

    @Override
    public int getSubType() {
        return 4;
    }

    @Override
    public void onNewModeRequest(EdgeLightsView edgeLightsView, EdgeLightsView.Mode mode) {
        mNextMode = mode;
    }

    @Override
    public void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        final EdgeLightsView edgeLightsView2 = edgeLightsView;
        PerimeterPathGuide perimeterPathGuide2 = perimeterPathGuide;
        edgeLightsView2.setVisibility(0);
        final AnimatorSet animatorSet = new AnimatorSet();
        EdgeLight[] edgeLightArr = mLights;
        int length = edgeLightArr.length;
        int i = 0;
        while (i < length) {
            EdgeLight edgeLight = edgeLightArr[i];
            boolean z2 = edgeLight == mBlueLight || edgeLight == mRedLight;
            boolean z3 = edgeLight == mRedLight || edgeLight == mYellowLight;
            float regionCenter = perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.BOTTOM);
            float makeClockwise = (z2 ? PerimeterPathGuide.makeClockwise(perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.TOP)) : regionCenter) - regionCenter;
            float regionCenter2 = perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.TOP) - perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.BOTTOM);
            float f = regionCenter2 - 0.0f;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setStartDelay(z3 ? 100 : 0);
            ofFloat.setDuration(433L);
            ofFloat.setInterpolator(FULFILL_PERIMETER_INTERPOLATOR);
            // FIXME: Redundancy
            ofFloat.addUpdateListener(valueAnim -> lambdaStartFp0(edgeLight, makeClockwise, regionCenter, f, 0.0f, edgeLightsView, valueAnim));
            if (!z3) {
                animatorSet.play(ofFloat);
            } else {
                float interpolation = ofFloat.getInterpolator().getInterpolation(100.0f / ((float) ofFloat.getDuration())) * regionCenter2;
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
                ofFloat2.setStartDelay(ofFloat.getStartDelay() + 100);
                ofFloat2.setDuration(733L);
                ofFloat2.setInterpolator(FULFILL_PERIMETER_INTERPOLATOR);
                ofFloat2.addUpdateListener(valueAnim -> lambdaStartFp1(edgeLight, interpolation, perimeterPathGuide, edgeLightsView, valueAnim));
                animatorSet.play(ofFloat);
                animatorSet.play(ofFloat2);
            }
            i++;
            perimeterPathGuide2 = perimeterPathGuide;
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (mNextMode == null) {
                    mDisappearing = false;
                    animatorSet.start();
                } else {
                    // FIXME: Wrong approach on handler(s)
                    new Handler().postDelayed(() -> edgeLightsView2.commitModeTransition(mNextMode), 500);
                }
            }
        });
        animatorSet.start();
    }

    // FIXME
    private /* synthetic */ void lambdaStartFp0(EdgeLight edgeLight, float f, float f2, float f3, float f4,
            EdgeLightsView edgeLightsView, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        edgeLight.setOffset((f * animatedFraction) + f2);
        if (!mDisappearing) {
            edgeLight.setLength((f3 * animatedFraction) + f4);
        }
        edgeLightsView.setAssistLights(mLights);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.max(float, float):float}
     arg types: [float, int]
     candidates:
      ClspMth{java.lang.Math.max(double, double):double}
      ClspMth{java.lang.Math.max(int, int):int}
      ClspMth{java.lang.Math.max(long, long):long}
      ClspMth{java.lang.Math.max(float, float):float} */
    // FIXME
    private /* synthetic */ void lambdaStartFp1(EdgeLight edgeLight, float f, PerimeterPathGuide perimeterPathGuide,
            EdgeLightsView edgeLightsView, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (animatedFraction != 0.0f) {
            mDisappearing = true;
            if (edgeLight == mRedLight) {
                mRedLight.setLength(Math.max(((0.0f - f) * animatedFraction) + f, 0.0f));
                mBlueLight.setLength(Math.abs(mBlueLight.getOffset()) - Math.abs(mRedLight.getOffset()));
            } else {
                if (edgeLight == mYellowLight) {
                    mYellowLight.setOffset((perimeterPathGuide.getRegionCenter(PerimeterPathGuide.Region.BOTTOM) * 2.0f) - (mRedLight.getOffset() + mRedLight.getLength()));
                    mYellowLight.setLength(mRedLight.getLength());
                    mGreenLight.setOffset((perimeterPathGuide.getRegionCenter(PerimeterPathGuide.Region.BOTTOM) * 2.0f) - (mBlueLight.getOffset() + mBlueLight.getLength()));
                    mGreenLight.setLength(mBlueLight.getLength());
                }
            }
            edgeLightsView.setAssistLights(mLights);
        }
    }

    @Override
    public void onAudioLevelUpdate(float f, float f2) {
    }

    @Override
    public void onConfigurationChanged() {
    }
}

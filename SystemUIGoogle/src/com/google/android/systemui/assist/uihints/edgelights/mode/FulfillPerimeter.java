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
import com.google.android.systemui.assist.uihints.edgelights.mode.FulfillPerimeter;

public final class FulfillPerimeter implements EdgeLightsView.Mode {
    private static final PathInterpolator FULFILL_PERIMETER_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
    private final EdgeLight mBlueLight;
    /* access modifiers changed from: private */
    public boolean mDisappearing = false;
    private final EdgeLight mGreenLight;
    private final EdgeLight[] mLights;
    /* access modifiers changed from: private */
    public EdgeLightsView.Mode mNextMode;
    private final EdgeLight mRedLight;
    private final EdgeLight mYellowLight;

    public int getSubType() {
        return 4;
    }

    public FulfillPerimeter(Context context) {
        this.mBlueLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_blue, null), 0.0f, 0.0f);
        this.mRedLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_red, null), 0.0f, 0.0f);
        this.mYellowLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_yellow, null), 0.0f, 0.0f);
        this.mGreenLight = new EdgeLight(context.getResources().getColor(R.color.edge_light_green, null), 0.0f, 0.0f);
        this.mLights = new EdgeLight[]{this.mBlueLight, this.mRedLight, this.mGreenLight, this.mYellowLight};
    }

    public void onNewModeRequest(EdgeLightsView edgeLightsView, EdgeLightsView.Mode mode) {
        this.mNextMode = mode;
    }

    public void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        final EdgeLightsView edgeLightsView2 = edgeLightsView;
        PerimeterPathGuide perimeterPathGuide2 = perimeterPathGuide;
        boolean z = false;
        edgeLightsView2.setVisibility(0);
        final AnimatorSet animatorSet = new AnimatorSet();
        EdgeLight[] edgeLightArr = this.mLights;
        int length = edgeLightArr.length;
        int i = 0;
        while (i < length) {
            EdgeLight edgeLight = edgeLightArr[i];
            boolean z2 = (edgeLight == this.mBlueLight || edgeLight == this.mRedLight) ? true : z;
            boolean z3 = (edgeLight == this.mRedLight || edgeLight == this.mYellowLight) ? true : z;
            float regionCenter = perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.BOTTOM);
            float makeClockwise = (z2 ? PerimeterPathGuide.makeClockwise(perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.TOP)) : regionCenter) - regionCenter;
            float regionCenter2 = perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.TOP) - perimeterPathGuide2.getRegionCenter(PerimeterPathGuide.Region.BOTTOM);
            float f = regionCenter2 - 0.0f;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setStartDelay(z3 ? 100 : 0);
            ofFloat.setDuration(433L);
            ofFloat.setInterpolator(FULFILL_PERIMETER_INTERPOLATOR);
            EdgeLight edgeLight2 = edgeLight;
            // FIXME: Redundancy
            ValueAnimator valueAnimator = ofFloat;
            ofFloat.addUpdateListener(valueAnim -> lambdaStartFp0(edgeLight, makeClockwise, regionCenter, f, 0.0f, edgeLightsView, valueAnim));
            if (!z3) {
                animatorSet.play(valueAnimator);
            } else {
                float interpolation = valueAnimator.getInterpolator().getInterpolation(100.0f / ((float) valueAnimator.getDuration())) * regionCenter2;
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
                ofFloat2.setStartDelay(valueAnimator.getStartDelay() + 100);
                ofFloat2.setDuration(733L);
                ofFloat2.setInterpolator(FULFILL_PERIMETER_INTERPOLATOR);
                ofFloat2.addUpdateListener(valueAnim -> lambdaStartFp1(edgeLight2, interpolation, perimeterPathGuide, edgeLightsView, valueAnim));
                animatorSet.play(valueAnimator);
                animatorSet.play(ofFloat2);
            }
            i++;
            perimeterPathGuide2 = perimeterPathGuide;
            z = false;
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.uihints.edgelights.mode.FulfillPerimeter.C15751 */

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (FulfillPerimeter.this.mNextMode == null) {
                    boolean unused = FulfillPerimeter.this.mDisappearing = false;
                    animatorSet.start();
                } else if (FulfillPerimeter.this.mNextMode != null) {
                    // FIXME: Wrong approach on handler(s)
                    new Handler().postDelayed(() -> edgeLightsView2.commitModeTransition(mNextMode), 500);
                }
            }
        });
        animatorSet.start();
    }

    // FIXME
    public /* synthetic */ void lambdaStartFp0(EdgeLight edgeLight, float f, float f2, float f3, float f4, EdgeLightsView edgeLightsView, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        edgeLight.setOffset((f * animatedFraction) + f2);
        if (!this.mDisappearing) {
            edgeLight.setLength((f3 * animatedFraction) + f4);
        }
        edgeLightsView.setAssistLights(this.mLights);
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
    public /* synthetic */ void lambdaStartFp1(EdgeLight edgeLight, float f, PerimeterPathGuide perimeterPathGuide, EdgeLightsView edgeLightsView, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (animatedFraction != 0.0f) {
            this.mDisappearing = true;
            EdgeLight edgeLight2 = this.mRedLight;
            if (edgeLight == edgeLight2) {
                edgeLight2.setLength(Math.max(((0.0f - f) * animatedFraction) + f, 0.0f));
                EdgeLight edgeLight3 = this.mBlueLight;
                edgeLight3.setLength(Math.abs(edgeLight3.getOffset()) - Math.abs(this.mRedLight.getOffset()));
            } else {
                EdgeLight edgeLight4 = this.mYellowLight;
                if (edgeLight == edgeLight4) {
                    edgeLight4.setOffset((perimeterPathGuide.getRegionCenter(PerimeterPathGuide.Region.BOTTOM) * 2.0f) - (this.mRedLight.getOffset() + this.mRedLight.getLength()));
                    this.mYellowLight.setLength(this.mRedLight.getLength());
                    this.mGreenLight.setOffset((perimeterPathGuide.getRegionCenter(PerimeterPathGuide.Region.BOTTOM) * 2.0f) - (this.mBlueLight.getOffset() + this.mBlueLight.getLength()));
                    this.mGreenLight.setLength(this.mBlueLight.getLength());
                }
            }
            edgeLightsView.setAssistLights(this.mLights);
        }
    }

    @Override
    public void onAudioLevelUpdate(float f, float f2) {
        // TODO: I couldn't find this method anywhere!

    }

    @Override
    public void onConfigurationChanged() {
        // TODO: I couldn't find this method anywhere!

    }
}

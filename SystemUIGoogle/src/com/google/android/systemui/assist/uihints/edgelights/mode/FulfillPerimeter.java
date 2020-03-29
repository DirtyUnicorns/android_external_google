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
            $$Lambda$FulfillPerimeter$MZtUjbRyns2SZEYMcv6IQbgrRY r11 = r0;
            EdgeLight edgeLight2 = edgeLight;
            $$Lambda$FulfillPerimeter$MZtUjbRyns2SZEYMcv6IQbgrRY r0 = new ValueAnimator.AnimatorUpdateListener(edgeLight, makeClockwise, regionCenter, f, 0.0f, edgeLightsView) {
                /* class com.google.android.systemui.assist.uihints.edgelights.mode.$$Lambda$FulfillPerimeter$MZtUjbRyns2SZEYMcv6IQbgrRY */
                private final /* synthetic */ EdgeLight f$1;
                private final /* synthetic */ float f$2;
                private final /* synthetic */ float f$3;
                private final /* synthetic */ float f$4;
                private final /* synthetic */ float f$5;
                private final /* synthetic */ EdgeLightsView f$6;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                }

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    FulfillPerimeter.this.lambda$start$0$FulfillPerimeter(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, valueAnimator);
                }
            };
            ValueAnimator valueAnimator = ofFloat;
            valueAnimator.addUpdateListener(r11);
            if (!z3) {
                animatorSet.play(valueAnimator);
            } else {
                float interpolation = valueAnimator.getInterpolator().getInterpolation(100.0f / ((float) valueAnimator.getDuration())) * regionCenter2;
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
                ofFloat2.setStartDelay(valueAnimator.getStartDelay() + 100);
                ofFloat2.setDuration(733L);
                ofFloat2.setInterpolator(FULFILL_PERIMETER_INTERPOLATOR);
                ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(edgeLight2, interpolation, perimeterPathGuide, edgeLightsView) {
                    /* class com.google.android.systemui.assist.uihints.edgelights.mode.$$Lambda$FulfillPerimeter$4qfpqiVttSOidi4h0dCycMmHzTE */
                    private final /* synthetic */ EdgeLight f$1;
                    private final /* synthetic */ float f$2;
                    private final /* synthetic */ PerimeterPathGuide f$3;
                    private final /* synthetic */ EdgeLightsView f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        FulfillPerimeter.this.lambda$start$1$FulfillPerimeter(this.f$1, this.f$2, this.f$3, this.f$4, valueAnimator);
                    }
                });
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
                    new Handler().postDelayed(new Runnable(edgeLightsView2) {
                        /* class com.google.android.systemui.assist.uihints.edgelights.mode.$$Lambda$FulfillPerimeter$1$uToVp7_HsUUglmWlzavSyXNWCo */
                        private final /* synthetic */ EdgeLightsView f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            FulfillPerimeter.C15751.this.lambda$onAnimationEnd$0$FulfillPerimeter$1(this.f$1);
                        }
                    }, 500);
                }
            }

            public /* synthetic */ void lambda$onAnimationEnd$0$FulfillPerimeter$1(EdgeLightsView edgeLightsView) {
                edgeLightsView.commitModeTransition(FulfillPerimeter.this.mNextMode);
            }
        });
        animatorSet.start();
    }

    public /* synthetic */ void lambda$start$0$FulfillPerimeter(EdgeLight edgeLight, float f, float f2, float f3, float f4, EdgeLightsView edgeLightsView, ValueAnimator valueAnimator) {
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
    public /* synthetic */ void lambda$start$1$FulfillPerimeter(EdgeLight edgeLight, float f, PerimeterPathGuide perimeterPathGuide, EdgeLightsView edgeLightsView, ValueAnimator valueAnimator) {
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
}

package com.google.android.systemui.assist.uihints.edgelights.mode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.android.systemui.R;
import com.android.systemui.assist.p003ui.EdgeLight;
import com.android.systemui.assist.p003ui.PerimeterPathGuide;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightUpdateListener;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public final class FulfillBottom implements EdgeLightsView.Mode {
    private static final LinearInterpolator EXIT_FADE_INTERPOLATOR = new LinearInterpolator();
    private static final PathInterpolator EXIT_TO_CORNER_INTERPOLATOR = new PathInterpolator(0.1f, 0.0f, 0.5f, 1.0f);
    private static final FastOutSlowInInterpolator SWINGING_INTERPOLATOR = new FastOutSlowInInterpolator();
    private final int LEFT_BLUE_DURATION = 1000;
    private final float LEFT_BLUE_END_TO_WIDTH = 0.356f;
    private final float LEFT_BLUE_START_TO_WIDTH = 0.0f;
    private final int LEFT_GREEN_DURATION = 1500;
    private final float LEFT_GREEN_END_TO_WIDTH = 1.0f;
    private final float LEFT_GREEN_START_TO_WIDTH = 0.3f;
    private final int LEFT_RED_DURATION = 1175;
    private final float LEFT_RED_END_TO_WIDTH = 0.154f;
    private final float LEFT_RED_START_TO_WIDTH = 0.028f;
    private final int LEFT_YELLOW_DURATION = 1325;
    private final float LEFT_YELLOW_END_TO_WIDTH = 0.31f;
    private final float LEFT_YELLOW_START_TO_WIDTH = 0.149f;
    private final int RIGHT_BLUE_DURATION = 1500;
    private final float RIGHT_BLUE_END_TO_WIDTH = 0.687f;
    private final float RIGHT_BLUE_START_TO_WIDTH = 0.0f;
    private final int RIGHT_GREEN_DURATION = 1000;
    private final float RIGHT_GREEN_END_TO_WIDTH = 1.0f;
    private final float RIGHT_GREEN_START_TO_WIDTH = 0.9f;
    private final int RIGHT_RED_DURATION = 1325;
    private final float RIGHT_RED_END_TO_WIDTH = 0.867f;
    private final float RIGHT_RED_START_TO_WIDTH = 0.687f;
    private final int RIGHT_YELLOW_DURATION = 1175;
    private final float RIGHT_YELLOW_END_TO_WIDTH = 0.947f;
    private final float RIGHT_YELLOW_START_TO_WIDTH = 0.857f;
    private EdgeLight mBlueLight;
    private Context mContext;
    /* access modifiers changed from: private */
    public EdgeLightsView mEdgeLightsView = null;
    private AnimatorSet mExitAnimations;
    private EdgeLight mGreenLight;
    private PerimeterPathGuide mGuide = null;
    private final boolean mIsListening;
    /* access modifiers changed from: private */
    public final ArrayList<ValueAnimator> mLightAnimators = new ArrayList<>();
    private ArrayList<EdgeLight> mLights;
    private EdgeLight[] mLightsArray;
    /* access modifiers changed from: private */
    public EdgeLightsView.Mode mNextMode = null;
    private final Random mRandom = new Random();
    private EdgeLight mRedLight;
    private boolean mSwingingToLeft = false;
    private EdgeLight mYellowLight;

    public int getSubType() {
        return 3;
    }

    public FulfillBottom(Context context, boolean z) {
        this.mContext = context;
        this.mIsListening = z;
    }

    public boolean isListening() {
        return this.mIsListening;
    }

    public void onNewModeRequest(EdgeLightsView edgeLightsView, EdgeLightsView.Mode mode) {
        this.mNextMode = mode;
        cancelSwingingAnimations();
        Log.v("FulfillBottom", "got mode " + mode.getClass().getSimpleName());
        if (!(mode instanceof Gone) && !(mode instanceof HalfListening)) {
            AnimatorSet animatorSet = this.mExitAnimations;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            this.mEdgeLightsView.commitModeTransition(this.mNextMode);
        } else if (this.mExitAnimations == null) {
            animateExit();
        }
    }

    private void cancelSwingingAnimations() {
        while (!this.mLightAnimators.isEmpty()) {
            this.mLightAnimators.get(0).cancel();
        }
    }

    public void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        this.mEdgeLightsView = edgeLightsView;
        this.mGuide = perimeterPathGuide;
        edgeLightsView.setVisibility(0);
        EdgeLight[] assistLights = edgeLightsView.getAssistLights();
        if (((mode instanceof FullListening) || (mode instanceof FulfillBottom)) && assistLights.length == 4) {
            this.mBlueLight = assistLights[0];
            this.mRedLight = assistLights[1];
            this.mYellowLight = assistLights[2];
            this.mGreenLight = assistLights[3];
        } else {
            float regionWidth = this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM) * 0.25f;
            this.mBlueLight = new EdgeLight(this.mContext.getResources().getColor(R.color.edge_light_blue, null), 0.0f, regionWidth);
            this.mRedLight = new EdgeLight(this.mContext.getResources().getColor(R.color.edge_light_red, null), regionWidth, regionWidth);
            this.mYellowLight = new EdgeLight(this.mContext.getResources().getColor(R.color.edge_light_yellow, null), 2.0f * regionWidth, regionWidth);
            this.mGreenLight = new EdgeLight(this.mContext.getResources().getColor(R.color.edge_light_green, null), 3.0f * regionWidth, regionWidth);
        }
        this.mLights = new ArrayList<>();
        this.mLights.addAll(Arrays.asList(this.mBlueLight, this.mRedLight, this.mYellowLight, this.mGreenLight));
        this.mLightsArray = new EdgeLight[4];
        this.mLightsArray[0] = this.mLights.get(0);
        this.mLightsArray[1] = this.mLights.get(1);
        this.mLightsArray[2] = this.mLights.get(2);
        this.mLightsArray[3] = this.mLights.get(3);
        this.mSwingingToLeft = mode instanceof FulfillBottom ? ((FulfillBottom) mode).swingingToLeft() : this.mRandom.nextBoolean();
        float length = 1.0f - (this.mBlueLight.getLength() / (getEnd(this.mBlueLight, !this.mSwingingToLeft) * this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM)));
        float f = 0.0f;
        while (true) {
            if (f >= 1.0f) {
                break;
            } else if (SWINGING_INTERPOLATOR.getInterpolation(f) >= length) {
                length = f;
                break;
            } else {
                f += 0.01f;
            }
        }
        if (length > 1.0f) {
            length = 0.0f;
        }
        animate(length);
    }

    public void onConfigurationChanged() {
        if (this.mNextMode == null) {
            start(this.mEdgeLightsView, this.mGuide, this);
            return;
        }
        AnimatorSet animatorSet = this.mExitAnimations;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.mExitAnimations = null;
        }
        onNewModeRequest(this.mEdgeLightsView, this.mNextMode);
    }

    private void animate(float f) {
        Iterator it = new ArrayList(this.mLightAnimators).iterator();
        while (it.hasNext()) {
            ((ValueAnimator) it.next()).cancel();
        }
        this.mLightAnimators.clear();
        animateLight(this.mBlueLight, this.mSwingingToLeft, f);
        animateLight(this.mRedLight, this.mSwingingToLeft, f);
        animateLight(this.mYellowLight, this.mSwingingToLeft, f);
        animateLight(this.mGreenLight, this.mSwingingToLeft, f);
        this.mSwingingToLeft = !this.mSwingingToLeft;
    }

    /* access modifiers changed from: private */
    public void animateLight(EdgeLight edgeLight, boolean z, float f) {
        EdgeLight edgeLight2;
        final EdgeLight edgeLight3 = edgeLight;
        final boolean z2 = z;
        float regionWidth = this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
        float start = getStart(edgeLight3, !z2) * regionWidth;
        float end = (getEnd(edgeLight3, !z2) * regionWidth) - start;
        float start2 = getStart(edgeLight, z) * regionWidth;
        float end2 = (getEnd(edgeLight, z) * regionWidth) - start2;
        final ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        if (f > 0.0f) {
            start = edgeLight.getOffset();
            end = edgeLight.getLength();
        }
        float f2 = start;
        float f3 = end;
        float f4 = end2 - f3;
        float f5 = start2 - f2;
        if (this.mLights.indexOf(edgeLight3) == 0) {
            edgeLight2 = null;
        } else {
            ArrayList<EdgeLight> arrayList = this.mLights;
            edgeLight2 = arrayList.get(arrayList.indexOf(edgeLight3) - 1);
        }
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(f5, f2, edgeLight, edgeLight2, f4, f3, f, regionWidth) {
            /* class com.google.android.systemui.assist.uihints.edgelights.mode.$$Lambda$FulfillBottom$fp8ggQW3JR0UJvZuTM3dxnC8dqc */
            private final /* synthetic */ float f$1;
            private final /* synthetic */ float f$2;
            private final /* synthetic */ EdgeLight f$3;
            private final /* synthetic */ EdgeLight f$4;
            private final /* synthetic */ float f$5;
            private final /* synthetic */ float f$6;
            private final /* synthetic */ float f$7;
            private final /* synthetic */ float f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                FulfillBottom.this.lambda$animateLight$0$FulfillBottom(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, valueAnimator);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.uihints.edgelights.mode.FulfillBottom.C15721 */
            private boolean mCancelled = false;

            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                FulfillBottom.this.mLightAnimators.remove(ofFloat);
                if (!this.mCancelled) {
                    FulfillBottom.this.animateLight(edgeLight3, !z2, 0.0f);
                }
            }
        });
        ofFloat.setDuration((long) (((float) getDuration(edgeLight, z)) * (1.0f - f)));
        ofFloat.setInterpolator(SWINGING_INTERPOLATOR);
        this.mLightAnimators.add(ofFloat);
        ofFloat.start();
    }

    public /* synthetic */ void lambda$animateLight$0$FulfillBottom(float f, float f2, EdgeLight edgeLight, EdgeLight edgeLight2, float f3, float f4, float f5, float f6, ValueAnimator valueAnimator) {
        float animatedFraction = (f * valueAnimator.getAnimatedFraction()) + f2;
        if (edgeLight2 != null) {
            animatedFraction = Math.min(animatedFraction, edgeLight2.getOffset() + edgeLight2.getLength());
        }
        edgeLight.setOffset(animatedFraction);
        edgeLight.setLength((f3 * valueAnimator.getAnimatedFraction()) + f4);
        if (f5 > 0.0f && edgeLight == this.mGreenLight) {
            edgeLight.setLength(f6 - edgeLight.getOffset());
        }
        this.mEdgeLightsView.setAssistLights(this.mLightsArray);
    }

    private boolean swingingToLeft() {
        return this.mSwingingToLeft;
    }

    private ValueAnimator createToCornersAnimator() {
        EdgeLight[] copy = EdgeLight.copy(this.mLightsArray);
        EdgeLight[] copy2 = EdgeLight.copy(this.mLightsArray);
        float regionWidth = this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM_LEFT) * 0.6f;
        this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM_LEFT);
        float f = -regionWidth;
        float regionWidth2 = this.mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
        copy2[0].setOffset(f);
        copy2[0].setLength(0.0f);
        copy2[1].setOffset(f);
        copy2[1].setLength(0.0f);
        float f2 = regionWidth2 + regionWidth;
        copy2[2].setOffset(f2);
        copy2[2].setLength(0.0f);
        copy2[3].setOffset(f2);
        copy2[3].setLength(0.0f);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setInterpolator(EXIT_TO_CORNER_INTERPOLATOR);
        ofFloat.setDuration(350L);
        ofFloat.addUpdateListener(new EdgeLightUpdateListener(copy, copy2, this.mLightsArray, this.mEdgeLightsView));
        return ofFloat;
    }

    private void animateExit() {
        ValueAnimator createToCornersAnimator = createToCornersAnimator();
        ValueAnimator createFadeOutAnimator = createFadeOutAnimator();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(createToCornersAnimator);
        animatorSet.play(createFadeOutAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.uihints.edgelights.mode.FulfillBottom.C15732 */
            private boolean mCancelled = false;

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                FulfillBottom.this.mEdgeLightsView.setVisibility(8);
                if (FulfillBottom.this.mNextMode != null && !this.mCancelled) {
                    FulfillBottom.this.mEdgeLightsView.commitModeTransition(FulfillBottom.this.mNextMode);
                }
            }

            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                this.mCancelled = true;
            }
        });
        this.mExitAnimations = animatorSet;
        animatorSet.start();
    }

    private ValueAnimator createFadeOutAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat.setInterpolator(EXIT_FADE_INTERPOLATOR);
        ofFloat.setDuration(350L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.google.android.systemui.assist.uihints.edgelights.mode.$$Lambda$FulfillBottom$wORCirM__9ie4nYAWvSBrm9e_A */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                FulfillBottom.this.lambda$createFadeOutAnimator$1$FulfillBottom(valueAnimator);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.uihints.edgelights.mode.FulfillBottom.C15743 */

            public void onAnimationEnd(Animator animator) {
                FulfillBottom.this.mEdgeLightsView.setAssistLights(new EdgeLight[0]);
                FulfillBottom.this.mEdgeLightsView.setAlpha(1.0f);
            }
        });
        return ofFloat;
    }

    public /* synthetic */ void lambda$createFadeOutAnimator$1$FulfillBottom(ValueAnimator valueAnimator) {
        this.mEdgeLightsView.setAlpha(1.0f - valueAnimator.getAnimatedFraction());
    }

    private float getStart(EdgeLight edgeLight, boolean z) {
        if (edgeLight == this.mBlueLight) {
            return 0.0f;
        }
        if (edgeLight == this.mRedLight) {
            return z ? 0.028f : 0.687f;
        }
        if (edgeLight == this.mYellowLight) {
            return z ? 0.149f : 0.857f;
        }
        if (edgeLight == this.mGreenLight) {
            return z ? 0.3f : 0.9f;
        }
        Log.e("FulfillBottom", "getStart: light not recognized");
        return 0.0f;
    }

    private float getEnd(EdgeLight edgeLight, boolean z) {
        if (edgeLight == this.mBlueLight) {
            return z ? 0.356f : 0.687f;
        }
        if (edgeLight == this.mRedLight) {
            return z ? 0.154f : 0.867f;
        }
        if (edgeLight == this.mYellowLight) {
            return z ? 0.31f : 0.947f;
        }
        if (edgeLight == this.mGreenLight) {
            return 1.0f;
        }
        Log.e("FulfillBottom", "getEnd: light not recognized");
        if (z) {
            return 0.356f;
        }
        return 0.687f;
    }

    private long getDuration(EdgeLight edgeLight, boolean z) {
        if (edgeLight == this.mBlueLight) {
            return z ? 1000 : 1500;
        }
        if (edgeLight == this.mRedLight) {
            return z ? 1175 : 1325;
        }
        if (edgeLight == this.mYellowLight) {
            if (z) {
                return 1325;
            }
            return 1175;
        } else if (edgeLight != this.mGreenLight) {
            Log.e("FulfillBottom", "getDuration: light not recognized");
            if (z) {
                return 1000;
            }
            return 1500;
        } else if (z) {
            return 1500;
        } else {
            return 1000;
        }
    }
}

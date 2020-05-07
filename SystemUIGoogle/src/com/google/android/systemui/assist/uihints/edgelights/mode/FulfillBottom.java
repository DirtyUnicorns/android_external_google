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
import com.android.systemui.assist.ui.EdgeLight;
import com.android.systemui.assist.ui.PerimeterPathGuide;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightUpdateListener;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public final class FulfillBottom implements EdgeLightsView.Mode {
    private static final LinearInterpolator EXIT_FADE_INTERPOLATOR;
    private static final PathInterpolator EXIT_TO_CORNER_INTERPOLATOR;
    private static final FastOutSlowInInterpolator SWINGING_INTERPOLATOR;
    private final int LEFT_BLUE_DURATION;
    private final float LEFT_BLUE_END_TO_WIDTH;
    private final float LEFT_BLUE_START_TO_WIDTH;
    private final int LEFT_GREEN_DURATION;
    private final float LEFT_GREEN_END_TO_WIDTH;
    private final float LEFT_GREEN_START_TO_WIDTH;
    private final int LEFT_RED_DURATION;
    private final float LEFT_RED_END_TO_WIDTH;
    private final float LEFT_RED_START_TO_WIDTH;
    private final int LEFT_YELLOW_DURATION;
    private final float LEFT_YELLOW_END_TO_WIDTH;
    private final float LEFT_YELLOW_START_TO_WIDTH;
    private final int RIGHT_BLUE_DURATION;
    private final float RIGHT_BLUE_END_TO_WIDTH;
    private final float RIGHT_BLUE_START_TO_WIDTH;
    private final int RIGHT_GREEN_DURATION;
    private final float RIGHT_GREEN_END_TO_WIDTH;
    private final float RIGHT_GREEN_START_TO_WIDTH;
    private final int RIGHT_RED_DURATION;
    private final float RIGHT_RED_END_TO_WIDTH;
    private final float RIGHT_RED_START_TO_WIDTH;
    private final int RIGHT_YELLOW_DURATION;
    private final float RIGHT_YELLOW_END_TO_WIDTH;
    private final float RIGHT_YELLOW_START_TO_WIDTH;
    private EdgeLight mBlueLight;
    private Context mContext;
    private EdgeLightsView mEdgeLightsView;
    private AnimatorSet mExitAnimations;
    private EdgeLight mGreenLight;
    private PerimeterPathGuide mGuide;
    private final boolean mIsListening;
    private final ArrayList<ValueAnimator> mLightAnimators;
    private ArrayList<EdgeLight> mLights;
    private EdgeLight[] mLightsArray;
    private EdgeLightsView.Mode mNextMode;
    private final Random mRandom;
    private EdgeLight mRedLight;
    private boolean mSwingingToLeft;
    private EdgeLight mYellowLight;

    static {
        EXIT_TO_CORNER_INTERPOLATOR = new PathInterpolator(0.1f, 0.0f, 0.5f, 1.0f);
        SWINGING_INTERPOLATOR = new FastOutSlowInInterpolator();
        EXIT_FADE_INTERPOLATOR = new LinearInterpolator();
    }

    public FulfillBottom(Context context, boolean z) {
        LEFT_BLUE_DURATION = 1000;
        LEFT_BLUE_START_TO_WIDTH = 0.0f;
        LEFT_BLUE_END_TO_WIDTH = 0.356f;
        LEFT_RED_DURATION = 1175;
        LEFT_RED_START_TO_WIDTH = 0.028f;
        LEFT_RED_END_TO_WIDTH = 0.154f;
        LEFT_YELLOW_DURATION = 1325;
        LEFT_YELLOW_START_TO_WIDTH = 0.149f;
        LEFT_YELLOW_END_TO_WIDTH = 0.31f;
        LEFT_GREEN_DURATION = 1500;
        LEFT_GREEN_START_TO_WIDTH = 0.3f;
        LEFT_GREEN_END_TO_WIDTH = 1.0f;
        RIGHT_BLUE_DURATION = 1500;
        RIGHT_BLUE_START_TO_WIDTH = 0.0f;
        RIGHT_BLUE_END_TO_WIDTH = 0.687f;
        RIGHT_RED_DURATION = 1325;
        RIGHT_RED_START_TO_WIDTH = 0.687f;
        RIGHT_RED_END_TO_WIDTH = 0.867f;
        RIGHT_YELLOW_DURATION = 1175;
        RIGHT_YELLOW_START_TO_WIDTH = 0.857f;
        RIGHT_YELLOW_END_TO_WIDTH = 0.947f;
        RIGHT_GREEN_DURATION = 1000;
        RIGHT_GREEN_START_TO_WIDTH = 0.9f;
        RIGHT_GREEN_END_TO_WIDTH = 1.0f;
        mEdgeLightsView = null;
        mGuide = null;
        mNextMode = null;
        mSwingingToLeft = false;
        mLightAnimators = new ArrayList<>();
        mRandom = new Random();
        mContext = context;
        mIsListening = z;
    }

    public boolean isListening() {
        return mIsListening;
    }

    @Override
    public int getSubType() {
        return 3;
    }

    @Override
    public void onNewModeRequest(EdgeLightsView edgeLightsView, EdgeLightsView.Mode mode) {
        mNextMode = mode;
        cancelSwingingAnimations();
        Log.v("FulfillBottom", "got mode " + mode.getClass().getSimpleName());
        if (!(mode instanceof Gone) && !(mode instanceof HalfListening)) {
            if (mExitAnimations != null) {
                mExitAnimations.cancel();
            }
            mEdgeLightsView.commitModeTransition(mNextMode);
        } else if (mExitAnimations == null) {
            animateExit();
        }
    }

    private void cancelSwingingAnimations() {
        while (!mLightAnimators.isEmpty()) {
            mLightAnimators.get(0).cancel();
        }
    }

    @Override
    public void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        mEdgeLightsView = edgeLightsView;
        mGuide = perimeterPathGuide;
        edgeLightsView.setVisibility(0);
        EdgeLight[] assistLights = edgeLightsView.getAssistLights();
        if (((mode instanceof FullListening) || (mode instanceof FulfillBottom)) && assistLights.length == 4) {
            mBlueLight = assistLights[0];
            mRedLight = assistLights[1];
            mYellowLight = assistLights[2];
            mGreenLight = assistLights[3];
        } else {
            float regionWidth = mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM) * 0.25f;
            mBlueLight = new EdgeLight(mContext.getResources().getColor(R.color.edge_light_blue, null), 0.0f, regionWidth);
            mRedLight = new EdgeLight(mContext.getResources().getColor(R.color.edge_light_red, null), regionWidth, regionWidth);
            mYellowLight = new EdgeLight(mContext.getResources().getColor(R.color.edge_light_yellow, null), 2.0f * regionWidth, regionWidth);
            mGreenLight = new EdgeLight(mContext.getResources().getColor(R.color.edge_light_green, null), 3.0f * regionWidth, regionWidth);
        }
        mLights = new ArrayList<EdgeLight>();
        mLights.addAll(Arrays.asList(mBlueLight, mRedLight, mYellowLight, mGreenLight));
        mLightsArray = new EdgeLight[4];
        mLightsArray[0] = mLights.get(0);
        mLightsArray[1] = mLights.get(1);
        mLightsArray[2] = mLights.get(2);
        mLightsArray[3] = mLights.get(3);
        mSwingingToLeft = mode instanceof FulfillBottom ? ((FulfillBottom) mode).swingingToLeft() : mRandom.nextBoolean();
        float length = 1.0f - (mBlueLight.getLength() / (getEnd(mBlueLight, !mSwingingToLeft) * mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM)));
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

    @Override
    public void onConfigurationChanged() {
        if (mNextMode == null) {
            start(mEdgeLightsView, mGuide, this);
            return;
        }
        if (mExitAnimations != null) {
            mExitAnimations.cancel();
            mExitAnimations = null;
        }
        onNewModeRequest(mEdgeLightsView, mNextMode);
    }

    private void animate(float f) {
        for (Object o : new ArrayList(mLightAnimators)) {
            ((ValueAnimator) o).cancel();
        }
        mLightAnimators.clear();
        animateLight(mBlueLight, mSwingingToLeft, f);
        animateLight(mRedLight, mSwingingToLeft, f);
        animateLight(mYellowLight, mSwingingToLeft, f);
        animateLight(mGreenLight, mSwingingToLeft, f);
        mSwingingToLeft = !mSwingingToLeft;
    }

    private void animateLight(EdgeLight edgeLight, boolean z, float f) {
        EdgeLight edgeLight2;
        final EdgeLight edgeLight3 = edgeLight;
        final boolean z2 = z;
        float regionWidth = mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
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
        if (mLights.indexOf(edgeLight3) == 0) {
            edgeLight2 = null;
        } else {
            ArrayList<EdgeLight> arrayList = mLights;
            edgeLight2 = arrayList.get(arrayList.indexOf(edgeLight3) - 1);
        }
        ofFloat.addUpdateListener(valueAnimator -> lambdaAnimateLightFb(f5, f2, edgeLight, edgeLight2,
                f4, f3, f, regionWidth, valueAnimator));
        ofFloat.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled = false;
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                mCancelled = true;
            }
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                mLightAnimators.remove(ofFloat);
                if (!mCancelled) {
                    animateLight(edgeLight3, !z2, 0.0f);
                }
            }
        });
        ofFloat.setDuration((long) (((float) getDuration(edgeLight, z)) * (1.0f - f)));
        ofFloat.setInterpolator(SWINGING_INTERPOLATOR);
        mLightAnimators.add(ofFloat);
        ofFloat.start();
    }

    // FIXME: Maybe this thing is too huge to put in a lambda...
    private /* synthetic */ void lambdaAnimateLightFb(float f, float f2, EdgeLight edgeLight, EdgeLight edgeLight2,
            float f3, float f4, float f5, float f6, ValueAnimator valueAnimator) {
        float animatedFraction = (f * valueAnimator.getAnimatedFraction()) + f2;
        if (edgeLight2 != null) {
            animatedFraction = Math.min(animatedFraction, edgeLight2.getOffset() + edgeLight2.getLength());
        }
        edgeLight.setOffset(animatedFraction);
        edgeLight.setLength((f3 * valueAnimator.getAnimatedFraction()) + f4);
        if (f5 > 0.0f && edgeLight == mGreenLight) {
            edgeLight.setLength(f6 - edgeLight.getOffset());
        }
        mEdgeLightsView.setAssistLights(mLightsArray);
    }

    private boolean swingingToLeft() {
        return mSwingingToLeft;
    }

    private ValueAnimator createToCornersAnimator() {
        EdgeLight[] copy = EdgeLight.copy(mLightsArray);
        EdgeLight[] copy2 = EdgeLight.copy(mLightsArray);
        float regionWidth = mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM_LEFT) * 0.6f;
        mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM_LEFT);
        float f = -regionWidth;
        float regionWidth2 = mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
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
        ofFloat.addUpdateListener(new EdgeLightUpdateListener(copy, copy2, mLightsArray, mEdgeLightsView));
        return ofFloat;
    }

    private void animateExit() {
        ValueAnimator createToCornersAnimator = createToCornersAnimator();
        ValueAnimator createFadeOutAnimator = createFadeOutAnimator();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(createToCornersAnimator);
        animatorSet.play(createFadeOutAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled = false;
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                mEdgeLightsView.setVisibility(8);
                if (mNextMode != null && !mCancelled) {
                    mEdgeLightsView.commitModeTransition(mNextMode);
                }
            }
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                mCancelled = true;
            }
        });
        mExitAnimations = animatorSet;
        animatorSet.start();
    }

    private ValueAnimator createFadeOutAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat.setInterpolator(EXIT_FADE_INTERPOLATOR);
        ofFloat.setDuration(350L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                mEdgeLightsView.setAlpha(1.0f - valueAnimator.getAnimatedFraction());
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                mEdgeLightsView.setAssistLights(new EdgeLight[0]);
                mEdgeLightsView.setAlpha(1.0f);
            }
        });
        return ofFloat;
    }

    private float getStart(EdgeLight edgeLight, boolean z) {
        if (edgeLight == mBlueLight) {
            return 0.0f;
        }
        if (edgeLight == mRedLight) {
            return z ? 0.028f : 0.687f;
        }
        if (edgeLight == mYellowLight) {
            return z ? 0.149f : 0.857f;
        }
        if (edgeLight == mGreenLight) {
            return z ? 0.3f : 0.9f;
        }
        Log.e("FulfillBottom", "getStart: light not recognized");
        return 0.0f;
    }

    private float getEnd(EdgeLight edgeLight, boolean z) {
        if (edgeLight == mBlueLight) {
            return z ? 0.356f : 0.687f;
        }
        if (edgeLight == mRedLight) {
            return z ? 0.154f : 0.867f;
        }
        if (edgeLight == mYellowLight) {
            return z ? 0.31f : 0.947f;
        }
        if (edgeLight == mGreenLight) {
            return 1.0f;
        }
        Log.e("FulfillBottom", "getEnd: light not recognized");
        if (z) {
            return 0.356f;
        }
        return 0.687f;
    }

    private long getDuration(EdgeLight edgeLight, boolean z) {
        if (edgeLight == mBlueLight) {
            return z ? 1000 : 1500;
        }
        if (edgeLight == mRedLight) {
            return z ? 1175 : 1325;
        }
        if (edgeLight == mYellowLight) {
            if (z) {
                return 1325;
            }
            return 1175;
        } else if (edgeLight != mGreenLight) {
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

    @Override
    public void onAudioLevelUpdate(float f, float f2) {
    }
}

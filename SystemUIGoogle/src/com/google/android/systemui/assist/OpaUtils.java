package com.google.android.systemui.assist;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.util.ArraySet;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.app.AssistUtils;
import com.android.systemui.R;

public final class OpaUtils {
    static final Interpolator INTERPOLATOR_40_40 = new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f);
    static final Interpolator INTERPOLATOR_40_OUT = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);

    static float getDeltaDiamondPositionBottomX() {
        return 0.0f;
    }

    static float getDeltaDiamondPositionLeftY() {
        return 0.0f;
    }

    static float getDeltaDiamondPositionRightY() {
        return 0.0f;
    }

    static float getDeltaDiamondPositionTopX() {
        return 0.0f;
    }

    static Animator getScaleAnimatorX(View view, float f, int i, Interpolator interpolator) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(3, f);
        renderNodeAnimator.setTarget(view);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setDuration((long) i);
        return renderNodeAnimator;
    }

    static Animator getScaleAnimatorY(View view, float f, int i, Interpolator interpolator) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(4, f);
        renderNodeAnimator.setTarget(view);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setDuration((long) i);
        return renderNodeAnimator;
    }

    static Animator getDeltaAnimatorX(View view, Interpolator interpolator, float f, int i) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(8, view.getX() + f);
        renderNodeAnimator.setTarget(view);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setDuration((long) i);
        return renderNodeAnimator;
    }

    static Animator getDeltaAnimatorY(View view, Interpolator interpolator, float f, int i) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(9, view.getY() + f);
        renderNodeAnimator.setTarget(view);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setDuration((long) i);
        return renderNodeAnimator;
    }

    static Animator getTranslationAnimatorX(View view, Interpolator interpolator, int i) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(0, 0.0f);
        renderNodeAnimator.setTarget(view);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setDuration((long) i);
        return renderNodeAnimator;
    }

    static Animator getTranslationAnimatorY(View view, Interpolator interpolator, int i) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(1, 0.0f);
        renderNodeAnimator.setTarget(view);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setDuration((long) i);
        return renderNodeAnimator;
    }

    static ObjectAnimator getAlphaObjectAnimator(View view, float f, int i, int i2, Interpolator interpolator) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, f);
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(i);
        ofFloat.setStartDelay(i2);
        return ofFloat;
    }

    static Animator getAlphaAnimator(View view, float f, int i, Interpolator interpolator) {
        return getAlphaAnimator(view, f, i, 0, interpolator);
    }

    static Animator getAlphaAnimator(View view, float f, int i, int i2, Interpolator interpolator) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(11, f);
        renderNodeAnimator.setTarget(view);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setDuration((long) i);
        renderNodeAnimator.setStartDelay((long) i2);
        return renderNodeAnimator;
    }

    static Animator getLongestAnim(ArraySet<Animator> arraySet) {
        long l = Long.MIN_VALUE;
        Animator animator = null;
        for (int i = arraySet.size() - 1; i >= 0; --i) {
            Animator animator2 = arraySet.valueAt(i);
            long l2 = l;
            if (animator2.getTotalDuration() > l) {
                l2 = animator2.getTotalDuration();
                animator = animator2;
            }
            l = l2;
        }
        return animator;
    }

    static ObjectAnimator getScaleObjectAnimator(View view, float f, int i, Interpolator interpolator) {
        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat(View.SCALE_X, f), PropertyValuesHolder.ofFloat(View.SCALE_Y, f));
        ofPropertyValuesHolder.setDuration(i);
        ofPropertyValuesHolder.setInterpolator(interpolator);
        return ofPropertyValuesHolder;
    }

    static ObjectAnimator getTranslationObjectAnimatorY(View view, Interpolator interpolator, float f, float f2, int i) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.Y, f2, f2 + f);
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(i);
        return ofFloat;
    }

    static ObjectAnimator getTranslationObjectAnimatorX(View view, Interpolator interpolator, float f, float f2, int i) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.X, f2, f2 + f);
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(i);
        return ofFloat;
    }

    static float getPxVal(Resources resources, int i) {
        return (float) resources.getDimensionPixelOffset(i);
    }

    static boolean isAGSACurrentAssistant(Context context) {
        ComponentName assistComponentForUser = new AssistUtils(context).getAssistComponentForUser(-2);
        return assistComponentForUser != null && "com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService".equals(assistComponentForUser.flattenToString());
    }

    static float getDeltaDiamondPositionTopY(Resources resources) {
        return -getPxVal(resources, R.dimen.opa_diamond_translation);
    }

    static float getDeltaDiamondPositionLeftX(Resources resources) {
        return -getPxVal(resources, R.dimen.opa_diamond_translation);
    }

    static float getDeltaDiamondPositionRightX(Resources resources) {
        return getPxVal(resources, R.dimen.opa_diamond_translation);
    }

    static float getDeltaDiamondPositionBottomY(Resources resources) {
        return getPxVal(resources, R.dimen.opa_diamond_translation);
    }
}

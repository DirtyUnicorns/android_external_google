package com.google.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.Interpolators;
import com.google.android.systemui.elmyra.feedback.FeedbackEffect;
import com.google.android.systemui.elmyra.sensors.GestureSensor;
import java.util.ArrayList;

public class LockscreenOpaLayout extends FrameLayout implements FeedbackEffect {
    private final Interpolator INTERPOLATOR_5_100 = new PathInterpolator(1.0f, 0.0f, 0.95f, 1.0f);
    private final int RED_YELLOW_START_DELAY = 17;
    private final ArrayList<View> mAnimatedViews = new ArrayList<>();
    private View mBlue;
    private AnimatorSet mCannedAnimatorSet;
    /* access modifiers changed from: private */
    public final ArraySet<Animator> mCurrentAnimators = new ArraySet<>();
    /* access modifiers changed from: private */
    public AnimatorSet mGestureAnimatorSet;
    /* access modifiers changed from: private */
    public int mGestureState = 0;
    private View mGreen;
    private AnimatorSet mLineAnimatorSet;
    private View mRed;
    private Resources mResources;
    private View mYellow;

    public LockscreenOpaLayout(Context context) {
        super(context);
    }

    public LockscreenOpaLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public LockscreenOpaLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public LockscreenOpaLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mResources = getResources();
        this.mBlue = findViewById(R.id.blue);
        this.mRed = findViewById(R.id.red);
        this.mYellow = findViewById(R.id.yellow);
        this.mGreen = findViewById(R.id.green);
        this.mAnimatedViews.add(this.mBlue);
        this.mAnimatedViews.add(this.mRed);
        this.mAnimatedViews.add(this.mYellow);
        this.mAnimatedViews.add(this.mGreen);
    }

    private void startCannedAnimation() {
        if (isAttachedToWindow()) {
            skipToStartingValue();
            this.mGestureState = 3;
            this.mGestureAnimatorSet = getCannedAnimatorSet();
            this.mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                /* class com.google.android.systemui.assist.LockscreenOpaLayout.C15431 */

                public void onAnimationEnd(Animator animator) {
                    int unused = mGestureState = 1;
                    LockscreenOpaLayout lockscreenOpaLayout = LockscreenOpaLayout.this;
                    AnimatorSet unused2 = lockscreenOpaLayout.mGestureAnimatorSet = lockscreenOpaLayout.getLineAnimatorSet();
                    mGestureAnimatorSet.setCurrentPlayTime(0);
                }
            });
            this.mGestureAnimatorSet.start();
            return;
        }
        skipToStartingValue();
    }

    /* access modifiers changed from: private */
    public void startRetractAnimation() {
        if (isAttachedToWindow()) {
            AnimatorSet animatorSet = this.mGestureAnimatorSet;
            if (animatorSet != null) {
                animatorSet.removeAllListeners();
                this.mGestureAnimatorSet.cancel();
            }
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet) getRetractAnimatorSet());
            startAll(this.mCurrentAnimators);
            this.mGestureState = 4;
            return;
        }
        skipToStartingValue();
    }

    /* access modifiers changed from: private */
    public void startCollapseAnimation() {
        if (isAttachedToWindow()) {
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet) getCollapseAnimatorSet());
            startAll(this.mCurrentAnimators);
            this.mGestureState = 2;
            return;
        }
        skipToStartingValue();
    }

    private void startAll(ArraySet<Animator> arraySet) {
        for (int size = arraySet.size() - 1; size >= 0; size--) {
            arraySet.valueAt(size).start();
        }
    }

    private ArraySet<Animator> getRetractAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mRed, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mBlue, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mGreen, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mYellow, OpaUtils.INTERPOLATOR_40_OUT, 190));
        OpaUtils.getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.LockscreenOpaLayout.C15442 */

            public void onAnimationEnd(Animator animator) {
                mCurrentAnimators.clear();
                skipToStartingValue();
                int unused = mGestureState = 0;
                AnimatorSet unused2 = mGestureAnimatorSet = null;
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getCollapseAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mRed, OpaUtils.INTERPOLATOR_40_OUT, 133));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mBlue, OpaUtils.INTERPOLATOR_40_OUT, 150));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mYellow, OpaUtils.INTERPOLATOR_40_OUT, 133));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mGreen, OpaUtils.INTERPOLATOR_40_OUT, 150));
        OpaUtils.getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.LockscreenOpaLayout.C15453 */

            public void onAnimationEnd(Animator animator) {
                mCurrentAnimators.clear();
                AnimatorSet unused = mGestureAnimatorSet = null;
                int unused2 = mGestureState = 0;
                skipToStartingValue();
            }
        });
        return arraySet;
    }

    /* access modifiers changed from: private */
    public void skipToStartingValue() {
        int size = this.mAnimatedViews.size();
        for (int i = 0; i < size; i++) {
            View view = this.mAnimatedViews.get(i);
            view.setAlpha(0.0f);
            view.setTranslationX(0.0f);
        }
    }

    public void onRelease() {
        int i = this.mGestureState;
        if (i != 2 && i != 4) {
            if (i == 3) {
                if (this.mGestureAnimatorSet.isRunning()) {
                    this.mGestureAnimatorSet.removeAllListeners();
                    this.mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                        /* class com.google.android.systemui.assist.LockscreenOpaLayout.C15464 */

                        public void onAnimationEnd(Animator animator) {
                            startRetractAnimation();
                        }
                    });
                    return;
                }
                this.mGestureState = 4;
                startRetractAnimation();
            } else if (i == 1) {
                startRetractAnimation();
            }
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.max(long, long):long}
     arg types: [int, long]
     candidates:
      ClspMth{java.lang.Math.max(double, double):double}
      ClspMth{java.lang.Math.max(int, int):int}
      ClspMth{java.lang.Math.max(float, float):float}
      ClspMth{java.lang.Math.max(long, long):long} */
    public void onProgress(float f, int i) {
        int i2 = this.mGestureState;
        if (i2 != 2) {
            if (i2 == 4) {
                endCurrentAnimation();
            }
            if (f == 0.0f) {
                this.mGestureState = 0;
                return;
            }
            long max = Math.max(0L, ((long) (f * 533.0f)) - 167);
            int i3 = this.mGestureState;
            if (i3 == 0) {
                startCannedAnimation();
            } else if (i3 == 1) {
                this.mGestureAnimatorSet.setCurrentPlayTime(max);
            } else if (i3 == 3 && max >= 167) {
                this.mGestureAnimatorSet.end();
                if (this.mGestureState == 1) {
                    this.mGestureAnimatorSet.setCurrentPlayTime(max);
                }
            }
        }
    }

    public void onResolve(GestureSensor.DetectionProperties detectionProperties) {
        int i = this.mGestureState;
        if (i != 4 && i != 2) {
            if (i == 3) {
                this.mGestureState = 2;
                this.mGestureAnimatorSet.removeAllListeners();
                this.mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    /* class com.google.android.systemui.assist.LockscreenOpaLayout.C15475 */

                    public void onAnimationEnd(Animator animator) {
                        LockscreenOpaLayout lockscreenOpaLayout = LockscreenOpaLayout.this;
                        AnimatorSet unused = lockscreenOpaLayout.mGestureAnimatorSet = lockscreenOpaLayout.getLineAnimatorSet();
                        mGestureAnimatorSet.removeAllListeners();
                        mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                            /* class com.google.android.systemui.assist.LockscreenOpaLayout.C15475.C15481 */

                            public void onAnimationEnd(Animator animator) {
                                startCollapseAnimation();
                            }
                        });
                        mGestureAnimatorSet.end();
                    }
                });
                return;
            }
            AnimatorSet animatorSet = this.mGestureAnimatorSet;
            if (animatorSet != null) {
                this.mGestureState = 2;
                animatorSet.removeAllListeners();
                this.mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    /* class com.google.android.systemui.assist.LockscreenOpaLayout.C15496 */

                    public void onAnimationEnd(Animator animator) {
                        startCollapseAnimation();
                    }
                });
                if (!this.mGestureAnimatorSet.isStarted()) {
                    this.mGestureAnimatorSet.start();
                }
            }
        }
    }

    private AnimatorSet getCannedAnimatorSet() {
        AnimatorSet animatorSet = this.mCannedAnimatorSet;
        if (animatorSet != null) {
            animatorSet.removeAllListeners();
            this.mCannedAnimatorSet.cancel();
            return this.mCannedAnimatorSet;
        }
        this.mCannedAnimatorSet = new AnimatorSet();
        ObjectAnimator translationObjectAnimatorX = OpaUtils.getTranslationObjectAnimatorX(this.mRed, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_canned_ry), this.mRed.getX(), 83);
        translationObjectAnimatorX.setStartDelay(17);
        ObjectAnimator translationObjectAnimatorX2 = OpaUtils.getTranslationObjectAnimatorX(this.mYellow, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_canned_ry), this.mYellow.getX(), 83);
        translationObjectAnimatorX2.setStartDelay(17);
        this.mCannedAnimatorSet.play(translationObjectAnimatorX).with(translationObjectAnimatorX2).with(OpaUtils.getTranslationObjectAnimatorX(this.mBlue, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_canned_bg), this.mBlue.getX(), 167)).with(OpaUtils.getTranslationObjectAnimatorX(this.mGreen, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_canned_bg), this.mGreen.getX(), 167)).with(OpaUtils.getAlphaObjectAnimator(this.mRed, 1.0f, 50, 130, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(this.mYellow, 1.0f, 50, 130, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(this.mBlue, 1.0f, 50, 113, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(this.mGreen, 1.0f, 50, 113, Interpolators.LINEAR));
        return this.mCannedAnimatorSet;
    }

    /* access modifiers changed from: private */
    public AnimatorSet getLineAnimatorSet() {
        AnimatorSet animatorSet = this.mLineAnimatorSet;
        if (animatorSet != null) {
            animatorSet.removeAllListeners();
            this.mLineAnimatorSet.cancel();
            return this.mLineAnimatorSet;
        }
        this.mLineAnimatorSet = new AnimatorSet();
        this.mLineAnimatorSet.play(OpaUtils.getTranslationObjectAnimatorX(this.mRed, this.INTERPOLATOR_5_100, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_translation_ry), this.mRed.getX(), 366)).with(OpaUtils.getTranslationObjectAnimatorX(this.mYellow, this.INTERPOLATOR_5_100, OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_translation_ry), this.mYellow.getX(), 366)).with(OpaUtils.getTranslationObjectAnimatorX(this.mGreen, this.INTERPOLATOR_5_100, OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_translation_bg), this.mGreen.getX(), 366)).with(OpaUtils.getTranslationObjectAnimatorX(this.mBlue, this.INTERPOLATOR_5_100, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_lockscreen_translation_bg), this.mBlue.getX(), 366));
        return this.mLineAnimatorSet;
    }

    private void endCurrentAnimation() {
        if (!this.mCurrentAnimators.isEmpty()) {
            for (int size = this.mCurrentAnimators.size() - 1; size >= 0; size--) {
                Animator valueAt = this.mCurrentAnimators.valueAt(size);
                valueAt.removeAllListeners();
                valueAt.end();
            }
            this.mCurrentAnimators.clear();
        }
        this.mGestureState = 0;
    }
}

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
    private final ArraySet<Animator> mCurrentAnimators = new ArraySet<>();
    private AnimatorSet mGestureAnimatorSet;
    private int mGestureState = 0;
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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mResources = getResources();
        mBlue = findViewById(R.id.blue);
        mRed = findViewById(R.id.red);
        mYellow = findViewById(R.id.yellow);
        mGreen = findViewById(R.id.green);
        mAnimatedViews.add(mBlue);
        mAnimatedViews.add(mRed);
        mAnimatedViews.add(mYellow);
        mAnimatedViews.add(mGreen);
    }

    private void startCannedAnimation() {
        if (isAttachedToWindow()) {
            skipToStartingValue();
            mGestureState = 3;
            mGestureAnimatorSet = getCannedAnimatorSet();
            mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    mGestureState = 1;
                    LockscreenOpaLayout lockscreenOpaLayout = LockscreenOpaLayout.this;
                    lockscreenOpaLayout.mGestureAnimatorSet = lockscreenOpaLayout.getLineAnimatorSet();
                    mGestureAnimatorSet.setCurrentPlayTime(0);
                }
            });
            mGestureAnimatorSet.start();
            return;
        }
        skipToStartingValue();
    }

    private void startRetractAnimation() {
        if (isAttachedToWindow()) {
            if (mGestureAnimatorSet != null) {
                mGestureAnimatorSet.removeAllListeners();
                mGestureAnimatorSet.cancel();
            }
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getRetractAnimatorSet());
            startAll(mCurrentAnimators);
            mGestureState = 4;
            return;
        }
        skipToStartingValue();
    }

    private void startCollapseAnimation() {
        if (isAttachedToWindow()) {
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getCollapseAnimatorSet());
            startAll(mCurrentAnimators);
            mGestureState = 2;
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
        arraySet.add(OpaUtils.getTranslationAnimatorX(mRed, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mBlue, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mGreen, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mYellow, OpaUtils.INTERPOLATOR_40_OUT, 190));
        OpaUtils.getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                mCurrentAnimators.clear();
                skipToStartingValue();
                mGestureState = 0;
                mGestureAnimatorSet = null;
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getCollapseAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        arraySet.add(OpaUtils.getTranslationAnimatorX(mRed, OpaUtils.INTERPOLATOR_40_OUT, 133));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mBlue, OpaUtils.INTERPOLATOR_40_OUT, 150));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mYellow, OpaUtils.INTERPOLATOR_40_OUT, 133));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mGreen, OpaUtils.INTERPOLATOR_40_OUT, 150));
        OpaUtils.getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                mCurrentAnimators.clear();
                mGestureAnimatorSet = null;
                mGestureState = 0;
                skipToStartingValue();
            }
        });
        return arraySet;
    }

    private void skipToStartingValue() {
        for (int i = 0; i < mAnimatedViews.size(); i++) {
            View view = mAnimatedViews.get(i);
            view.setAlpha(0.0f);
            view.setTranslationX(0.0f);
        }
    }

    public void onRelease() {
        if (mGestureState != 2 && mGestureState != 4) {
            if (mGestureState == 3) {
                if (mGestureAnimatorSet.isRunning()) {
                    mGestureAnimatorSet.removeAllListeners();
                    mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            startRetractAnimation();
                        }
                    });
                    return;
                }
                mGestureState = 4;
                startRetractAnimation();
            } else if (mGestureState == 1) {
                startRetractAnimation();
            }
        }
    }

    public void onProgress(float f, int i) {
        if (mGestureState != 2) {
            if (mGestureState == 4) {
                endCurrentAnimation();
            }
            if (f == 0.0f) {
                mGestureState = 0;
                return;
            }
            long max = Math.max(0L, ((long) (f * 533.0f)) - 167);
            if (mGestureState == 0) {
                startCannedAnimation();
            } else if (mGestureState == 1) {
                mGestureAnimatorSet.setCurrentPlayTime(max);
            } else if (mGestureState == 3 && max >= 167) {
                mGestureAnimatorSet.end();
                if (mGestureState == 1) {
                    mGestureAnimatorSet.setCurrentPlayTime(max);
                }
            }
        }
    }

    public void onResolve(GestureSensor.DetectionProperties detectionProperties) {
        if (mGestureState != 4 && mGestureState != 2) {
            if (mGestureState == 3) {
                mGestureState = 2;
                mGestureAnimatorSet.removeAllListeners();
                mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        LockscreenOpaLayout lockscreenOpaLayout = LockscreenOpaLayout.this;
                        lockscreenOpaLayout.mGestureAnimatorSet = lockscreenOpaLayout.getLineAnimatorSet();
                        mGestureAnimatorSet.removeAllListeners();
                        mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animator) {
                                startCollapseAnimation();
                            }
                        });
                        mGestureAnimatorSet.end();
                    }
                });
                return;
            }
            if (mGestureAnimatorSet != null) {
                mGestureState = 2;
                mGestureAnimatorSet.removeAllListeners();
                mGestureAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        startCollapseAnimation();
                    }
                });
                if (!mGestureAnimatorSet.isStarted()) {
                    mGestureAnimatorSet.start();
                }
            }
        }
    }

    private AnimatorSet getCannedAnimatorSet() {
        if (mCannedAnimatorSet != null) {
            mCannedAnimatorSet.removeAllListeners();
            mCannedAnimatorSet.cancel();
            return mCannedAnimatorSet;
        }
        mCannedAnimatorSet = new AnimatorSet();
        ObjectAnimator translationObjectAnimatorX = OpaUtils.getTranslationObjectAnimatorX(mRed, OpaUtils.INTERPOLATOR_40_40,
                -OpaUtils.getPxVal(mResources, R.dimen.opa_lockscreen_canned_ry), mRed.getX(), 83);
        translationObjectAnimatorX.setStartDelay(17);
        ObjectAnimator translationObjectAnimatorX2 = OpaUtils.getTranslationObjectAnimatorX(mYellow, OpaUtils.INTERPOLATOR_40_40,
                OpaUtils.getPxVal(mResources, R.dimen.opa_lockscreen_canned_ry), mYellow.getX(), 83);
        translationObjectAnimatorX2.setStartDelay(17);
        mCannedAnimatorSet.play(translationObjectAnimatorX).with(translationObjectAnimatorX2)
                .with(OpaUtils.getTranslationObjectAnimatorX(mBlue, OpaUtils.INTERPOLATOR_40_40,
                -OpaUtils.getPxVal(mResources, R.dimen.opa_lockscreen_canned_bg), mBlue.getX(), 167))
                .with(OpaUtils.getTranslationObjectAnimatorX(mGreen, OpaUtils.INTERPOLATOR_40_40,
                OpaUtils.getPxVal(mResources, R.dimen.opa_lockscreen_canned_bg), mGreen.getX(), 167))
                .with(OpaUtils.getAlphaObjectAnimator(mRed, 1.0f, 50, 130, Interpolators.LINEAR))
                .with(OpaUtils.getAlphaObjectAnimator(mYellow, 1.0f, 50, 130, Interpolators.LINEAR))
                .with(OpaUtils.getAlphaObjectAnimator(mBlue, 1.0f, 50, 113, Interpolators.LINEAR))
                .with(OpaUtils.getAlphaObjectAnimator(mGreen, 1.0f, 50, 113, Interpolators.LINEAR));
        return mCannedAnimatorSet;
    }

    private AnimatorSet getLineAnimatorSet() {
        if (mLineAnimatorSet != null) {
            mLineAnimatorSet.removeAllListeners();
            mLineAnimatorSet.cancel();
            return mLineAnimatorSet;
        }
        mLineAnimatorSet = new AnimatorSet();
        mLineAnimatorSet.play(OpaUtils.getTranslationObjectAnimatorX(mRed, INTERPOLATOR_5_100, -OpaUtils.getPxVal(mResources,
                R.dimen.opa_lockscreen_translation_ry), mRed.getX(), 366)).with(OpaUtils.getTranslationObjectAnimatorX(mYellow,
                INTERPOLATOR_5_100, OpaUtils.getPxVal(mResources, R.dimen.opa_lockscreen_translation_ry), mYellow.getX(), 366))
                .with(OpaUtils.getTranslationObjectAnimatorX(mGreen, INTERPOLATOR_5_100, OpaUtils.getPxVal(mResources,
                R.dimen.opa_lockscreen_translation_bg), mGreen.getX(), 366)).with(OpaUtils.getTranslationObjectAnimatorX(mBlue,
                INTERPOLATOR_5_100, -OpaUtils.getPxVal(mResources, R.dimen.opa_lockscreen_translation_bg), mBlue.getX(), 366));
        return mLineAnimatorSet;
    }

    private void endCurrentAnimation() {
        if (!mCurrentAnimators.isEmpty()) {
            for (int size = mCurrentAnimators.size() - 1; size >= 0; size--) {
                Animator valueAt = mCurrentAnimators.valueAt(size);
                valueAt.removeAllListeners();
                valueAt.end();
            }
            mCurrentAnimators.clear();
        }
        mGestureState = 0;
    }
}

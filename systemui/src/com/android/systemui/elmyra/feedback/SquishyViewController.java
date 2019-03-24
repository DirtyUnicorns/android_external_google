package com.google.android.systemui.elmyra.feedback;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.ServiceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.IRotationWatcher.Stub;
import android.view.IWindowManager;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import java.util.ArrayList;
import java.util.List;

class SquishyViewController implements FeedbackEffect {
    private static final Interpolator SQUISH_TRANSLATION_MAP = new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f);
    private AnimatorSet mAnimatorSet;
    private final Context mContext;
    private float mLastPressure;
    private final List<View> mLeftViews = new ArrayList();
    private float mPressure;
    private final List<View> mRightViews = new ArrayList();
    private final Stub mRotationWatcher = new RotationWatcherStub();
    private int mScreenRotation;
    private final float mSquishTranslationMax;
    private final IWindowManager mWindowManager;


    class RotationWatcherStub extends Stub {
        RotationWatcherStub() {
        }

        public void onRotationChanged(int i) {
            mScreenRotation = i;
        }
    }

    private class SpringInterpolator implements Interpolator {
        private float mBounce;
        private float mMass;

        SpringInterpolator(float f, float f2) {
            mMass = f;
            mBounce = f2;
        }

        public float getInterpolation(float f) {
            return (float) ((-(Math.exp((-(f / mMass))) * Math.cos(mBounce * f))) + 1.0d);
        }
    }

    public SquishyViewController(Context context) {
        mContext = context;
        mSquishTranslationMax = m11px(8.0f);
        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        try {
            mScreenRotation = mWindowManager.watchRotation(mRotationWatcher, mContext.getDisplay().getDisplayId());
        } catch (Throwable e) {
            mScreenRotation = 0;
        }
    }

    private AnimatorSet createSpringbackAnimatorSet(View view) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, new float[]{view.getTranslationX(), 0.0f});
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, new float[]{view.getTranslationY(), 0.0f});
        ofFloat.setDuration(250);
        ofFloat2.setDuration(250);
        float max = 3.1f * Math.max(Math.abs(view.getTranslationX()) / 8.0f, Math.abs(view.getTranslationY()) / 8.0f);
        ofFloat.setInterpolator(new SpringInterpolator(0.31f, max));
        ofFloat2.setInterpolator(new SpringInterpolator(0.31f, max));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
        animatorSet.setStartDelay(50);
        return animatorSet;
    }

    private AnimatorSet createSpringbackAnimatorSets() {
        int i = 0;
        AnimatorSet animatorSet = new AnimatorSet();
        for (int i2 = 0; i2 < mLeftViews.size(); i2++) {
            animatorSet.play(createSpringbackAnimatorSet((View) mLeftViews.get(i2)));
        }
        while (i < mRightViews.size()) {
            animatorSet.play(createSpringbackAnimatorSet((View) mRightViews.get(i)));
            i++;
        }
        return animatorSet;
    }

    /* renamed from: px */
    private float m11px(float f) {
        return TypedValue.applyDimension(1, f, mContext.getResources().getDisplayMetrics());
    }

    private void setViewTranslation(View view, float f) {
        if (view.isAttachedToWindow()) {
            if (view.getLayoutDirection() == 1) {
                f *= -1.0f;
            }
            switch (mScreenRotation) {
                case 0:
                case 2:
                    view.setTranslationX(f);
                    view.setTranslationY(0.0f);
                    return;
                case 1:
                    view.setTranslationX(0.0f);
                    view.setTranslationY(-f);
                    return;
                case 3:
                    view.setTranslationX(0.0f);
                    view.setTranslationY(f);
                    return;
                default:
                    return;
            }
        }
    }

    private void translateViews(float f) {
        int i = 0;
        for (int i2 = 0; i2 < mLeftViews.size(); i2++) {
            setViewTranslation((View) mLeftViews.get(i2), f);
        }
        while (i < mRightViews.size()) {
            setViewTranslation((View) mRightViews.get(i), -f);
            i++;
        }
    }

    public void addLeftView(View view) {
        mLeftViews.add(view);
    }

    public void addRightView(View view) {
        mRightViews.add(view);
    }

    public void clearViews() {
        translateViews(0.0f);
        mLeftViews.clear();
        mRightViews.clear();
    }

    public boolean isAttachedToWindow() {
        int i;
        for (i = 0; i < mLeftViews.size(); i++) {
            if (!((View) mLeftViews.get(i)).isAttachedToWindow()) {
                return false;
            }
        }
        for (i = 0; i < mRightViews.size(); i++) {
            if (!((View) mRightViews.get(i)).isAttachedToWindow()) {
                return false;
            }
        }
        return true;
    }

    @Override
	public void onProgress(float f, int i) {
        float min = Math.min(f, 1.0f) / 1.0f;
        if (min != 0.0f) {
            mPressure = (1.0f * min) + (mLastPressure * 0.0f);
        } else {
            mPressure = min;
        }
        if (mAnimatorSet == null || !mAnimatorSet.isRunning()) {
            if (min - mLastPressure < -0.1f) {
                mAnimatorSet = createSpringbackAnimatorSets();
                mAnimatorSet.start();
            } else {
                translateViews(mSquishTranslationMax * SQUISH_TRANSLATION_MAP.getInterpolation(mPressure));
            }
        }
        mLastPressure = mPressure;
    }

    @Override
	public void onRelease() {
        onProgress(0.0f, 0);
    }

    public void onResolve(DetectionProperties detectionProperties) {
        onProgress(0.0f, 0);
    }
}

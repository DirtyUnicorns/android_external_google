package com.google.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.ButtonInterface;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.google.android.systemui.elmyra.feedback.FeedbackEffect;
import com.google.android.systemui.elmyra.sensors.GestureSensor;
import java.util.ArrayList;

public class OpaLayout extends FrameLayout implements ButtonInterface, FeedbackEffect {
    private final Interpolator HOME_DISAPPEAR_INTERPOLATOR;
    private final ArrayList<View> mAnimatedViews;
    private int mAnimationState;
    private View mBlue;
    private View mBottom;
    private final ArraySet<Animator> mCurrentAnimators;
    private boolean mDelayTouchFeedback;
    private final Runnable mDiamondAnimation;
    private boolean mDiamondAnimationDelayed;
    private final Interpolator mDiamondInterpolator;
    private long mGestureAnimationSetDuration;
    private AnimatorSet mGestureAnimatorSet;
    private AnimatorSet mGestureLineSet;
    private int mGestureState;
    private View mGreen;
    private ImageView mHalo;
    private KeyButtonView mHome;
    private int mHomeDiameter;
    private boolean mIsPressed;
    private boolean mIsVertical;
    private View mLeft;
    private boolean mOpaEnabled;
    private boolean mOpaEnabledNeedsUpdate;
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener;
    private OverviewProxyService mOverviewProxyService;
    private View mRed;
    private Resources mResources;
    private final Runnable mRetract;
    private View mRight;
    private long mStartTime;
    private View mTop;
    private int mTouchDownX;
    private int mTouchDownY;
    private ImageView mWhite;
    private ImageView mWhiteCutout;
    private boolean mWindowVisible;
    private View mYellow;

    public OpaLayout(Context context) {
        this(context, null);
    }

    public OpaLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpaLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        HOME_DISAPPEAR_INTERPOLATOR = new PathInterpolator(0.65f, 0.0f, 1.0f, 1.0f);
        mDiamondInterpolator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
        mCurrentAnimators = new ArraySet<>();
        mAnimatedViews = new ArrayList<>();
        mAnimationState = 0;
        mGestureState = 0;
        mRetract = () -> {
            cancelCurrentAnimation();
            startRetractAnimation();
        };
        mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() {
            public void onConnectionChanged(boolean z) {
                updateOpaLayout();
            }
        };
        mDiamondAnimation = () -> {
            if (mCurrentAnimators.isEmpty()) {
                startDiamondAnimation();
            }
        };
    }

    public OpaLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mResources = getResources();
        mBlue = findViewById(R.id.blue);
        mRed = findViewById(R.id.red);
        mYellow = findViewById(R.id.yellow);
        mGreen = findViewById(R.id.green);
        mWhite = findViewById(R.id.white);
        mWhiteCutout = findViewById(R.id.white_cutout);
        mHalo = findViewById(R.id.halo);
        mHome = findViewById(R.id.home_button);
        mHalo.setImageDrawable(KeyButtonDrawable.create(new ContextThemeWrapper(getContext(), R.style.DualToneLightTheme), new ContextThemeWrapper(getContext(), R.style.DualToneDarkTheme), R.drawable.halo, true, null));
        mHomeDiameter = mResources.getDimensionPixelSize(R.dimen.opa_disabled_home_diameter);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mWhiteCutout.setLayerType(2, paint);
        mAnimatedViews.add(mBlue);
        mAnimatedViews.add(mRed);
        mAnimatedViews.add(mYellow);
        mAnimatedViews.add(mGreen);
        mAnimatedViews.add(mWhite);
        mAnimatedViews.add(mWhiteCutout);
        mAnimatedViews.add(mHalo);
        mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    }

    public void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        mWindowVisible = i == 0;
        if (mWindowVisible) {
            updateOpaLayout();
        }
        cancelCurrentAnimation();
        skipToStartingValue();
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mHome.setOnLongClickListener(onLongClickListener);
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mHome.setOnTouchListener(onTouchListener);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (getOpaEnabled() && ValueAnimator.areAnimatorsEnabled()) {
            if (mGestureState == 0) {
                final int action = motionEvent.getAction();
                if (action != 0) {
                    if (action != 1) {
                        if (action != 2) {
                            if (action != 3) {
                                return false;
                            }
                        } else {
                            float quickStepTouchSlopPx = QuickStepContract.getQuickStepTouchSlopPx(getContext());
                            if (Math.abs(motionEvent.getRawX() - mTouchDownX) > quickStepTouchSlopPx || Math.abs(motionEvent.getRawY() - mTouchDownY) > quickStepTouchSlopPx) {
                                abortCurrentGesture();
                                return false;
                            }
                            return false;
                        }
                    }
                    if (mDiamondAnimationDelayed) {
                        if (mIsPressed) {
                            postDelayed(mRetract, 200);
                        }
                    } else {
                        if (mAnimationState == 1) {
                            long elapsedRealtime = SystemClock.elapsedRealtime();
                            removeCallbacks(mRetract);
                            postDelayed(mRetract, 100 - (elapsedRealtime - mStartTime));
                            removeCallbacks(mDiamondAnimation);
                            cancelLongPress();
                            return false;
                        }
                        if (mIsPressed) {
                            mRetract.run();
                        }
                    }
                    mIsPressed = false;
                } else {
                    mTouchDownX = (int)motionEvent.getRawX();
                    mTouchDownY = (int)motionEvent.getRawY();
                    boolean b;
                    if (!mCurrentAnimators.isEmpty()) {
                        if (mAnimationState != 2) {
                            return false;
                        }
                        endCurrentAnimation();
                        b = true;
                    } else {
                        b = false;
                    }
                    mStartTime = SystemClock.elapsedRealtime();
                    mIsPressed = true;
                    removeCallbacks(mDiamondAnimation);
                    removeCallbacks(mRetract);
                    if (mDelayTouchFeedback && !b) {
                        mDiamondAnimationDelayed = true;
                        postDelayed(mDiamondAnimation, ViewConfiguration.getTapTimeout());
                    } else {
                        mDiamondAnimationDelayed = false;
                        startDiamondAnimation();
                    }
                }
            }
        }
        return false;
    }

    public void setAccessibilityDelegate(View.AccessibilityDelegate accessibilityDelegate) {
        super.setAccessibilityDelegate(accessibilityDelegate);
        mHome.setAccessibilityDelegate(accessibilityDelegate);
    }

    public void setImageDrawable(Drawable drawable) {
        mWhite.setImageDrawable(drawable);
        mWhiteCutout.setImageDrawable(drawable);
    }

    public void abortCurrentGesture() {
        mHome.abortCurrentGesture();
        mIsPressed = false;
        mDiamondAnimationDelayed = false;
        removeCallbacks(mDiamondAnimation);
        cancelLongPress();
        if (mAnimationState == 3 || mAnimationState == 1) {
            mRetract.run();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateOpaLayout();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mOverviewProxyService.addCallback(mOverviewProxyListener);
        mOpaEnabledNeedsUpdate = true;
        post(this::getOpaEnabled);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mOverviewProxyService.removeCallback(mOverviewProxyListener);
    }

    private void startDiamondAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            setDotsVisible();
            mCurrentAnimators.addAll(getDiamondAnimatorSet());
            mAnimationState = 1;
            startAll(mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    private void startRetractAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getRetractAnimatorSet());
            mAnimationState = 2;
            startAll(mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    private void startLineAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getLineAnimatorSet());
            mAnimationState = 3;
            startAll(mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    private void startCollapseAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getCollapseAnimatorSet());
            mAnimationState = 3;
            startAll(mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    private void startAll(ArraySet<Animator> arraySet) {
        for (int size = arraySet.size() - 1; size >= 0; size--) {
            arraySet.valueAt(size).start();
        }
    }

    private boolean allowAnimations() {
        return isAttachedToWindow() && mWindowVisible;
    }

    private ArraySet<Animator> getDiamondAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        arraySet.add(OpaUtils.getDeltaAnimatorY(mTop, mDiamondInterpolator, -OpaUtils.getPxVal(mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(mTop, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mTop, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getDeltaAnimatorY(mBottom, mDiamondInterpolator, OpaUtils.getPxVal(mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(mBottom, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mBottom, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getDeltaAnimatorX(mLeft, mDiamondInterpolator, -OpaUtils.getPxVal(mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(mLeft, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mLeft, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getDeltaAnimatorX(mRight, mDiamondInterpolator, OpaUtils.getPxVal(mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(mRight, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mRight, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorX(mWhite, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mWhite, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorX(mWhiteCutout, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mWhiteCutout, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorX(mHalo, 0.47619048f, 100, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mHalo, 0.47619048f, 100, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getAlphaAnimator(mHalo, 0.0f, 100, Interpolators.FAST_OUT_SLOW_IN));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animator) {
                mCurrentAnimators.clear();
            }
            public void onAnimationEnd(Animator animator) {
                startLineAnimation();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getRetractAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        arraySet.add(OpaUtils.getTranslationAnimatorX(mRed, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(mRed, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(mRed, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mRed, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mBlue, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(mBlue, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(mBlue, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mBlue, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mGreen, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(mGreen, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(mGreen, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mGreen, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getTranslationAnimatorX(mYellow, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(mYellow, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(mYellow, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mYellow, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorX(mWhite, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mWhite, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorX(mWhiteCutout, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mWhiteCutout, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorX(mHalo, 1.0f, 190, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(mHalo, 1.0f, 190, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getAlphaAnimator(mHalo, 1.0f, 190, Interpolators.FAST_OUT_SLOW_IN));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                mCurrentAnimators.clear();
                skipToStartingValue();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getCollapseAnimatorSet() {
        Animator animator;
        Animator animator2;
        Animator animator3;
        Animator animator4;
        ArraySet<Animator> arraySet = new ArraySet<>();
        if (mIsVertical) {
            animator = OpaUtils.getTranslationAnimatorY(mRed, OpaUtils.INTERPOLATOR_40_OUT, 133);
        } else {
            animator = OpaUtils.getTranslationAnimatorX(mRed, OpaUtils.INTERPOLATOR_40_OUT, 133);
        }
        arraySet.add(animator);
        arraySet.add(OpaUtils.getScaleAnimatorX(mRed, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mRed, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        if (mIsVertical) {
            animator2 = OpaUtils.getTranslationAnimatorY(mBlue, OpaUtils.INTERPOLATOR_40_OUT, 150);
        } else {
            animator2 = OpaUtils.getTranslationAnimatorX(mBlue, OpaUtils.INTERPOLATOR_40_OUT, 150);
        }
        arraySet.add(animator2);
        arraySet.add(OpaUtils.getScaleAnimatorX(mBlue, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mBlue, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        if (mIsVertical) {
            animator3 = OpaUtils.getTranslationAnimatorY(mYellow, OpaUtils.INTERPOLATOR_40_OUT, 133);
        } else {
            animator3 = OpaUtils.getTranslationAnimatorX(mYellow, OpaUtils.INTERPOLATOR_40_OUT, 133);
        }
        arraySet.add(animator3);
        arraySet.add(OpaUtils.getScaleAnimatorX(mYellow, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mYellow, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        if (mIsVertical) {
            animator4 = OpaUtils.getTranslationAnimatorY(mGreen, OpaUtils.INTERPOLATOR_40_OUT, 150);
        } else {
            animator4 = OpaUtils.getTranslationAnimatorX(mGreen, OpaUtils.INTERPOLATOR_40_OUT, 150);
        }
        arraySet.add(animator4);
        arraySet.add(OpaUtils.getScaleAnimatorX(mGreen, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(mGreen, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        Animator scaleAnimatorX = OpaUtils.getScaleAnimatorX(mWhite, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorY = OpaUtils.getScaleAnimatorY(mWhite, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorX2 = OpaUtils.getScaleAnimatorX(mWhiteCutout, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorY2 = OpaUtils.getScaleAnimatorY(mWhiteCutout, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorX3 = OpaUtils.getScaleAnimatorX(mHalo, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorY3 = OpaUtils.getScaleAnimatorY(mHalo, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator alphaAnimator = OpaUtils.getAlphaAnimator(mHalo, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        scaleAnimatorX.setStartDelay(33);
        scaleAnimatorY.setStartDelay(33);
        scaleAnimatorX2.setStartDelay(33);
        scaleAnimatorY2.setStartDelay(33);
        scaleAnimatorX3.setStartDelay(33);
        scaleAnimatorY3.setStartDelay(33);
        alphaAnimator.setStartDelay(33);
        arraySet.add(scaleAnimatorX);
        arraySet.add(scaleAnimatorY);
        arraySet.add(scaleAnimatorX2);
        arraySet.add(scaleAnimatorY2);
        arraySet.add(scaleAnimatorX3);
        arraySet.add(scaleAnimatorY3);
        arraySet.add(alphaAnimator);
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                mCurrentAnimators.clear();
                skipToStartingValue();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getLineAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        if (mIsVertical) {
            arraySet.add(OpaUtils.getDeltaAnimatorY(mRed, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorX(mRed, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorY(mBlue, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorY(mYellow, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorX(mYellow, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorY(mGreen, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), 225));
        } else {
            arraySet.add(OpaUtils.getDeltaAnimatorX(mRed, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorY(mRed, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorX(mBlue, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorX(mYellow, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorY(mYellow, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorX(mGreen, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), 225));
        }
        arraySet.add(OpaUtils.getScaleAnimatorX(mWhite, 0.0f, 83, HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorY(mWhite, 0.0f, 83, HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorX(mWhiteCutout, 0.0f, 83, HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorY(mWhiteCutout, 0.0f, 83, HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorX(mHalo, 0.0f, 83, HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorY(mHalo, 0.0f, 83, HOME_DISAPPEAR_INTERPOLATOR));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                startCollapseAnimation();
            }

            public void onAnimationCancel(Animator animator) {
                mCurrentAnimators.clear();
            }
        });
        return arraySet;
    }

    public boolean getOpaEnabled() {
        if (mOpaEnabledNeedsUpdate) {
            ((AssistManagerGoogle) Dependency.get(AssistManager.class)).dispatchOpaEnabledState();
            if (mOpaEnabledNeedsUpdate) {
                Log.w("OpaLayout", "mOpaEnabledNeedsUpdate not cleared by AssistManagerGoogle!");
            }
        }
        return mOpaEnabled;
    }

    public void setOpaEnabled(boolean z) {
        Log.i("OpaLayout", "Setting opa enabled to " + z);
        mOpaEnabled = z;
        mOpaEnabledNeedsUpdate = false;
        updateOpaLayout();
    }

    public void updateOpaLayout() {
        boolean shouldShowSwipeUpUI = mOverviewProxyService.shouldShowSwipeUpUI();
        boolean z = true;
        boolean z2 = mOpaEnabled && !shouldShowSwipeUpUI;
        mHalo.setVisibility(z2 ? 0 : 4);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mHalo.getLayoutParams();
        if (z2 || shouldShowSwipeUpUI) {
            z = false;
        }
        int i = z ? mHomeDiameter : -1;
        layoutParams.width = i;
        layoutParams.height = i;
        mWhite.setLayoutParams(layoutParams);
        mWhiteCutout.setLayoutParams(layoutParams);
        ImageView.ScaleType scaleType = z ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER;
        mWhite.setScaleType(scaleType);
        mWhiteCutout.setScaleType(scaleType);
    }

    private void cancelCurrentAnimation() {
        if (!mCurrentAnimators.isEmpty()) {
            for (int size = mCurrentAnimators.size() - 1; size >= 0; size--) {
                Animator valueAt = mCurrentAnimators.valueAt(size);
                valueAt.removeAllListeners();
                valueAt.cancel();
            }
            mCurrentAnimators.clear();
            mAnimationState = 0;
        }
        if (mGestureAnimatorSet != null) {
            mGestureAnimatorSet.cancel();
            mGestureState = 0;
        }
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
        mAnimationState = 0;
    }

    private Animator getLongestAnim(ArraySet<Animator> arraySet) {
        long j = Long.MIN_VALUE;
        Animator animator = null;
        for (int size = arraySet.size() - 1; size >= 0; size--) {
            Animator valueAt = arraySet.valueAt(size);
            if (valueAt.getTotalDuration() > j) {
                j = valueAt.getTotalDuration();
                animator = valueAt;
            }
        }
        return animator;
    }

    private void setDotsVisible() {
        for (int i = 0; i < mAnimatedViews.size(); i++) {
            mAnimatedViews.get(i).setAlpha(1.0f);
        }
    }

    private void skipToStartingValue() {
        for (int i = 0; i < mAnimatedViews.size(); i++) {
            View view = mAnimatedViews.get(i);
            view.setScaleY(1.0f);
            view.setScaleX(1.0f);
            view.setTranslationY(0.0f);
            view.setTranslationX(0.0f);
            view.setAlpha(0.0f);
        }
        mHalo.setAlpha(1.0f);
        mWhite.setAlpha(1.0f);
        mWhiteCutout.setAlpha(1.0f);
        mAnimationState = 0;
        mGestureState = 0;
    }

    public void setVertical(boolean z) {
        if (!(mIsVertical == z || mGestureAnimatorSet == null)) {
            mGestureAnimatorSet.cancel();
            mGestureAnimatorSet = null;
            skipToStartingValue();
        }
        mIsVertical = z;
        mHome.setVertical(z);
        if (mIsVertical) {
            mTop = mGreen;
            mBottom = mBlue;
            mRight = mYellow;
            mLeft = mRed;
            return;
        }
        mTop = mRed;
        mBottom = mYellow;
        mLeft = mBlue;
        mRight = mGreen;
    }

    public void setDarkIntensity(float f) {
        if (mWhite.getDrawable() instanceof KeyButtonDrawable) {
            ((KeyButtonDrawable) mWhite.getDrawable()).setDarkIntensity(f);
        }
        ((KeyButtonDrawable) mHalo.getDrawable()).setDarkIntensity(f);
        mWhite.invalidate();
        mHalo.invalidate();
        mHome.setDarkIntensity(f);
    }

    public void setDelayTouchFeedback(boolean z) {
        mHome.setDelayTouchFeedback(z);
        mDelayTouchFeedback = z;
    }

    public void onRelease() {
        if (mAnimationState == 0 && mGestureState == 1) {
            if (mGestureAnimatorSet != null) {
                mGestureAnimatorSet.cancel();
            }
            mGestureState = 0;
            startRetractAnimation();
        }
    }

    public void onProgress(float f, int i) {
        if (mGestureState != 2 && allowAnimations()) {
            if (mAnimationState == 2) {
                endCurrentAnimation();
            }
            if (mAnimationState != 0) {
                return;
            }
            if (mGestureAnimatorSet == null) {
                mGestureAnimatorSet = getGestureAnimatorSet();
                mGestureAnimationSetDuration = mGestureAnimatorSet.getTotalDuration();
            }
            mGestureAnimatorSet.setCurrentPlayTime((long) ((mGestureAnimationSetDuration - 1) * f));
            mGestureState = f == 0.0f ? 0 : 1;
        }
    }

    public void onResolve(GestureSensor.DetectionProperties detectionProperties) {
        if (mAnimationState == 0) {
            if (mGestureState != 1 || mGestureAnimatorSet == null || mGestureAnimatorSet.isStarted()) {
                skipToStartingValue();
                return;
            }
            mGestureAnimatorSet.start();
            mGestureState = 2;
        }
    }

    private AnimatorSet getGestureAnimatorSet() {
        if (mGestureLineSet != null) {
            mGestureLineSet.removeAllListeners();
            mGestureLineSet.cancel();
            return mGestureLineSet;
        }
        mGestureLineSet = new AnimatorSet();
        ObjectAnimator scaleObjectAnimator = OpaUtils.getScaleObjectAnimator(mWhite, 0.0f, 100, OpaUtils.INTERPOLATOR_40_OUT);
        ObjectAnimator scaleObjectAnimator2 = OpaUtils.getScaleObjectAnimator(mWhiteCutout, 0.0f, 100, OpaUtils.INTERPOLATOR_40_OUT);
        ObjectAnimator scaleObjectAnimator3 = OpaUtils.getScaleObjectAnimator(mHalo, 0.0f, 100, OpaUtils.INTERPOLATOR_40_OUT);
        scaleObjectAnimator.setStartDelay(50);
        scaleObjectAnimator2.setStartDelay(50);
        mGestureLineSet.play(scaleObjectAnimator).with(scaleObjectAnimator2).with(scaleObjectAnimator3);
        mGestureLineSet.play(OpaUtils.getScaleObjectAnimator(mTop, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN)).with(scaleObjectAnimator).with(OpaUtils.getAlphaObjectAnimator(mRed, 1.0f, 50, 130, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(mYellow, 1.0f, 50, 130, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(mBlue, 1.0f, 50, 113, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(mGreen, 1.0f, 50, 113, Interpolators.LINEAR)).with(OpaUtils.getScaleObjectAnimator(mBottom, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN)).with(OpaUtils.getScaleObjectAnimator(mLeft, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN)).with(OpaUtils.getScaleObjectAnimator(mRight, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        if (mIsVertical) {
            ObjectAnimator translationObjectAnimatorY = OpaUtils.getTranslationObjectAnimatorY(mRed, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), mRed.getY() + OpaUtils.getDeltaDiamondPositionLeftY(), 350);
            translationObjectAnimatorY.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    startCollapseAnimation();
                }
            });
            mGestureLineSet.play(translationObjectAnimatorY).with(scaleObjectAnimator3).with(OpaUtils.getTranslationObjectAnimatorY(mBlue, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), mBlue.getY() + OpaUtils.getDeltaDiamondPositionBottomY(mResources), 350)).with(OpaUtils.getTranslationObjectAnimatorY(mYellow, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), mYellow.getY() + OpaUtils.getDeltaDiamondPositionRightY(), 350)).with(OpaUtils.getTranslationObjectAnimatorY(mGreen, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), mGreen.getY() + OpaUtils.getDeltaDiamondPositionTopY(mResources), 350));
        } else {
            ObjectAnimator translationObjectAnimatorX = OpaUtils.getTranslationObjectAnimatorX(mRed, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), mRed.getX() + OpaUtils.getDeltaDiamondPositionTopX(), 350);
            translationObjectAnimatorX.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    startCollapseAnimation();
                }
            });
            mGestureLineSet.play(translationObjectAnimatorX).with(scaleObjectAnimator).with(OpaUtils.getTranslationObjectAnimatorX(mBlue, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), mBlue.getX() + OpaUtils.getDeltaDiamondPositionLeftX(mResources), 350)).with(OpaUtils.getTranslationObjectAnimatorX(mYellow, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_ry), mYellow.getX() + OpaUtils.getDeltaDiamondPositionBottomX(), 350)).with(OpaUtils.getTranslationObjectAnimatorX(mGreen, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(mResources, R.dimen.opa_line_x_trans_bg), mGreen.getX() + OpaUtils.getDeltaDiamondPositionRightX(mResources), 350));
        }
        return mGestureLineSet;
    }
}

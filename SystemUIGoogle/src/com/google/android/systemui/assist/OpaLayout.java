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
    /* access modifiers changed from: private */
    public final ArraySet<Animator> mCurrentAnimators;
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
        this.HOME_DISAPPEAR_INTERPOLATOR = new PathInterpolator(0.65f, 0.0f, 1.0f, 1.0f);
        this.mDiamondInterpolator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
        this.mCurrentAnimators = new ArraySet<>();
        this.mAnimatedViews = new ArrayList<>();
        this.mAnimationState = 0;
        this.mGestureState = 0;
        this.mRetract = new Runnable() {
            /* class com.google.android.systemui.assist.OpaLayout.C15511 */

            public void run() {
                OpaLayout.this.cancelCurrentAnimation();
                OpaLayout.this.startRetractAnimation();
            }
        };
        this.mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() {
            /* class com.google.android.systemui.assist.OpaLayout.C15522 */

            public void onConnectionChanged(boolean z) {
                OpaLayout.this.updateOpaLayout();
            }
        };
        this.mDiamondAnimation = new Runnable() {
            /* class com.google.android.systemui.assist.$$Lambda$OpaLayout$FW1rmJcZbiemVKFJwNyO1Lz2ero */

            public final void run() {
                OpaLayout.this.lambda$new$1$OpaLayout();
            }
        };
    }

    public OpaLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mResources = getResources();
        this.mBlue = findViewById(R.id.blue);
        this.mRed = findViewById(R.id.red);
        this.mYellow = findViewById(R.id.yellow);
        this.mGreen = findViewById(R.id.green);
        this.mWhite = (ImageView) findViewById(R.id.white);
        this.mWhiteCutout = (ImageView) findViewById(R.id.white_cutout);
        this.mHalo = (ImageView) findViewById(R.id.halo);
        this.mHome = (KeyButtonView) findViewById(R.id.home_button);
        this.mHalo.setImageDrawable(KeyButtonDrawable.create(new ContextThemeWrapper(getContext(), R.style.DualToneLightTheme), new ContextThemeWrapper(getContext(), R.style.DualToneDarkTheme), R.drawable.halo, true, null));
        this.mHomeDiameter = this.mResources.getDimensionPixelSize(R.dimen.opa_disabled_home_diameter);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        this.mWhiteCutout.setLayerType(2, paint);
        this.mAnimatedViews.add(this.mBlue);
        this.mAnimatedViews.add(this.mRed);
        this.mAnimatedViews.add(this.mYellow);
        this.mAnimatedViews.add(this.mGreen);
        this.mAnimatedViews.add(this.mWhite);
        this.mAnimatedViews.add(this.mWhiteCutout);
        this.mAnimatedViews.add(this.mHalo);
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    }

    public void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        this.mWindowVisible = i == 0;
        if (i == 0) {
            updateOpaLayout();
            return;
        }
        cancelCurrentAnimation();
        skipToStartingValue();
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mHome.setOnLongClickListener(onLongClickListener);
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mHome.setOnTouchListener(onTouchListener);
    }

    public /* synthetic */ void lambda$new$1$OpaLayout() {
        if (this.mCurrentAnimators.isEmpty()) {
            startDiamondAnimation();
        }
    }

    // TODO: Clean up this code!
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!this.getOpaEnabled()) return false;
        if (!ValueAnimator.areAnimatorsEnabled()) return false;
        if (this.mGestureState != 0) {
            return false;
        }
        int n = motionEvent.getAction();
        if (n != 0) {
            if (n != 1) {
                if (n != 2) {
                    if (n != 3) {
                        return false;
                    }
                } else {
                    float f = QuickStepContract.getQuickStepTouchSlopPx(this.getContext());
                    if (!(Math.abs(motionEvent.getRawX() - (float)this.mTouchDownX) > f)) {
                        if (!(Math.abs(motionEvent.getRawY() - (float)this.mTouchDownY) > f)) return false;
                    }
                    this.abortCurrentGesture();
                    return false;
                }
            }
            if (this.mDiamondAnimationDelayed) {
                if (this.mIsPressed) {
                    this.postDelayed(this.mRetract, 200L);
                }
            } else {
                if (this.mAnimationState == 1) {
                    long l = SystemClock.elapsedRealtime();
                    long l2 = this.mStartTime;
                    this.removeCallbacks(this.mRetract);
                    this.postDelayed(this.mRetract, 100L - (l - l2));
                    this.removeCallbacks(this.mDiamondAnimation);
                    this.cancelLongPress();
                    return false;
                }
                if (this.mIsPressed) {
                    this.mRetract.run();
                }
            }
            this.mIsPressed = false;
            return false;
        }
        this.mTouchDownX = (int)motionEvent.getRawX();
        this.mTouchDownY = (int)motionEvent.getRawY();
        if (!this.mCurrentAnimators.isEmpty()) {
            if (this.mAnimationState != 2) return false;
            this.endCurrentAnimation();
            n = 1;
        } else {
            n = 0;
        }
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mIsPressed = true;
        this.removeCallbacks(this.mDiamondAnimation);
        this.removeCallbacks(this.mRetract);
        if (this.mDelayTouchFeedback && n == 0) {
            this.mDiamondAnimationDelayed = true;
            this.postDelayed(this.mDiamondAnimation, (long)ViewConfiguration.getTapTimeout());
            return false;
        }
        this.mDiamondAnimationDelayed = false;
        this.startDiamondAnimation();
        return false;
    }

    public void setAccessibilityDelegate(View.AccessibilityDelegate accessibilityDelegate) {
        super.setAccessibilityDelegate(accessibilityDelegate);
        this.mHome.setAccessibilityDelegate(accessibilityDelegate);
    }

    public void setImageDrawable(Drawable drawable) {
        this.mWhite.setImageDrawable(drawable);
        this.mWhiteCutout.setImageDrawable(drawable);
    }

    public void abortCurrentGesture() {
        this.mHome.abortCurrentGesture();
        this.mIsPressed = false;
        this.mDiamondAnimationDelayed = false;
        removeCallbacks(this.mDiamondAnimation);
        cancelLongPress();
        int i = this.mAnimationState;
        if (i == 3 || i == 1) {
            this.mRetract.run();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateOpaLayout();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mOverviewProxyService.addCallback(this.mOverviewProxyListener);
        this.mOpaEnabledNeedsUpdate = true;
        post(new Runnable() {
            /* class com.google.android.systemui.assist.$$Lambda$qadRDAXGXctZjQfVlEtWjxSCCE */

            public final void run() {
                OpaLayout.this.getOpaEnabled();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mOverviewProxyService.removeCallback(this.mOverviewProxyListener);
    }

    private void startDiamondAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            setDotsVisible();
            this.mCurrentAnimators.addAll((ArraySet) getDiamondAnimatorSet());
            this.mAnimationState = 1;
            startAll(this.mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    /* access modifiers changed from: private */
    public void startRetractAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet) getRetractAnimatorSet());
            this.mAnimationState = 2;
            startAll(this.mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    /* access modifiers changed from: private */
    public void startLineAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet) getLineAnimatorSet());
            this.mAnimationState = 3;
            startAll(this.mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    /* access modifiers changed from: private */
    public void startCollapseAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet) getCollapseAnimatorSet());
            this.mAnimationState = 3;
            startAll(this.mCurrentAnimators);
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
        return isAttachedToWindow() && this.mWindowVisible;
    }

    private ArraySet<Animator> getDiamondAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        arraySet.add(OpaUtils.getDeltaAnimatorY(this.mTop, this.mDiamondInterpolator, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mTop, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mTop, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getDeltaAnimatorY(this.mBottom, this.mDiamondInterpolator, OpaUtils.getPxVal(this.mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mBottom, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mBottom, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getDeltaAnimatorX(this.mLeft, this.mDiamondInterpolator, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mLeft, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mLeft, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getDeltaAnimatorX(this.mRight, this.mDiamondInterpolator, OpaUtils.getPxVal(this.mResources, R.dimen.opa_diamond_translation), 200));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mRight, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mRight, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mWhite, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mWhite, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mWhiteCutout, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mWhiteCutout, 0.625f, 200, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mHalo, 0.47619048f, 100, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mHalo, 0.47619048f, 100, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getAlphaAnimator(this.mHalo, 0.0f, 100, Interpolators.FAST_OUT_SLOW_IN));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.OpaLayout.C15533 */

            public void onAnimationCancel(Animator animator) {
                OpaLayout.this.mCurrentAnimators.clear();
            }

            public void onAnimationEnd(Animator animator) {
                OpaLayout.this.startLineAnimation();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getRetractAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mRed, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(this.mRed, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mRed, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mRed, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mBlue, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(this.mBlue, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mBlue, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mBlue, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mGreen, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(this.mGreen, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mGreen, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mGreen, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getTranslationAnimatorX(this.mYellow, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getTranslationAnimatorY(this.mYellow, OpaUtils.INTERPOLATOR_40_OUT, 190));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mYellow, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mYellow, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mWhite, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mWhite, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mWhiteCutout, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mWhiteCutout, 1.0f, 190, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mHalo, 1.0f, 190, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mHalo, 1.0f, 190, Interpolators.FAST_OUT_SLOW_IN));
        arraySet.add(OpaUtils.getAlphaAnimator(this.mHalo, 1.0f, 190, Interpolators.FAST_OUT_SLOW_IN));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.OpaLayout.C15544 */

            public void onAnimationEnd(Animator animator) {
                OpaLayout.this.mCurrentAnimators.clear();
                OpaLayout.this.skipToStartingValue();
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
        if (this.mIsVertical) {
            animator = OpaUtils.getTranslationAnimatorY(this.mRed, OpaUtils.INTERPOLATOR_40_OUT, 133);
        } else {
            animator = OpaUtils.getTranslationAnimatorX(this.mRed, OpaUtils.INTERPOLATOR_40_OUT, 133);
        }
        arraySet.add(animator);
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mRed, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mRed, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        if (this.mIsVertical) {
            animator2 = OpaUtils.getTranslationAnimatorY(this.mBlue, OpaUtils.INTERPOLATOR_40_OUT, 150);
        } else {
            animator2 = OpaUtils.getTranslationAnimatorX(this.mBlue, OpaUtils.INTERPOLATOR_40_OUT, 150);
        }
        arraySet.add(animator2);
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mBlue, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mBlue, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        if (this.mIsVertical) {
            animator3 = OpaUtils.getTranslationAnimatorY(this.mYellow, OpaUtils.INTERPOLATOR_40_OUT, 133);
        } else {
            animator3 = OpaUtils.getTranslationAnimatorX(this.mYellow, OpaUtils.INTERPOLATOR_40_OUT, 133);
        }
        arraySet.add(animator3);
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mYellow, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mYellow, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        if (this.mIsVertical) {
            animator4 = OpaUtils.getTranslationAnimatorY(this.mGreen, OpaUtils.INTERPOLATOR_40_OUT, 150);
        } else {
            animator4 = OpaUtils.getTranslationAnimatorX(this.mGreen, OpaUtils.INTERPOLATOR_40_OUT, 150);
        }
        arraySet.add(animator4);
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mGreen, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mGreen, 1.0f, 200, OpaUtils.INTERPOLATOR_40_OUT));
        Animator scaleAnimatorX = OpaUtils.getScaleAnimatorX(this.mWhite, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorY = OpaUtils.getScaleAnimatorY(this.mWhite, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorX2 = OpaUtils.getScaleAnimatorX(this.mWhiteCutout, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorY2 = OpaUtils.getScaleAnimatorY(this.mWhiteCutout, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorX3 = OpaUtils.getScaleAnimatorX(this.mHalo, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator scaleAnimatorY3 = OpaUtils.getScaleAnimatorY(this.mHalo, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
        Animator alphaAnimator = OpaUtils.getAlphaAnimator(this.mHalo, 1.0f, 150, Interpolators.FAST_OUT_SLOW_IN);
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
            /* class com.google.android.systemui.assist.OpaLayout.C15555 */

            public void onAnimationEnd(Animator animator) {
                OpaLayout.this.mCurrentAnimators.clear();
                OpaLayout.this.skipToStartingValue();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getLineAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        if (this.mIsVertical) {
            arraySet.add(OpaUtils.getDeltaAnimatorY(this.mRed, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorX(this.mRed, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorY(this.mBlue, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorY(this.mYellow, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorX(this.mYellow, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorY(this.mGreen, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), 225));
        } else {
            arraySet.add(OpaUtils.getDeltaAnimatorX(this.mRed, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorY(this.mRed, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorX(this.mBlue, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorX(this.mYellow, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), 225));
            arraySet.add(OpaUtils.getDeltaAnimatorY(this.mYellow, Interpolators.FAST_OUT_SLOW_IN, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_y_translation), 133));
            arraySet.add(OpaUtils.getDeltaAnimatorX(this.mGreen, Interpolators.FAST_OUT_SLOW_IN, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), 225));
        }
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mWhite, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mWhite, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mWhiteCutout, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mWhiteCutout, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorX(this.mHalo, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(OpaUtils.getScaleAnimatorY(this.mHalo, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.OpaLayout.C15566 */

            public void onAnimationEnd(Animator animator) {
                OpaLayout.this.startCollapseAnimation();
            }

            public void onAnimationCancel(Animator animator) {
                OpaLayout.this.mCurrentAnimators.clear();
            }
        });
        return arraySet;
    }

    public boolean getOpaEnabled() {
        if (this.mOpaEnabledNeedsUpdate) {
            ((AssistManagerGoogle) Dependency.get(AssistManager.class)).dispatchOpaEnabledState();
            if (this.mOpaEnabledNeedsUpdate) {
                Log.w("OpaLayout", "mOpaEnabledNeedsUpdate not cleared by AssistManagerGoogle!");
            }
        }
        return this.mOpaEnabled;
    }

    public void setOpaEnabled(boolean z) {
        Log.i("OpaLayout", "Setting opa enabled to " + z);
        this.mOpaEnabled = z;
        this.mOpaEnabledNeedsUpdate = false;
        updateOpaLayout();
    }

    public void updateOpaLayout() {
        boolean shouldShowSwipeUpUI = this.mOverviewProxyService.shouldShowSwipeUpUI();
        boolean z = true;
        boolean z2 = this.mOpaEnabled && !shouldShowSwipeUpUI;
        this.mHalo.setVisibility(z2 ? 0 : 4);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mHalo.getLayoutParams();
        if (z2 || shouldShowSwipeUpUI) {
            z = false;
        }
        int i = z ? this.mHomeDiameter : -1;
        layoutParams.width = i;
        layoutParams.height = i;
        this.mWhite.setLayoutParams(layoutParams);
        this.mWhiteCutout.setLayoutParams(layoutParams);
        ImageView.ScaleType scaleType = z ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER;
        this.mWhite.setScaleType(scaleType);
        this.mWhiteCutout.setScaleType(scaleType);
    }

    /* access modifiers changed from: private */
    public void cancelCurrentAnimation() {
        if (!this.mCurrentAnimators.isEmpty()) {
            for (int size = this.mCurrentAnimators.size() - 1; size >= 0; size--) {
                Animator valueAt = this.mCurrentAnimators.valueAt(size);
                valueAt.removeAllListeners();
                valueAt.cancel();
            }
            this.mCurrentAnimators.clear();
            this.mAnimationState = 0;
        }
        AnimatorSet animatorSet = this.mGestureAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.mGestureState = 0;
        }
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
        this.mAnimationState = 0;
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
        int size = this.mAnimatedViews.size();
        for (int i = 0; i < size; i++) {
            this.mAnimatedViews.get(i).setAlpha(1.0f);
        }
    }

    /* access modifiers changed from: private */
    public void skipToStartingValue() {
        int size = this.mAnimatedViews.size();
        for (int i = 0; i < size; i++) {
            View view = this.mAnimatedViews.get(i);
            view.setScaleY(1.0f);
            view.setScaleX(1.0f);
            view.setTranslationY(0.0f);
            view.setTranslationX(0.0f);
            view.setAlpha(0.0f);
        }
        this.mHalo.setAlpha(1.0f);
        this.mWhite.setAlpha(1.0f);
        this.mWhiteCutout.setAlpha(1.0f);
        this.mAnimationState = 0;
        this.mGestureState = 0;
    }

    public void setVertical(boolean z) {
        AnimatorSet animatorSet;
        if (!(this.mIsVertical == z || (animatorSet = this.mGestureAnimatorSet) == null)) {
            animatorSet.cancel();
            this.mGestureAnimatorSet = null;
            skipToStartingValue();
        }
        this.mIsVertical = z;
        this.mHome.setVertical(z);
        if (this.mIsVertical) {
            this.mTop = this.mGreen;
            this.mBottom = this.mBlue;
            this.mRight = this.mYellow;
            this.mLeft = this.mRed;
            return;
        }
        this.mTop = this.mRed;
        this.mBottom = this.mYellow;
        this.mLeft = this.mBlue;
        this.mRight = this.mGreen;
    }

    public void setDarkIntensity(float f) {
        if (this.mWhite.getDrawable() instanceof KeyButtonDrawable) {
            ((KeyButtonDrawable) this.mWhite.getDrawable()).setDarkIntensity(f);
        }
        ((KeyButtonDrawable) this.mHalo.getDrawable()).setDarkIntensity(f);
        this.mWhite.invalidate();
        this.mHalo.invalidate();
        this.mHome.setDarkIntensity(f);
    }

    public void setDelayTouchFeedback(boolean z) {
        this.mHome.setDelayTouchFeedback(z);
        this.mDelayTouchFeedback = z;
    }

    public void onRelease() {
        if (this.mAnimationState == 0 && this.mGestureState == 1) {
            AnimatorSet animatorSet = this.mGestureAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            this.mGestureState = 0;
            startRetractAnimation();
        }
    }

    public void onProgress(float f, int i) {
        if (this.mGestureState != 2 && allowAnimations()) {
            if (this.mAnimationState == 2) {
                endCurrentAnimation();
            }
            if (this.mAnimationState == 0) {
                if (this.mGestureAnimatorSet == null) {
                    this.mGestureAnimatorSet = getGestureAnimatorSet();
                    this.mGestureAnimationSetDuration = this.mGestureAnimatorSet.getTotalDuration();
                }
                this.mGestureAnimatorSet.setCurrentPlayTime((long) (((float) (this.mGestureAnimationSetDuration - 1)) * f));
                if (f == 0.0f) {
                    this.mGestureState = 0;
                } else {
                    this.mGestureState = 1;
                }
            }
        }
    }

    public void onResolve(GestureSensor.DetectionProperties detectionProperties) {
        AnimatorSet animatorSet;
        if (this.mAnimationState == 0) {
            if (this.mGestureState != 1 || (animatorSet = this.mGestureAnimatorSet) == null || animatorSet.isStarted()) {
                skipToStartingValue();
                return;
            }
            this.mGestureAnimatorSet.start();
            this.mGestureState = 2;
        }
    }

    private AnimatorSet getGestureAnimatorSet() {
        AnimatorSet animatorSet = this.mGestureLineSet;
        if (animatorSet != null) {
            animatorSet.removeAllListeners();
            this.mGestureLineSet.cancel();
            return this.mGestureLineSet;
        }
        this.mGestureLineSet = new AnimatorSet();
        ObjectAnimator scaleObjectAnimator = OpaUtils.getScaleObjectAnimator(this.mWhite, 0.0f, 100, OpaUtils.INTERPOLATOR_40_OUT);
        ObjectAnimator scaleObjectAnimator2 = OpaUtils.getScaleObjectAnimator(this.mWhiteCutout, 0.0f, 100, OpaUtils.INTERPOLATOR_40_OUT);
        ObjectAnimator scaleObjectAnimator3 = OpaUtils.getScaleObjectAnimator(this.mHalo, 0.0f, 100, OpaUtils.INTERPOLATOR_40_OUT);
        scaleObjectAnimator.setStartDelay(50);
        scaleObjectAnimator2.setStartDelay(50);
        this.mGestureLineSet.play(scaleObjectAnimator).with(scaleObjectAnimator2).with(scaleObjectAnimator3);
        this.mGestureLineSet.play(OpaUtils.getScaleObjectAnimator(this.mTop, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN)).with(scaleObjectAnimator).with(OpaUtils.getAlphaObjectAnimator(this.mRed, 1.0f, 50, 130, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(this.mYellow, 1.0f, 50, 130, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(this.mBlue, 1.0f, 50, 113, Interpolators.LINEAR)).with(OpaUtils.getAlphaObjectAnimator(this.mGreen, 1.0f, 50, 113, Interpolators.LINEAR)).with(OpaUtils.getScaleObjectAnimator(this.mBottom, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN)).with(OpaUtils.getScaleObjectAnimator(this.mLeft, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN)).with(OpaUtils.getScaleObjectAnimator(this.mRight, 0.8f, 200, Interpolators.FAST_OUT_SLOW_IN));
        if (this.mIsVertical) {
            ObjectAnimator translationObjectAnimatorY = OpaUtils.getTranslationObjectAnimatorY(this.mRed, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), this.mRed.getY() + OpaUtils.getDeltaDiamondPositionLeftY(), 350);
            translationObjectAnimatorY.addListener(new AnimatorListenerAdapter() {
                /* class com.google.android.systemui.assist.OpaLayout.C15577 */

                public void onAnimationEnd(Animator animator) {
                    OpaLayout.this.startCollapseAnimation();
                }
            });
            this.mGestureLineSet.play(translationObjectAnimatorY).with(scaleObjectAnimator3).with(OpaUtils.getTranslationObjectAnimatorY(this.mBlue, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), this.mBlue.getY() + OpaUtils.getDeltaDiamondPositionBottomY(this.mResources), 350)).with(OpaUtils.getTranslationObjectAnimatorY(this.mYellow, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), this.mYellow.getY() + OpaUtils.getDeltaDiamondPositionRightY(), 350)).with(OpaUtils.getTranslationObjectAnimatorY(this.mGreen, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), this.mGreen.getY() + OpaUtils.getDeltaDiamondPositionTopY(this.mResources), 350));
        } else {
            ObjectAnimator translationObjectAnimatorX = OpaUtils.getTranslationObjectAnimatorX(this.mRed, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), this.mRed.getX() + OpaUtils.getDeltaDiamondPositionTopX(), 350);
            translationObjectAnimatorX.addListener(new AnimatorListenerAdapter() {
                /* class com.google.android.systemui.assist.OpaLayout.C15588 */

                public void onAnimationEnd(Animator animator) {
                    OpaLayout.this.startCollapseAnimation();
                }
            });
            this.mGestureLineSet.play(translationObjectAnimatorX).with(scaleObjectAnimator).with(OpaUtils.getTranslationObjectAnimatorX(this.mBlue, OpaUtils.INTERPOLATOR_40_40, -OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), this.mBlue.getX() + OpaUtils.getDeltaDiamondPositionLeftX(this.mResources), 350)).with(OpaUtils.getTranslationObjectAnimatorX(this.mYellow, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_ry), this.mYellow.getX() + OpaUtils.getDeltaDiamondPositionBottomX(), 350)).with(OpaUtils.getTranslationObjectAnimatorX(this.mGreen, OpaUtils.INTERPOLATOR_40_40, OpaUtils.getPxVal(this.mResources, R.dimen.opa_line_x_trans_bg), this.mGreen.getX() + OpaUtils.getDeltaDiamondPositionRightX(this.mResources), 350));
        }
        return this.mGestureLineSet;
    }
}

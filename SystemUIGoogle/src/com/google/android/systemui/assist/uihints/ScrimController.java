package com.google.android.systemui.assist.uihints;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import com.android.systemui.R;
import com.android.systemui.assist.PhenotypeHelper;
import com.google.android.systemui.assist.uihints.TranscriptionController;

public class ScrimController implements TranscriptionController.TranscriptionSpaceListener {
    private static final LinearInterpolator ALPHA_INTERPOLATOR = new LinearInterpolator();
    private ValueAnimator mAlphaAnimator;
    private boolean mCardForcesScrimGone = false;
    private boolean mCardTransitionAnimated = false;
    private boolean mCardVisible = false;
    private View mFullscreenScrim;
    private boolean mHaveAccurateLightness = false;
    private boolean mInFullListening = false;
    private float mInvocationProgress = 0.0f;
    private boolean mIsDozing = false;
    private final LightnessProvider mLightnessProvider;
    private float mMedianLightness;
    private final OverlappedElementController mOverlappedElement;
    private final PhenotypeHelper mPhenotypeHelper = new PhenotypeHelper();
    private final View mRoot;
    private boolean mTranscriptionVisible = false;
    private final VisibilityListener mVisibilityListener;

    public ScrimController(Context context, ViewGroup viewGroup, LightnessProvider lightnessProvider, VisibilityListener visibilityListener, Runnable runnable) {
        mRoot = LayoutInflater.from(context).inflate(R.layout.scrim_view, viewGroup, false);
        mRoot.setBackgroundTintBlendMode(BlendMode.SRC_IN);
        mFullscreenScrim = new View(context);
        mFullscreenScrim.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        mFullscreenScrim.setContentDescription(context.getString(R.string.assistant_scrim_label));
        mFullscreenScrim.setVisibility(8);
        mLightnessProvider = lightnessProvider;
        mVisibilityListener = visibilityListener;
        mRoot.setOnClickListener(view -> runnable.run());
        mFullscreenScrim.setOnClickListener(view -> runnable.run());
        mOverlappedElement = new OverlappedElementController(context);
        viewGroup.addView(mFullscreenScrim);
        viewGroup.addView(mRoot);
    }

    public Rect getTouchableRegion() {
        Rect rect = new Rect();
        if (mFullscreenScrim.getVisibility() == 0) {
            mFullscreenScrim.getBoundsOnScreen(rect);
        } else {
            mRoot.getBoundsOnScreen(rect);
            rect.top = rect.bottom - mRoot.getContext().getResources().getDimensionPixelSize(R.dimen.scrim_touchable_height);
        }
        return rect;
    }

    public boolean isVisible() {
        return mRoot.getVisibility() == 0;
    }

    public IBinder getSurfaceControllerHandle() {
        View view = mFullscreenScrim.getVisibility() == 0 ? mFullscreenScrim : mRoot;
        if (view.getViewRootImpl() == null) {
            return null;
        }
        return view.getViewRootImpl().getSurfaceControl().getHandle();
    }

    public void onStateChanged(TranscriptionController.State state, TranscriptionController.State state2) {
        boolean z = state2 != TranscriptionController.State.NONE;
        if (mTranscriptionVisible != z) {
            mTranscriptionVisible = z;
            refresh();
        }
    }

    public void setCardVisible(boolean z, boolean z2, boolean z3) {
        mCardVisible = z;
        mCardTransitionAnimated = z2;
        mCardForcesScrimGone = z3;
        refresh();
    }

    public void setInvocationProgress(float f) {
        float constrain = MathUtils.constrain(f, 0.0f, 1.0f);
        if (mInvocationProgress != constrain) {
            mInvocationProgress = constrain;
            refresh();
        }
    }

    public void setInFullListening(boolean z) {
        mInFullListening = z;
        refresh();
        mRoot.sendAccessibilityEvent(8);
    }

    public void setIsDozing(boolean z) {
        mIsDozing = z;
        refresh();
    }

    public void setMedianLightness(float f) {
        mHaveAccurateLightness = true;
        mMedianLightness = f;
        refresh();
    }

    public void onLightnessInvalidated() {
        mHaveAccurateLightness = false;
        refresh();
    }

    void refresh() {
        if (!mHaveAccurateLightness || mIsDozing) {
            setRelativeAlpha(0.0f, false);
        } else if (mCardVisible && mCardForcesScrimGone) {
            setRelativeAlpha(0.0f, mCardTransitionAnimated);
        } else if (mInFullListening || mTranscriptionVisible) {
            if (!mCardVisible || isVisible()) {
                setRelativeAlpha(1.0f, false);
            } else {
                setRelativeAlpha(0.0f, mCardTransitionAnimated);
            }
        } else if (mCardVisible) {
            setRelativeAlpha(0.0f, mCardTransitionAnimated);
        } else {
            float f = mInvocationProgress;
            if (f > 0.0f) {
                setRelativeAlpha(Math.min(1.0f, f), false);
            } else {
                setRelativeAlpha(0.0f, true);
            }
        }
    }

    public boolean isDark() {
        return mMedianLightness <= NgaUiController.getDarkUiThreshold();
    }

    protected void setRelativeAlpha(float f, boolean z) {
        setAlphaAnimator(null);
        if (!mHaveAccurateLightness && f > 0.0f) {
            return;
        }
        if (f > 0.0f) {
            if (mRoot.getVisibility() != 0) {
                mLightnessProvider.setMuted(true);
                updateColor();
                setVisibility(0);
            }
            if (z) {
                setAlphaAnimator(createRelativeAlphaAnimator(f));
            } else {
                setAlpha(f * 1.0f);
            }
        } else if (z) {
            ValueAnimator createRelativeAlphaAnimator = createRelativeAlphaAnimator(f);
            createRelativeAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled = false;

                public void onAnimationCancel(Animator animator) {
                    super.onAnimationCancel(animator);
                    mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (!mCancelled) {
                        mLightnessProvider.setMuted(false);
                        setVisibility(8);
                    }
                }
            });
            setAlphaAnimator(createRelativeAlphaAnimator);
        } else {
            setAlpha(f);
            mLightnessProvider.setMuted(false);
            setVisibility(8);
        }
    }

    private boolean shouldShowFullScreenScrim() {
        return !mPhenotypeHelper.getBoolean("assist_tap_passthrough", true);
    }

    private void setVisibility(int i) {
        if (i != mRoot.getVisibility()) {
            mRoot.setVisibility(i);
            if (shouldShowFullScreenScrim() || i == 8) {
                mFullscreenScrim.setVisibility(i);
            }
            mVisibilityListener.onVisibilityChanged(mRoot.getVisibility());
            if (i != 0) {
                mOverlappedElement.setAlpha(1.0f, false);
                refresh();
            }
            mRoot.setBackground(mRoot.getVisibility() == 0 ? mRoot.getContext().getDrawable(R.drawable.scrim_strip) : null);
        }
    }

    private void setAlpha(float f) {
        mRoot.setAlpha(f);
        mFullscreenScrim.setAlpha(f);
        mOverlappedElement.setAlpha(1.0f - f, false);
    }

    private ValueAnimator createRelativeAlphaAnimator(float f) {
        ValueAnimator duration = ValueAnimator.ofFloat(mRoot.getAlpha(), f * 1.0f).setDuration((long) ((Math.abs(f - mRoot.getAlpha()) / 1.0f) * 300.0f));
        duration.setInterpolator(ALPHA_INTERPOLATOR);
        duration.addUpdateListener(valueAnimator -> setAlpha((Float) valueAnimator.getAnimatedValue()));
        return duration;
    }

    private void setAlphaAnimator(ValueAnimator valueAnimator) {
        if (mAlphaAnimator != null && mAlphaAnimator.isRunning()) {
            mAlphaAnimator.cancel();
        }
        mAlphaAnimator = valueAnimator;
        if (mAlphaAnimator != null) {
            mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    setAlphaAnimator(null);
                }
            });
            mAlphaAnimator.start();
        }
    }

    private void updateColor() {
        mRoot.setBackgroundTintList(ColorStateList.valueOf(isDark() ? -16777216 : -1));
    }
}

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
import com.android.systemui.C1733R$dimen;
import com.android.systemui.C1734R$drawable;
import com.android.systemui.C1737R$layout;
import com.android.systemui.C1741R$string;
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
    /* access modifiers changed from: private */
    public final LightnessProvider mLightnessProvider;
    private float mMedianLightness;
    private final OverlappedElementController mOverlappedElement;
    private final PhenotypeHelper mPhenotypeHelper = new PhenotypeHelper();
    private final View mRoot;
    private boolean mTranscriptionVisible = false;
    private final VisibilityListener mVisibilityListener;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public ScrimController(Context context, ViewGroup viewGroup, LightnessProvider lightnessProvider, VisibilityListener visibilityListener, Runnable runnable) {
        this.mRoot = LayoutInflater.from(context).inflate(C1737R$layout.scrim_view, viewGroup, false);
        this.mRoot.setBackgroundTintBlendMode(BlendMode.SRC_IN);
        this.mFullscreenScrim = new View(context);
        this.mFullscreenScrim.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.mFullscreenScrim.setContentDescription(context.getString(C1741R$string.assistant_scrim_label));
        this.mFullscreenScrim.setVisibility(8);
        this.mLightnessProvider = lightnessProvider;
        this.mVisibilityListener = visibilityListener;
        $$Lambda$ScrimController$sdFC7TSkhSEX7D4ytsO0cJbxEX0 r6 = new View.OnClickListener(runnable) {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$ScrimController$sdFC7TSkhSEX7D4ytsO0cJbxEX0 */
            private final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            public final void onClick(View view) {
                this.f$0.run();
            }
        };
        this.mRoot.setOnClickListener(r6);
        this.mFullscreenScrim.setOnClickListener(r6);
        this.mOverlappedElement = new OverlappedElementController(context);
        viewGroup.addView(this.mFullscreenScrim);
        viewGroup.addView(this.mRoot);
    }

    public Rect getTouchableRegion() {
        Rect rect = new Rect();
        if (this.mFullscreenScrim.getVisibility() == 0) {
            this.mFullscreenScrim.getBoundsOnScreen(rect);
        } else {
            this.mRoot.getBoundsOnScreen(rect);
            rect.top = rect.bottom - this.mRoot.getContext().getResources().getDimensionPixelSize(C1733R$dimen.scrim_touchable_height);
        }
        return rect;
    }

    public boolean isVisible() {
        return this.mRoot.getVisibility() == 0;
    }

    public IBinder getSurfaceControllerHandle() {
        View view = this.mFullscreenScrim.getVisibility() == 0 ? this.mFullscreenScrim : this.mRoot;
        if (view.getViewRootImpl() == null) {
            return null;
        }
        return view.getViewRootImpl().getSurfaceControl().getHandle();
    }

    public void onStateChanged(TranscriptionController.State state, TranscriptionController.State state2) {
        boolean z = state2 != TranscriptionController.State.NONE;
        if (this.mTranscriptionVisible != z) {
            this.mTranscriptionVisible = z;
            refresh();
        }
    }

    public void setCardVisible(boolean z, boolean z2, boolean z3) {
        this.mCardVisible = z;
        this.mCardTransitionAnimated = z2;
        this.mCardForcesScrimGone = z3;
        refresh();
    }

    public void setInvocationProgress(float f) {
        float constrain = MathUtils.constrain(f, 0.0f, 1.0f);
        if (this.mInvocationProgress != constrain) {
            this.mInvocationProgress = constrain;
            refresh();
        }
    }

    public void setInFullListening(boolean z) {
        this.mInFullListening = z;
        refresh();
        this.mRoot.sendAccessibilityEvent(8);
    }

    public void setIsDozing(boolean z) {
        this.mIsDozing = z;
        refresh();
    }

    public void setMedianLightness(float f) {
        this.mHaveAccurateLightness = true;
        this.mMedianLightness = f;
        refresh();
    }

    public void onLightnessInvalidated() {
        this.mHaveAccurateLightness = false;
        refresh();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.min(float, float):float}
     arg types: [int, float]
     candidates:
      ClspMth{java.lang.Math.min(double, double):double}
      ClspMth{java.lang.Math.min(long, long):long}
      ClspMth{java.lang.Math.min(int, int):int}
      ClspMth{java.lang.Math.min(float, float):float} */
    /* access modifiers changed from: package-private */
    public void refresh() {
        if (!this.mHaveAccurateLightness || this.mIsDozing) {
            setRelativeAlpha(0.0f, false);
        } else if (this.mCardVisible && this.mCardForcesScrimGone) {
            setRelativeAlpha(0.0f, this.mCardTransitionAnimated);
        } else if (this.mInFullListening || this.mTranscriptionVisible) {
            if (!this.mCardVisible || isVisible()) {
                setRelativeAlpha(1.0f, false);
            } else {
                setRelativeAlpha(0.0f, this.mCardTransitionAnimated);
            }
        } else if (this.mCardVisible) {
            setRelativeAlpha(0.0f, this.mCardTransitionAnimated);
        } else {
            float f = this.mInvocationProgress;
            if (f > 0.0f) {
                setRelativeAlpha(Math.min(1.0f, f), false);
            } else {
                setRelativeAlpha(0.0f, true);
            }
        }
    }

    public boolean isDark() {
        return this.mMedianLightness <= NgaUiController.getDarkUiThreshold();
    }

    /* access modifiers changed from: protected */
    public void setRelativeAlpha(float f, boolean z) {
        setAlphaAnimator(null);
        if (!this.mHaveAccurateLightness && f > 0.0f) {
            return;
        }
        if (f > 0.0f) {
            if (this.mRoot.getVisibility() != 0) {
                this.mLightnessProvider.setMuted(true);
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
                /* class com.google.android.systemui.assist.uihints.ScrimController.C15681 */
                private boolean mCancelled = false;

                public void onAnimationCancel(Animator animator) {
                    super.onAnimationCancel(animator);
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (!this.mCancelled) {
                        ScrimController.this.mLightnessProvider.setMuted(false);
                        ScrimController.this.setVisibility(8);
                    }
                }
            });
            setAlphaAnimator(createRelativeAlphaAnimator);
        } else {
            setAlpha(f);
            this.mLightnessProvider.setMuted(false);
            setVisibility(8);
        }
    }

    private boolean shouldShowFullScreenScrim() {
        return !this.mPhenotypeHelper.getBoolean("assist_tap_passthrough", true);
    }

    /* access modifiers changed from: private */
    public void setVisibility(int i) {
        if (i != this.mRoot.getVisibility()) {
            this.mRoot.setVisibility(i);
            if (shouldShowFullScreenScrim() || i == 8) {
                this.mFullscreenScrim.setVisibility(i);
            }
            this.mVisibilityListener.onVisibilityChanged(this.mRoot.getVisibility());
            if (i != 0) {
                this.mOverlappedElement.setAlpha(1.0f, false);
                refresh();
            }
            View view = this.mRoot;
            view.setBackground(view.getVisibility() == 0 ? this.mRoot.getContext().getDrawable(C1734R$drawable.scrim_strip) : null);
        }
    }

    private void setAlpha(float f) {
        this.mRoot.setAlpha(f);
        this.mFullscreenScrim.setAlpha(f);
        this.mOverlappedElement.setAlpha(1.0f - f, false);
    }

    private ValueAnimator createRelativeAlphaAnimator(float f) {
        ValueAnimator duration = ValueAnimator.ofFloat(this.mRoot.getAlpha(), f * 1.0f).setDuration((long) ((Math.abs(f - this.mRoot.getAlpha()) / 1.0f) * 300.0f));
        duration.setInterpolator(ALPHA_INTERPOLATOR);
        duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$ScrimController$55tqo6T7cBCnmVcDXpkt5aUf8 */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ScrimController.this.lambda$createRelativeAlphaAnimator$1$ScrimController(valueAnimator);
            }
        });
        return duration;
    }

    public /* synthetic */ void lambda$createRelativeAlphaAnimator$1$ScrimController(ValueAnimator valueAnimator) {
        setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* access modifiers changed from: private */
    public void setAlphaAnimator(ValueAnimator valueAnimator) {
        ValueAnimator valueAnimator2 = this.mAlphaAnimator;
        if (valueAnimator2 != null && valueAnimator2.isRunning()) {
            this.mAlphaAnimator.cancel();
        }
        this.mAlphaAnimator = valueAnimator;
        ValueAnimator valueAnimator3 = this.mAlphaAnimator;
        if (valueAnimator3 != null) {
            valueAnimator3.addListener(new AnimatorListenerAdapter() {
                /* class com.google.android.systemui.assist.uihints.ScrimController.C15692 */

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    ScrimController.this.setAlphaAnimator(null);
                }
            });
            this.mAlphaAnimator.start();
        }
    }

    private void updateColor() {
        this.mRoot.setBackgroundTintList(ColorStateList.valueOf(isDark() ? -16777216 : -1));
    }
}

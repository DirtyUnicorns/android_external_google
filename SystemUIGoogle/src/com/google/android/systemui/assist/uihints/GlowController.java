package com.google.android.systemui.assist.uihints;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import com.android.systemui.C1733R$dimen;
import com.android.systemui.C1737R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.assist.p003ui.EdgeLight;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsListener;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;
import com.google.android.systemui.assist.uihints.edgelights.mode.FulfillBottom;
import com.google.android.systemui.assist.uihints.edgelights.mode.FullListening;
import com.google.android.systemui.assist.uihints.edgelights.mode.Gone;
import com.google.android.systemui.assist.uihints.edgelights.mode.HalfListening;

public final class GlowController implements EdgeLightsListener {
    /* access modifiers changed from: private */
    public ValueAnimator mAnimator = null;
    private boolean mCardVisible = false;
    private final Context mContext;
    private EdgeLight[] mEdgeLights = null;
    private EdgeLightsView.Mode mEdgeLightsMode = null;
    private final GlowView mGlowView;
    private int mGlowsY = 0;
    private int mGlowsYDestination = 0;
    private boolean mInvocationCompleting = false;
    private float mMedianLightness;
    private int mNavigationMode;
    private final ScrimController mScrimController;
    private RollingAverage mSpeechRolling = new RollingAverage(3);
    private final VisibilityListener mVisibilityListener;

    private enum GlowState {
        SHORT_DARK_BACKGROUND,
        SHORT_LIGHT_BACKGROUND,
        TALL_DARK_BACKGROUND,
        TALL_LIGHT_BACKGROUND,
        GONE
    }

    private float getGlowWidthToViewWidth() {
        return 0.55f;
    }

    private long getMaxYAnimationDuration() {
        return 400;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public GlowController(Context context, ViewGroup viewGroup, LightnessProvider lightnessProvider, VisibilityListener visibilityListener, Runnable runnable) {
        this.mContext = context;
        this.mVisibilityListener = visibilityListener;
        this.mNavigationMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(new NavigationModeController.ModeChangedListener() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$GlowController$pRUOKgBpKNbCOCs2BtGayinrRI */

            public final void onNavigationModeChanged(int i) {
                GlowController.this.lambda$new$0$GlowController(i);
            }
        });
        this.mGlowView = (GlowView) LayoutInflater.from(context).inflate(C1737R$layout.glow_view, viewGroup, false);
        GlowView glowView = this.mGlowView;
        int i = this.mGlowsY;
        glowView.setGlowsY(i, i, null);
        this.mScrimController = new ScrimController(context, viewGroup, lightnessProvider, visibilityListener, runnable);
        viewGroup.addView(this.mGlowView);
        this.mGlowView.setOnClickListener(new View.OnClickListener(runnable) {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$GlowController$ixkPkGyv60M5wYmEgyBLwQiO_Gg */
            private final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            public final void onClick(View view) {
                this.f$0.run();
            }
        });
        this.mGlowView.setGlowsY(getMinTranslationY(), getMinTranslationY(), null);
        this.mGlowView.setGlowWidthRatio(getGlowWidthToViewWidth());
    }

    public /* synthetic */ void lambda$new$0$GlowController(int i) {
        this.mNavigationMode = i;
    }

    public void setInvocationProgress(float f) {
        if (this.mEdgeLightsMode instanceof Gone) {
            setVisibility(f > 0.0f ? 0 : 8);
            this.mGlowView.setBlurRadius(getInvocationBlurRadius(f));
            this.mGlowsY = getInvocationTranslationY(f);
            int i = this.mGlowsY;
            this.mGlowsYDestination = i;
            this.mGlowView.setGlowsY(i, i, null);
            this.mGlowView.distributeEvenly();
        }
    }

    public void setCardVisible(boolean z) {
        this.mCardVisible = z;
    }

    public void setHasMedianLightness(float f) {
        this.mScrimController.setMedianLightness(f);
        this.mGlowView.setGlowsBlendMode(f <= NgaUiController.getDarkUiThreshold() ? PorterDuff.Mode.LIGHTEN : PorterDuff.Mode.SRC_OVER);
        this.mMedianLightness = f;
    }

    public boolean isVisible() {
        return isGlowVisible() || isScrimVisible();
    }

    private boolean isGlowVisible() {
        return this.mGlowView.getVisibility() == 0;
    }

    public boolean isScrimVisible() {
        return this.mScrimController.isVisible();
    }

    public ScrimController getScrimController() {
        return this.mScrimController;
    }

    public void onAudioLevelUpdate(float f, float f2) {
        this.mSpeechRolling.add(f);
        maybeAnimateForSpeechConfidence();
    }

    public IBinder getScrimSurfaceControllerHandle() {
        return this.mScrimController.getSurfaceControllerHandle();
    }

    private boolean shouldAnimateForSpeechConfidence() {
        EdgeLightsView.Mode mode = this.mEdgeLightsMode;
        if (!(mode instanceof HalfListening) && !(mode instanceof FullListening) && !(mode instanceof FulfillBottom)) {
            return false;
        }
        if (this.mSpeechRolling.getAverage() >= 0.30000001192092896d || this.mGlowsYDestination > getMinTranslationY()) {
            return true;
        }
        return false;
    }

    public void maybeAnimateForSpeechConfidence() {
        if (shouldAnimateForSpeechConfidence()) {
            animateGlowTranslationY((int) MathUtils.lerp((float) getMinTranslationY(), (float) getMaxTranslationY(), (float) this.mSpeechRolling.getAverage()));
        }
    }

    public Rect getTouchableRegion() {
        if (isScrimVisible()) {
            return this.mScrimController.getTouchableRegion();
        }
        if (this.mGlowView.getVisibility() != 0 || !QuickStepContract.isGesturalMode(this.mNavigationMode)) {
            return null;
        }
        Rect rect = new Rect();
        this.mGlowView.getBoundsOnScreen(rect);
        rect.top = rect.bottom - getMaxTranslationY();
        return rect;
    }

    /* access modifiers changed from: private */
    public GlowState getState() {
        boolean z;
        EdgeLightsView.Mode mode = this.mEdgeLightsMode;
        boolean z2 = (mode instanceof FulfillBottom) && !((FulfillBottom) mode).isListening();
        EdgeLightsView.Mode mode2 = this.mEdgeLightsMode;
        if ((mode2 instanceof Gone) || mode2 == null || z2) {
            return GlowState.GONE;
        }
        boolean z3 = !this.mCardVisible && (mode2 instanceof HalfListening);
        if (isScrimVisible()) {
            z = this.mScrimController.isDark();
        } else {
            z = this.mMedianLightness < NgaUiController.getDarkUiThreshold();
        }
        if (z3) {
            if (z) {
                return GlowState.SHORT_DARK_BACKGROUND;
            }
            return GlowState.SHORT_LIGHT_BACKGROUND;
        } else if (z) {
            return GlowState.TALL_DARK_BACKGROUND;
        } else {
            return GlowState.TALL_LIGHT_BACKGROUND;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.min(float, float):float}
     arg types: [int, float]
     candidates:
      ClspMth{java.lang.Math.min(double, double):double}
      ClspMth{java.lang.Math.min(long, long):long}
      ClspMth{java.lang.Math.min(int, int):int}
      ClspMth{java.lang.Math.min(float, float):float} */
    private int getInvocationBlurRadius(float f) {
        return (int) MathUtils.lerp((float) getBlurRadius(), (float) this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_tall_blur), Math.min(1.0f, f * 5.0f));
    }

    private int getInvocationTranslationY(float f) {
        return (int) MathUtils.min((int) MathUtils.lerp((float) getMinTranslationY(), (float) this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_tall_min_y), f), this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_invocation_max));
    }

    private int getBlurRadius() {
        if (getState() == GlowState.GONE) {
            return this.mGlowView.getBlurRadius();
        }
        if (getState() == GlowState.SHORT_DARK_BACKGROUND || getState() == GlowState.SHORT_LIGHT_BACKGROUND) {
            return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_short_blur);
        }
        if (getState() == GlowState.TALL_DARK_BACKGROUND || getState() == GlowState.TALL_LIGHT_BACKGROUND) {
            return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_tall_blur);
        }
        return 0;
    }

    private int getMinTranslationY() {
        if (getState() == GlowState.SHORT_DARK_BACKGROUND || getState() == GlowState.SHORT_LIGHT_BACKGROUND) {
            return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_short_min_y);
        }
        if (getState() == GlowState.TALL_DARK_BACKGROUND || getState() == GlowState.TALL_LIGHT_BACKGROUND) {
            return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_tall_min_y);
        }
        return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_gone_min_y);
    }

    private int getMaxTranslationY() {
        if (getState() == GlowState.SHORT_DARK_BACKGROUND || getState() == GlowState.SHORT_LIGHT_BACKGROUND) {
            return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_short_max_y);
        }
        if (getState() == GlowState.TALL_DARK_BACKGROUND || getState() == GlowState.TALL_LIGHT_BACKGROUND) {
            return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_tall_max_y);
        }
        return this.mContext.getResources().getDimensionPixelSize(C1733R$dimen.glow_gone_max_y);
    }

    public void onModeStarted(EdgeLightsView.Mode mode) {
        boolean z = mode instanceof Gone;
        if (!z || this.mEdgeLightsMode != null) {
            this.mInvocationCompleting = !z;
            this.mEdgeLightsMode = mode;
            if (z) {
                this.mSpeechRolling = new RollingAverage(3);
            }
            this.mScrimController.setInFullListening(mode instanceof FullListening);
            animateGlowTranslationY(getMinTranslationY());
            if (this.mEdgeLightsMode instanceof HalfListening) {
                this.mGlowView.sendAccessibilityEvent(8);
                return;
            }
            return;
        }
        this.mEdgeLightsMode = mode;
    }

    public void onAssistLightsUpdated(EdgeLightsView.Mode mode, EdgeLight[] edgeLightArr) {
        int i;
        if (!getTranslationYProportionalToEdgeLights()) {
            this.mEdgeLights = null;
            this.mGlowView.distributeEvenly();
            return;
        }
        this.mEdgeLights = edgeLightArr;
        if ((this.mInvocationCompleting && (mode instanceof Gone)) || !(mode instanceof FullListening)) {
            return;
        }
        if (edgeLightArr == null || edgeLightArr.length != 4) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected 4 lights, have ");
            if (edgeLightArr == null) {
                i = 0;
            } else {
                i = edgeLightArr.length;
            }
            sb.append(i);
            Log.e("GlowController", sb.toString());
            return;
        }
        maybeAnimateForSpeechConfidence();
    }

    private boolean getTranslationYProportionalToEdgeLights() {
        return this.mEdgeLightsMode instanceof FullListening;
    }

    private long getYAnimationDuration(float f) {
        return (long) Math.min((float) getMaxYAnimationDuration(), Math.abs(f) / ((float) (((long) Math.abs(getMaxTranslationY() - getMinTranslationY())) / getMaxYAnimationDuration())));
    }

    private void animateGlowTranslationY(int i) {
        animateGlowTranslationY(i, getYAnimationDuration((float) (i - this.mGlowsY)));
    }

    private void animateGlowTranslationY(int i, long j) {
        if (i == this.mGlowsYDestination) {
            this.mGlowView.setGlowsY(this.mGlowsY, getMinTranslationY(), getTranslationYProportionalToEdgeLights() ? this.mEdgeLights : null);
            return;
        }
        this.mGlowsYDestination = i;
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mAnimator = ValueAnimator.ofInt(this.mGlowsY, i);
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$GlowController$KJ4xLkdL203Fm1c4oyb2UQ5GB5Y */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlowController.this.lambda$animateGlowTranslationY$2$GlowController(valueAnimator);
            }
        });
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.uihints.GlowController.C15621 */

            public void onAnimationEnd(Animator animator) {
                ValueAnimator unused = GlowController.this.mAnimator = null;
                if (GlowState.GONE.equals(GlowController.this.getState())) {
                    GlowController.this.removeGlow();
                } else {
                    GlowController.this.maybeAnimateForSpeechConfidence();
                }
            }
        });
        this.mAnimator.setInterpolator(new LinearInterpolator());
        this.mAnimator.setDuration(j);
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this.mGlowView.getBlurRadius(), getBlurRadius()) {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$GlowController$C0e53S46AzBGKoKkZC0Zr8ygp3Q */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlowController.this.lambda$animateGlowTranslationY$3$GlowController(this.f$1, this.f$2, valueAnimator);
            }
        });
        float glowWidthRatio = this.mGlowView.getGlowWidthRatio();
        this.mGlowView.setGlowWidthRatio(glowWidthRatio + ((getGlowWidthToViewWidth() - glowWidthRatio) * 1.0f));
        if (this.mGlowView.getVisibility() != 0) {
            setVisibility(0);
        }
        this.mAnimator.start();
    }

    public /* synthetic */ void lambda$animateGlowTranslationY$2$GlowController(ValueAnimator valueAnimator) {
        this.mGlowsY = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        this.mGlowView.setGlowsY(this.mGlowsY, getMinTranslationY(), getTranslationYProportionalToEdgeLights() ? this.mEdgeLights : null);
    }

    public /* synthetic */ void lambda$animateGlowTranslationY$3$GlowController(int i, int i2, ValueAnimator valueAnimator) {
        this.mGlowView.setBlurRadius((int) MathUtils.lerp((float) i, (float) i2, valueAnimator.getAnimatedFraction()));
    }

    private void setVisibility(int i) {
        this.mGlowView.setVisibility(i);
        if ((i == 0) != isVisible()) {
            this.mVisibilityListener.onVisibilityChanged(i);
            if (!isGlowVisible()) {
                onGlowRemoved();
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeGlow() {
        setVisibility(8);
    }

    private void onGlowRemoved() {
        this.mGlowView.clearCaches();
    }
}

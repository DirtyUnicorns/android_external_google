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
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.EdgeLight;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsListener;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;
import com.google.android.systemui.assist.uihints.edgelights.mode.FulfillBottom;
import com.google.android.systemui.assist.uihints.edgelights.mode.FullListening;
import com.google.android.systemui.assist.uihints.edgelights.mode.Gone;
import com.google.android.systemui.assist.uihints.edgelights.mode.HalfListening;

public final class GlowController implements EdgeLightsListener {
    private ValueAnimator mAnimator;
    private boolean mCardVisible;
    private final Context mContext;
    private EdgeLight[] mEdgeLights;
    private EdgeLightsView.Mode mEdgeLightsMode;
    private final GlowView mGlowView;
    private int mGlowsY;
    private int mGlowsYDestination;
    private boolean mInvocationCompleting;
    private float mMedianLightness;
    private int mNavigationMode;
    private final ScrimController mScrimController;
    private RollingAverage mSpeechRolling;
    private final VisibilityListener mVisibilityListener;

    public GlowController(Context context, ViewGroup viewGroup, LightnessProvider lightnessProvider,
            VisibilityListener visibilityListener, Runnable runnable) {
        mContext = context;
        mAnimator = null;
        mGlowsYDestination = 0;
        mCardVisible = false;
        mEdgeLights = null;
        mEdgeLightsMode = null;
        mGlowsY = 0;
        mGlowsYDestination = 0;
        mInvocationCompleting = false;
        mSpeechRolling = new RollingAverage(3);
        mVisibilityListener = visibilityListener;
        mNavigationMode = ((NavigationModeController) Dependency.get(NavigationModeController.class))
                .addListener(new NavigationModeController.ModeChangedListener() {
            public final void onNavigationModeChanged(int i) {
                mNavigationMode = i;
            }
        });
        mGlowView = (GlowView) LayoutInflater.from(context).inflate(R.layout.glow_view, viewGroup, false);
        mGlowView.setGlowsY(mGlowsY, mGlowsY, null);
        mScrimController = new ScrimController(context, viewGroup, lightnessProvider, visibilityListener, runnable);
        viewGroup.addView(mGlowView);

        mGlowView.setOnClickListener(l -> runnable.run());
        mGlowView.setGlowsY(getMinTranslationY(), getMinTranslationY(), null);
        mGlowView.setGlowWidthRatio(getGlowWidthToViewWidth());
    }

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

    public void setInvocationProgress(float f) {
        if (mEdgeLightsMode instanceof Gone) {
            setVisibility(f > 0.0f ? 0 : 8);
            mGlowView.setBlurRadius(getInvocationBlurRadius(f));
            mGlowsY = getInvocationTranslationY(f);
            int i = mGlowsY;
            mGlowsYDestination = i;
            mGlowView.setGlowsY(i, i, null);
            mGlowView.distributeEvenly();
        }
    }

    public void setCardVisible(boolean z) {
        mCardVisible = z;
    }

    public void setHasMedianLightness(float f) {
        mScrimController.setMedianLightness(f);
        mGlowView.setGlowsBlendMode(f <= NgaUiController.getDarkUiThreshold() ? PorterDuff.Mode.LIGHTEN : PorterDuff.Mode.SRC_OVER);
        mMedianLightness = f;
    }

    public boolean isVisible() {
        return isGlowVisible() || isScrimVisible();
    }

    private boolean isGlowVisible() {
        return mGlowView.getVisibility() == 0;
    }

    public boolean isScrimVisible() {
        return mScrimController.isVisible();
    }

    public ScrimController getScrimController() {
        return mScrimController;
    }

    public void onAudioLevelUpdate(float f, float f2) {
        mSpeechRolling.add(f);
        maybeAnimateForSpeechConfidence();
    }

    public IBinder getScrimSurfaceControllerHandle() {
        return mScrimController.getSurfaceControllerHandle();
    }

    private boolean shouldAnimateForSpeechConfidence() {
        if (!(mEdgeLightsMode instanceof HalfListening) && !(mEdgeLightsMode instanceof FullListening)
                 && !(mEdgeLightsMode instanceof FulfillBottom)) {
            return false;
        }
        return mSpeechRolling.getAverage() >= 0.30000001192092896d || mGlowsYDestination > getMinTranslationY();
    }

    public void maybeAnimateForSpeechConfidence() {
        if (shouldAnimateForSpeechConfidence()) {
            animateGlowTranslationY((int) MathUtils.lerp((float) getMinTranslationY(),
                    (float) getMaxTranslationY(), (float) mSpeechRolling.getAverage()));
        }
    }

    public Rect getTouchableRegion() {
        if (isScrimVisible()) {
            return mScrimController.getTouchableRegion();
        }
        if (mGlowView.getVisibility() != 0 || !QuickStepContract.isGesturalMode(mNavigationMode)) {
            return null;
        }
        Rect rect = new Rect();
        mGlowView.getBoundsOnScreen(rect);
        rect.top = rect.bottom - getMaxTranslationY();
        return rect;
    }

    private GlowState getState() {
        boolean state;
        if ((mEdgeLightsMode instanceof Gone) || mEdgeLightsMode == null || (mEdgeLightsMode instanceof FulfillBottom)
                && !((FulfillBottom) mEdgeLightsMode).isListening()) {
            return GlowState.GONE;
        }
        if (isScrimVisible()) {
            state = mScrimController.isDark();
        } else {
            state = mMedianLightness < NgaUiController.getDarkUiThreshold();
        }
        if (!mCardVisible && (mEdgeLightsMode instanceof HalfListening)) {
            if (state) {
                return GlowState.SHORT_DARK_BACKGROUND;
            }
            return GlowState.SHORT_LIGHT_BACKGROUND;
        } else if (state) {
            return GlowState.TALL_DARK_BACKGROUND;
        } else {
            return GlowState.TALL_LIGHT_BACKGROUND;
        }
    }

    private int getInvocationBlurRadius(float f) {
        return (int) MathUtils.lerp((float) getBlurRadius(), (float) mContext.getResources()
                .getDimensionPixelSize(R.dimen.glow_tall_blur), Math.min(1.0f, f * 5.0f));
    }

    private int getInvocationTranslationY(float f) {
        return (int) MathUtils.min((int) MathUtils.lerp((float) getMinTranslationY(), (float) mContext.getResources()
                .getDimensionPixelSize(R.dimen.glow_tall_min_y), f),
                mContext.getResources().getDimensionPixelSize(R.dimen.glow_invocation_max));
    }

    private int getBlurRadius() {
        if (getState() == GlowState.GONE) {
            return mGlowView.getBlurRadius();
        }
        if (getState() == GlowState.SHORT_DARK_BACKGROUND || getState() == GlowState.SHORT_LIGHT_BACKGROUND) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.glow_short_blur);
        }
        if (getState() == GlowState.TALL_DARK_BACKGROUND || getState() == GlowState.TALL_LIGHT_BACKGROUND) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.glow_tall_blur);
        }
        return 0;
    }

    private int getMinTranslationY() {
        if (getState() == GlowState.SHORT_DARK_BACKGROUND || getState() == GlowState.SHORT_LIGHT_BACKGROUND) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.glow_short_min_y);
        }
        if (getState() == GlowState.TALL_DARK_BACKGROUND || getState() == GlowState.TALL_LIGHT_BACKGROUND) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.glow_tall_min_y);
        }
        return mContext.getResources().getDimensionPixelSize(R.dimen.glow_gone_min_y);
    }

    private int getMaxTranslationY() {
        if (getState() == GlowState.SHORT_DARK_BACKGROUND || getState() == GlowState.SHORT_LIGHT_BACKGROUND) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.glow_short_max_y);
        }
        if (getState() == GlowState.TALL_DARK_BACKGROUND || getState() == GlowState.TALL_LIGHT_BACKGROUND) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.glow_tall_max_y);
        }
        return mContext.getResources().getDimensionPixelSize(R.dimen.glow_gone_max_y);
    }

    public void onModeStarted(EdgeLightsView.Mode mode) {
        if (!(mode instanceof Gone) || mEdgeLightsMode != null) {
            mInvocationCompleting = !(mode instanceof Gone);
            mEdgeLightsMode = mode;
            if (mode instanceof Gone) {
                mSpeechRolling = new RollingAverage(3);
            }
            mScrimController.setInFullListening(mode instanceof FullListening);
            animateGlowTranslationY(getMinTranslationY());
            if (mEdgeLightsMode instanceof HalfListening) {
                mGlowView.sendAccessibilityEvent(8);
                return;
            }
            return;
        }
        mEdgeLightsMode = mode;
    }

    public void onAssistLightsUpdated(EdgeLightsView.Mode mode, EdgeLight[] edgeLightArr) {
        int i;
        if (!getTranslationYProportionalToEdgeLights()) {
            mEdgeLights = null;
            mGlowView.distributeEvenly();
            return;
        }
        mEdgeLights = edgeLightArr;
        if (!(mode instanceof FullListening)) {
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
        return mEdgeLightsMode instanceof FullListening;
    }

    private long getYAnimationDuration(float f) {
        return (long) Math.min((float) getMaxYAnimationDuration(), Math.abs(f) /
                ((float) (((long) Math.abs(getMaxTranslationY() - getMinTranslationY())) / getMaxYAnimationDuration())));
    }

    private void animateGlowTranslationY(int i) {
        animateGlowTranslationY(i, getYAnimationDuration((float) (i - mGlowsY)));
    }

    private void animateGlowTranslationY(int i, long j) {
        if (i == mGlowsYDestination) {
            mGlowView.setGlowsY(mGlowsY, getMinTranslationY(), getTranslationYProportionalToEdgeLights() ? mEdgeLights : null);
            return;
        }
        mGlowsYDestination = i;
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofInt(mGlowsY, i);
        mAnimator.addUpdateListener(valueAnimator -> {
            mGlowsY = (Integer) valueAnimator.getAnimatedValue();
            mGlowView.setGlowsY(mGlowsY, getMinTranslationY(), getTranslationYProportionalToEdgeLights() ? mEdgeLights : null);
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                mAnimator = null;
                if (GlowState.GONE.equals(getState())) {
                    removeGlow();
                } else {
                    maybeAnimateForSpeechConfidence();
                }
            }
        });
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(j);
        mAnimator.addUpdateListener(valueAnim -> lambda$animateGlowTranslationY$3$GlowController(mGlowView.getBlurRadius(), getBlurRadius(), valueAnim));

        float glowWidthRatio = mGlowView.getGlowWidthRatio();
        mGlowView.setGlowWidthRatio(glowWidthRatio + ((getGlowWidthToViewWidth() - glowWidthRatio) * 1.0f));
        if (mGlowView.getVisibility() != 0) {
            setVisibility(0);
        }
        mAnimator.start();
    }

    private /* synthetic */ void lambda$animateGlowTranslationY$3$GlowController(int i, int i2, ValueAnimator valueAnimator) {
        mGlowView.setBlurRadius((int) MathUtils.lerp((float) i, (float) i2, valueAnimator.getAnimatedFraction()));
    }

    private void setVisibility(int i) {
        mGlowView.setVisibility(i);
        if ((i == 0) != isVisible()) {
            mVisibilityListener.onVisibilityChanged(i);
            if (!isGlowVisible()) {
                onGlowRemoved();
            }
        }
    }

    private void removeGlow() {
        setVisibility(8);
    }

    private void onGlowRemoved() {
        mGlowView.clearCaches();
    }
}

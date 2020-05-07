package com.google.android.systemui.assist.uihints;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.StatusBar;

final class OverlappedElementController {
    private final Context mContext;
    private ValueAnimator mAnimator;
    private float mAlpha;

    OverlappedElementController(Context context) {
        mContext = context;
        mAnimator = null;
        mAlpha = 1.0f;
    }

    public void setAlpha(float f, boolean z) {
        float f2 = mAlpha;
        if (f2 != f) {
            if (f2 == 1.0f && f < 1.0f) {
                Log.v("OverlappedElementController", "Overlapped elements becoming transparent.");
            } else if (mAlpha < 1.0f && f == 1.0f) {
                Log.v("OverlappedElementController", "Overlapped elements becoming opaque.");
            }
            if (mAnimator != null) {
                mAnimator.cancel();
            }
            if (!z) {
                mAlpha = f;
                tellOverlappedElementsSetAlpha(mAlpha);
                return;
            }
            mAnimator = ValueAnimator.ofFloat(mAlpha, f);
            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimator.setDuration((long) (Math.abs(f - mAlpha) * 300.0f));
            mAnimator.addUpdateListener(valueAnimator -> {
                mAlpha = (Float) valueAnimator.getAnimatedValue();
                tellOverlappedElementsSetAlpha(mAlpha);
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    mAnimator = null;
                }
            });
            mAnimator.start();
        }
    }

    private void tellOverlappedElementsSetAlpha(float f) {
        ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).notifyAssistantVisibilityChanged(1.0f - f);
        StatusBar statusBar = (StatusBar) ((SystemUIApplication) mContext.getApplicationContext()).getComponent(StatusBar.class);
        if (statusBar != null) {
            View ambientIndicationContainer = statusBar.getAmbientIndicationContainer();
            if (ambientIndicationContainer != null) {
                ambientIndicationContainer.setAlpha(f);
            }
            KeyguardBottomAreaView keyguardBottomAreaView = statusBar.getKeyguardBottomAreaView();
            if (keyguardBottomAreaView != null) {
                keyguardBottomAreaView.setAffordanceAlpha(f);
            }
        }
    }
}

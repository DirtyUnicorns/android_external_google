package com.google.android.systemui.assist.uihints;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.android.systemui.Dependency;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.StatusBar;

final class OverlappedElementController {
    private float mAlpha = 1.0f;
    /* access modifiers changed from: private */
    public ValueAnimator mAnimator = null;
    private final Context mContext;

    OverlappedElementController(Context context) {
        this.mContext = context;
    }

    public void setAlpha(float f, boolean z) {
        float f2 = this.mAlpha;
        if (f2 != f) {
            if (f2 == 1.0f && f < 1.0f) {
                Log.v("OverlappedElementController", "Overlapped elements becoming transparent.");
            } else if (this.mAlpha < 1.0f && f == 1.0f) {
                Log.v("OverlappedElementController", "Overlapped elements becoming opaque.");
            }
            ValueAnimator valueAnimator = this.mAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (!z) {
                this.mAlpha = f;
                tellOverlappedElementsSetAlpha(this.mAlpha);
                return;
            }
            this.mAnimator = ValueAnimator.ofFloat(this.mAlpha, f);
            this.mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            this.mAnimator.setDuration((long) (Math.abs(f - this.mAlpha) * 300.0f));
            this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.google.android.systemui.assist.uihints.C1559xc48e7d7 */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    lambda$setAlpha$0$OverlappedElementController(valueAnimator);
                }
            });
            this.mAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.google.android.systemui.assist.uihints.OverlappedElementController.C15671 */

                public void onAnimationEnd(Animator animator) {
                    ValueAnimator unused = mAnimator = null;
                }
            });
            this.mAnimator.start();
        }
    }

    public /* synthetic */ void lambda$setAlpha$0$OverlappedElementController(ValueAnimator valueAnimator) {
        this.mAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        tellOverlappedElementsSetAlpha(this.mAlpha);
    }

    private void tellOverlappedElementsSetAlpha(float f) {
        ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).notifyAssistantVisibilityChanged(1.0f - f);
        StatusBar statusBar = (StatusBar) SysUiServiceProvider.getComponent(this.mContext, StatusBar.class);
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

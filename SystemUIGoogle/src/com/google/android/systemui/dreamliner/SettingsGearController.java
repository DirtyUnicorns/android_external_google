package com.google.android.systemui.dreamliner;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Interpolators;
import java.util.concurrent.TimeUnit;

public class SettingsGearController extends SimpleOnGestureListener implements OnTouchListener {
    private static final long GEAR_VISIBLE_TIME_MILLIS = TimeUnit.SECONDS.toMillis(5);
    private final AccessibilityManager mAccessibilityManager;
    @VisibleForTesting
    boolean mBouncerShowing;
    private final Context mContext;
    @VisibleForTesting
    GestureDetector mGestureDetector;
    private final Runnable mHideGearRunnable = new Runnable() {
        public final void run() {
            hideGear();
        }
    };
    @VisibleForTesting
    boolean mIsDreaming;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    @VisibleForTesting
    KeyguardUpdateMonitorCallback mKeyguardVisibilityCallback;
    private final ImageView mSettingsGear;
    private final View mTouchDelegateView;

    SettingsGearController(Context context, ImageView imageView, View view, KeyguardUpdateMonitor keyguardUpdateMonitor) {
        this.mContext = context;
        this.mGestureDetector = new GestureDetector(context, this);
        this.mTouchDelegateView = view;
        this.mSettingsGear = imageView;
        this.mSettingsGear.setOnClickListener(new OnClickListener() {
            public final void onClick(View view) {
                lambda$new$0$SettingsGearController(view);
            }
        });
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mKeyguardVisibilityCallback = new KeyguardUpdateMonitorCallback() {
            public void onKeyguardBouncerChanged(boolean z) {
                SettingsGearController settingsGearController = SettingsGearController.this;
                settingsGearController.mBouncerShowing = z;
                if (settingsGearController.mBouncerShowing) {
                    settingsGearController.hideGear();
                }
            }

            public void onDreamingStateChanged(boolean z) {
                SettingsGearController settingsGearController = SettingsGearController.this;
                settingsGearController.mIsDreaming = z;
                if (!settingsGearController.mIsDreaming) {
                    settingsGearController.hideGear();
                }
            }
        };
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
    }

    public /* synthetic */ void lambda$new$0$SettingsGearController(View view) {
        hideGear();
        sendProtectedBroadcast("com.google.android.apps.dreamliner.SETTINGS");
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (this.mIsDreaming) {
            this.mGestureDetector.onTouchEvent(motionEvent);
        }
        return !this.mBouncerShowing;
    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        showGear();
        return false;
    }

    public boolean onDown(MotionEvent motionEvent) {
        sendProtectedBroadcast("com.google.android.systemui.dreamliner.TOUCH_EVENT");
        return false;
    }

    /* access modifiers changed from: 0000 */
    public void startMonitoring() {
        this.mSettingsGear.setVisibility(4);
        this.mIsDreaming = this.mKeyguardUpdateMonitor.isDreaming();
        this.mBouncerShowing = false;
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardVisibilityCallback);
        this.mTouchDelegateView.setOnTouchListener(this);
    }

    /* access modifiers changed from: 0000 */
    public void stopMonitoring() {
        this.mTouchDelegateView.setOnTouchListener(null);
        this.mKeyguardUpdateMonitor.removeCallback(this.mKeyguardVisibilityCallback);
        this.mSettingsGear.setVisibility(8);
    }

    private void showGear() {
        if (!this.mSettingsGear.isVisibleToUser()) {
            this.mSettingsGear.setVisibility(0);
            this.mSettingsGear.animate().setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).alpha(1.0f).start();
        }
        this.mSettingsGear.removeCallbacks(this.mHideGearRunnable);
        this.mSettingsGear.postDelayed(this.mHideGearRunnable, getRecommendedTimeoutMillis());
    }

    /* access modifiers changed from: private */
    public void hideGear() {
        if (this.mSettingsGear.isVisibleToUser()) {
            this.mSettingsGear.removeCallbacks(this.mHideGearRunnable);
            this.mSettingsGear.animate().setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).alpha(0.0f).withEndAction(new Runnable() {
                public final void run() {
                    lambda$hideGear$1$SettingsGearController();
                }
            }).start();
        }
    }

    public /* synthetic */ void lambda$hideGear$1$SettingsGearController() {
        this.mSettingsGear.setVisibility(4);
    }

    private void sendProtectedBroadcast(String str) {
        try {
            this.mContext.sendBroadcastAsUser(new Intent(str), UserHandle.CURRENT);
        } catch (SecurityException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot send event, action=");
            sb.append(str);
            Log.w("SettingsGearController", sb.toString(), e);
        }
    }

    private long getRecommendedTimeoutMillis() {
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        if (accessibilityManager == null) {
            return GEAR_VISIBLE_TIME_MILLIS;
        }
        return (long) accessibilityManager.getRecommendedTimeoutMillis(Math.toIntExact(GEAR_VISIBLE_TIME_MILLIS), 5);
    }
}
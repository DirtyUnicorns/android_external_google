package com.google.android.systemui.elmyra.feedback;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class UserActivity implements FeedbackEffect {
    private final KeyguardMonitor mKeyguardMonitor = ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class));
    private int mLastStage = 0;
    private final PowerManager mPowerManager;
    private int mTriggerCount = 0;

    public UserActivity(Context context) {
        mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
    }

    @Override
	public void onProgress(float f, int i) {
        if (!(i == mLastStage || i != 2 || mKeyguardMonitor.isShowing() || mPowerManager == null)) {
            mPowerManager.userActivity(SystemClock.uptimeMillis(), 0, 0);
            mTriggerCount++;
        }
        mLastStage = i;
    }

    @Override
	public void onRelease() {
    }

    public void onResolve(DetectionProperties detectionProperties) {
        mTriggerCount--;
    }
}

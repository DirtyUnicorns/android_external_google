package com.google.android.systemui.elmyra.feedback;

import android.content.Context;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.phone.StatusBar;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class NavUndimEffect implements FeedbackEffect {
    private final StatusBar mStatusBar;

    public NavUndimEffect(Context context) {
        mStatusBar = (StatusBar) SysUiServiceProvider.getComponent(context, StatusBar.class);
    }

    @Override
	public void onProgress(float f, int i) {
        if (mStatusBar != null) {
            mStatusBar.touchAutoDim();
        }
    }

    @Override
	public void onRelease() {
        if (mStatusBar != null) {
            mStatusBar.touchAutoDim();
        }
    }

    public void onResolve(DetectionProperties detectionProperties) {
        if (mStatusBar != null) {
            mStatusBar.touchAutoDim();
        }
    }
}

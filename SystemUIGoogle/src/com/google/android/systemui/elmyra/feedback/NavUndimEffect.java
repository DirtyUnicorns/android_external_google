package com.google.android.systemui.elmyra.feedback;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NavigationBarController;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class NavUndimEffect implements FeedbackEffect {
    private final NavigationBarController mNavBarController = ((NavigationBarController) Dependency.get(NavigationBarController.class));

    public void onProgress(float f, int i) {
        this.mNavBarController.touchAutoDim(0);
    }

    public void onRelease() {
        this.mNavBarController.touchAutoDim(0);
    }

    public void onResolve(DetectionProperties detectionProperties) {
        this.mNavBarController.touchAutoDim(0);
    }
}
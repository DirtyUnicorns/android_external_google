package com.google.android.systemui.statusbar.phone;

import android.widget.ImageView;
import com.android.internal.util.du.Utils;
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.dock.DockManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.google.android.systemui.NotificationLockscreenUserManagerGoogle;
import com.google.android.systemui.smartspace.SmartSpaceController;
import com.google.android.systemui.dreamliner.DockIndicationController;
import com.google.android.systemui.dreamliner.DockObserver;

public class StatusBarGoogle extends StatusBar {
    @Override
    public void start() {
        super.start();
        DockObserver dockObserver = (DockObserver) Dependency.get(DockManager.class);
        dockObserver.setDreamlinerGear((ImageView) mStatusBarWindow.findViewById(R.id.dreamliner_gear));
        dockObserver.setIndicationController(new DockIndicationController(mContext));

        // Enable MotionSense plugin
        enableOsloPlugin();
        ((NotificationLockscreenUserManagerGoogle) Dependency.get(NotificationLockscreenUserManager.class)).updateAodVisibilitySettings();
    }

    @Override
    public void setLockscreenUser(int i) {
        super.setLockscreenUser(i);
        SmartSpaceController.get(this.mContext).reloadData();
    }

    private void enableOsloPlugin() {
        if (Utils.isPackageInstalled(mContext, "com.google.oslo")) {
            Utils.setComponentState(mContext, "com.google.oslo", "com.google.oslo.OsloOverlay", true);
            Utils.setComponentState(mContext, "com.google.oslo", "com.google.oslo.OsloSensorManager", true);
            Utils.setComponentState(mContext, "com.google.oslo", "com.google.oslo.service.OsloService", true);
        }
    }
}

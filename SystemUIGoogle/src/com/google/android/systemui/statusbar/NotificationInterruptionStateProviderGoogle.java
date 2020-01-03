package com.google.android.systemui.statusbar;

import android.content.Context;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationInterruptionStateProvider;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.BatteryController;
import com.google.android.systemui.dreamliner.DockObserver;


import com.google.android.systemui.dreamliner.DockObserver;

public class NotificationInterruptionStateProviderGoogle extends NotificationInterruptionStateProvider {
    public NotificationInterruptionStateProviderGoogle(Context context, NotificationFilter notificationFilter, StatusBarStateController statusBarStateController, BatteryController batteryController) {
        super(context, notificationFilter, statusBarStateController, batteryController);
    }

    public boolean canAlertCommon(NotificationEntry notificationEntry) {
        if (!DockObserver.isDockingUiShowing()) {
            return super.canAlertCommon(notificationEntry);
        }

        return false;
    }
}
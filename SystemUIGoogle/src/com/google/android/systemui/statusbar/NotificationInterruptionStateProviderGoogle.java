package com.google.android.systemui.statusbar;

import android.content.Context;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationInterruptionStateProvider;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.BatteryController;
import com.google.android.systemui.dreamliner.DockObserver;

import com.google.android.systemui.dreamliner.DockObserver;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotificationInterruptionStateProviderGoogle extends 
        NotificationInterruptionStateProvider {
    
    @Inject
    public NotificationInterruptionStateProviderGoogle(Context context,
            NotificationFilter filter,
            StatusBarStateController stateController,
            BatteryController batteryController) {
        super(context, filter, stateController, batteryController);
    }

    @Override
    public boolean canAlertCommon(NotificationEntry notificationEntry) {
        if (!DockObserver.isDockingUiShowing()) {
            return super.canAlertCommon(notificationEntry);
        }

        return false;
    }
}
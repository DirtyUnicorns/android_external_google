package com.google.android.systemui;

import android.content.Context;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.dock.DockManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationInterruptionStateProvider;
import com.android.systemui.statusbar.policy.BatteryController;

import com.android.systemui.plugins.statusbar.StatusBarStateController;

import com.google.android.systemui.dreamliner.DockObserver;
import com.google.android.systemui.dreamliner.DreamlinerUtils;

import com.google.android.systemui.statusbar.NotificationInterruptionStateProviderGoogle;


public class SystemUIGoogleFactory extends SystemUIFactory {

    public DockManager provideDockManager(Context context) {
        return new DockObserver(context, DreamlinerUtils.getInstance(context));
    }

    public NotificationInterruptionStateProvider provideNotificationInterruptionStateProvider(Context context, NotificationFilter notificationFilter, StatusBarStateController statusBarStateController, BatteryController batteryController) {
        return new NotificationInterruptionStateProviderGoogle(context, notificationFilter, statusBarStateController, batteryController);
    }
}
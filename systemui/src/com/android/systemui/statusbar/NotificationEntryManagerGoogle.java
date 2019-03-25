package com.google.android.systemui.statusbar;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationEntryManager;
import com.google.android.systemui.dreamliner.DockObserver;

import java.util.HashSet;

public class NotificationEntryManagerGoogle extends NotificationEntryManager {

    public NotificationEntryManagerGoogle(Context context) {
        super(context);
    }

    public boolean shouldPeek(Entry entry, StatusBarNotification sbn) {
        if (DockObserver.isDockingUiShowing()) {
            return false;
        }
        return super.shouldPeek(entry, sbn);
    }
}
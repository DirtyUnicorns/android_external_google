package com.google.android.systemui.assist.uihints;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.util.Log;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;

public class TaskStackNotifier {
    private PendingIntent mIntent;
    private final TaskStackChangeListener mListener = new TaskStackChangeListener() {
        /* class com.google.android.systemui.assist.uihints.TaskStackNotifier.C15701 */

        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            sendIntent();
        }

        public void onTaskCreated(int i, ComponentName componentName) {
            sendIntent();
        }
    };
    private boolean mListenerRegistered = false;
    private final ActivityManagerWrapper mWrapper = ActivityManagerWrapper.getInstance();

    public void setIntent(PendingIntent pendingIntent) {
        this.mIntent = pendingIntent;
        if (this.mIntent != null && !this.mListenerRegistered) {
            this.mWrapper.registerTaskStackListener(this.mListener);
            this.mListenerRegistered = true;
        } else if (this.mIntent == null && this.mListenerRegistered) {
            this.mWrapper.unregisterTaskStackListener(this.mListener);
            this.mListenerRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    public void sendIntent() {
        PendingIntent pendingIntent = this.mIntent;
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e("TaskStackNotifier", "could not send intent", e);
            }
        }
    }
}

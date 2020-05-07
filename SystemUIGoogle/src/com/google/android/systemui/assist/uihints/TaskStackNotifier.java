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
        mIntent = pendingIntent;
        if (mIntent != null && !mListenerRegistered) {
            mWrapper.registerTaskStackListener(mListener);
            mListenerRegistered = true;
        } else if (mIntent == null && mListenerRegistered) {
            mWrapper.unregisterTaskStackListener(mListener);
            mListenerRegistered = false;
        }
    }

    private void sendIntent() {
        if (mIntent != null) {
            try {
                mIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e("TaskStackNotifier", "could not send intent", e);
            }
        }
    }
}

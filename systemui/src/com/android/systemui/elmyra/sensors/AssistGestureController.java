package com.google.android.systemui.elmyra.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Binder;
import android.os.SystemClock;
import android.util.Slog;
import android.util.TypedValue;
import com.android.systemui.R;
import com.google.android.systemui.elmyra.SnapshotConfiguration;
import com.google.android.systemui.elmyra.SnapshotController;
import com.google.android.systemui.elmyra.SnapshotLogger;
import com.google.android.systemui.elmyra.proto.nano.ElmyraChassis.Chassis;
import com.google.android.systemui.elmyra.proto.nano.SnapshotMessages.Snapshot;
import com.google.android.systemui.elmyra.proto.nano.SnapshotMessages.Snapshots;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import com.google.android.systemui.elmyra.sensors.GestureSensor.Listener;
import com.google.protobuf.nano.MessageNano;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

class AssistGestureController {
    private Chassis mChassis;
    private SnapshotLogger mCompleteGestures;
    private final long mFalsePrimeWindow;
    private final long mGestureCooldownTime;
    private Listener mGestureListener;
    private float mGestureProgress;
    private final GestureSensor mGestureSensor;
    private SnapshotLogger mIncompleteGestures;
    private boolean mIsFalsePrimed;
    private long mLastDetectionTime;
    private OPAQueryReceiver mOpaQueryReceiver;
    private final float mProgressAlpha;
    private final float mProgressReportThreshold;
    private final SnapshotController mSnapshotController;
    // When the squeeze detection starts (expressed in ms)
    private long mGestureStartTime;
    // Long squeeze duration threshold
    private long mLongSqueezeDuration;

    private class OPAQueryReceiver extends BroadcastReceiver {
        private OPAQueryReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.google.android.systemui.OPA_ELMYRA_QUERY_SUBMITTED")) {
                mCompleteGestures.didReceiveQuery();
            }
        }
    }

    AssistGestureController(Context context, GestureSensor gestureSensor) {
        this(context, gestureSensor, null);
    }

    AssistGestureController(Context context, GestureSensor gestureSensor, SnapshotConfiguration snapshotConfiguration) {
        int i = 0;
        mOpaQueryReceiver = new OPAQueryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.google.android.systemui.OPA_ELMYRA_QUERY_SUBMITTED");
        context.registerReceiver(mOpaQueryReceiver, intentFilter);
        mGestureSensor = gestureSensor;
        Resources resources = context.getResources();
        TypedValue typedValue = new TypedValue();
        mCompleteGestures = new SnapshotLogger(snapshotConfiguration != null ? snapshotConfiguration.getCompleteGestures() : 0);
        if (snapshotConfiguration != null) {
            i = snapshotConfiguration.getIncompleteGestures();
        }
        mIncompleteGestures = new SnapshotLogger(i);
        resources.getValue(R.dimen.elmyra_progress_alpha, typedValue, true);
        mProgressAlpha = typedValue.getFloat();
        resources.getValue(R.dimen.elmyra_progress_report_threshold, typedValue, true);
        mProgressReportThreshold = typedValue.getFloat();
        mGestureCooldownTime = (long) resources.getInteger(R.integer.elmyra_gesture_cooldown_time);
        mFalsePrimeWindow = mGestureCooldownTime + ((long) resources.getInteger(R.integer.elmyra_false_prime_window));
        mLongSqueezeDuration = (long) resources.getInteger(R.integer.elmyra_long_squeeze_duration);
        mSnapshotController = new SnapshotController(snapshotConfiguration);
    }

    private void sendGestureProgress(GestureSensor gestureSensor, float f, int i) {
        if (mGestureListener != null) {
            mGestureListener.onGestureProgress(gestureSensor, f, i);
        }
        mSnapshotController.onGestureProgress(gestureSensor, f, i);
    }


    public Chassis getChassisConfiguration() {
        return mChassis;
    }

    public void onGestureDetected(DetectionProperties detectionProperties) {
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis - mLastDetectionTime >= mGestureCooldownTime && !mIsFalsePrimed) {
            if (mGestureListener != null) {
                mGestureListener.onGestureDetected(mGestureSensor, detectionProperties);
            }
            mSnapshotController.onGestureDetected(mGestureSensor, detectionProperties);
            mLastDetectionTime = uptimeMillis;
        }
    }

    public void onGestureProgress(float f) {
        int i = 1;

        // If the gesture progress is equal to 0, the action hasn't started yet.
        if (mGestureProgress == 0.0f) {
            // Store the time when the action got triggered.
            mGestureStartTime = SystemClock.uptimeMillis();
        }

        if (f == 0.0f) {
            mGestureProgress = 0.0f;
            mIsFalsePrimed = false;
        } else {
            mGestureProgress = (mProgressAlpha * f) + ((1.0f - mProgressAlpha) * mGestureProgress);
        }

        long uptimeMillis = SystemClock.uptimeMillis();
        float f2;

        if (uptimeMillis - mLastDetectionTime >= mGestureCooldownTime && !mIsFalsePrimed) {
            if (uptimeMillis - mLastDetectionTime < mFalsePrimeWindow && f == 1.0f) {
                mIsFalsePrimed = true;
            } else if (mGestureProgress < mProgressReportThreshold) {
                sendGestureProgress(mGestureSensor, 0.0f, 0);
            } else if (uptimeMillis - mGestureStartTime >= mLongSqueezeDuration && f == 1.0f) {
                    f2 = (mGestureProgress - mProgressReportThreshold) / (1.0f - mProgressReportThreshold);
                    onGestureDetected(new DetectionProperties(false, false, /* longSqueeze */ true));
                    // Send progress to have consistent animations
                    sendGestureProgress(mGestureSensor, f2, f == 1f ? 2 : i);
            } else {
                f2 = (mGestureProgress - mProgressReportThreshold) / (1.0f - mProgressReportThreshold);
                sendGestureProgress(mGestureSensor, f2, f == 1f ? 2 : i);
            }
        }
    }

    public void onSnapshotReceived(Snapshot snapshot) {
        if (snapshot.header.gestureType == 1) {
            mCompleteGestures.addSnapshot(snapshot, System.currentTimeMillis());
        } else {
            mIncompleteGestures.addSnapshot(snapshot, System.currentTimeMillis());
        }
    }

    public void setGestureListener(Listener listener) {
        mGestureListener = listener;
    }

    public void setSnapshotListener(SnapshotController.Listener listener) {
        mSnapshotController.setListener(listener);
    }

    public void storeChassisConfiguration(Chassis chassis) {
        mChassis = chassis;
    }
}

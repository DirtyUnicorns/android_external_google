package com.google.android.systemui.elmyra.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Binder;
import android.os.SystemClock;
import android.util.TypedValue;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.google.android.systemui.elmyra.SnapshotConfiguration;
import com.google.android.systemui.elmyra.SnapshotController;
import com.google.android.systemui.elmyra.SnapshotLogger;
import com.google.android.systemui.elmyra.SnapshotLogger.Snapshot;
import com.google.android.systemui.elmyra.proto.nano.ChassisProtos;
import com.google.android.systemui.elmyra.proto.nano.ChassisProtos.Chassis;
import com.google.android.systemui.elmyra.proto.nano.SnapshotProtos;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import com.google.android.systemui.elmyra.sensors.GestureSensor.Listener;
import com.google.android.systemui.elmyra.sensors.config.GestureConfiguration;
import com.google.protobuf.nano.MessageNano;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

class AssistGestureController implements Dumpable {
    private final static String TAG = "Elmyra/AssistGestureController";

    private ChassisProtos.Chassis mChassis;
    private SnapshotLogger mCompleteGestures;
    private final long mFalsePrimeWindow;
    private final GestureConfiguration mGestureConfiguration;
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

    AssistGestureController(Context context, GestureSensor gestureSensor, GestureConfiguration gestureConfiguration) {
        this(context, gestureSensor, gestureConfiguration, null);
    }

    AssistGestureController(Context context, GestureSensor gestureSensor, GestureConfiguration gestureConfiguration, SnapshotConfiguration snapshotConfiguration) {
        mOpaQueryReceiver = new OPAQueryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.google.android.systemui.OPA_ELMYRA_QUERY_SUBMITTED");
        context.registerReceiver(mOpaQueryReceiver, intentFilter);
        mGestureSensor = gestureSensor;
        mGestureConfiguration = gestureConfiguration;
        Resources resources = context.getResources();
        TypedValue typedValue = new TypedValue();
        int i = 0;
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
        if (snapshotConfiguration != null) {
            mSnapshotController = new SnapshotController(snapshotConfiguration);
        } else {
            mSnapshotController = null;
        }
    }

    public void setGestureListener(Listener listener) {
        mGestureListener = listener;
    }

    public void setSnapshotListener(SnapshotController.Listener listener) {
        SnapshotController snapshotController = mSnapshotController;
        if (snapshotController != null) {
            snapshotController.setListener(listener);
        }
    }

    public void storeChassisConfiguration(ChassisProtos.Chassis chassisProtos) {
        mChassis = chassisProtos;
    }

    public ChassisProtos.Chassis getChassisConfiguration() {
        return mChassis;
    }

    public void onGestureProgress(float f) {
        if (mGestureProgress == 0.0f) {
            mGestureStartTime = SystemClock.uptimeMillis();
        }
        if (f == 0.0f) {
            mGestureProgress = 0.0f;
            mIsFalsePrimed = false;
        } else {
            float f2 = mProgressAlpha;
            mGestureProgress = (f2 * f) + ((1.0f - f2) * mGestureProgress);
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        long j = mLastDetectionTime;
        if (uptimeMillis - j >= mGestureCooldownTime && !mIsFalsePrimed) {
            int i = ((uptimeMillis - j) > mFalsePrimeWindow ? 1 : ((uptimeMillis - j) == mFalsePrimeWindow ? 0 : -1));
            int i2 = 1;
            if (i >= 0 || f != 1.0f) {
                float f3 = mGestureProgress;
                float f4 = mProgressReportThreshold;
                if (f3 < f4) {
                    sendGestureProgress(mGestureSensor, 0.0f, 0);
                } else if (uptimeMillis - mGestureStartTime >= mLongSqueezeDuration && f == 1.0f) {
                    float f5 = (f3 - f4) / (1.0f - f4);
                    onGestureDetected(new DetectionProperties(false, false, /* longSqueeze */ true));
                    // Send progress to have consistent animations
                    sendGestureProgress(mGestureSensor, f5, f == 1f ? 2 : i);
                } else {
                    float f5 = (f3 - f4) / (1.0f - f4);
                    if (f == 1.0f) {
                        i2 = 2;
                    }
                    sendGestureProgress(mGestureSensor, f5, i2);
                }
            } else {
                mIsFalsePrimed = true;
            }
        }
    }

    public void onGestureDetected(DetectionProperties detectionProperties) {
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis - mLastDetectionTime >= mGestureCooldownTime && !mIsFalsePrimed) {
            Listener listener = mGestureListener;
            if (listener != null) {
                listener.onGestureDetected(mGestureSensor, detectionProperties);
            }
            SnapshotController snapshotController = mSnapshotController;
            if (snapshotController != null) {
                snapshotController.onGestureDetected(mGestureSensor, detectionProperties);
            }
            mLastDetectionTime = uptimeMillis;
        }
    }

    public void onSnapshotReceived(SnapshotProtos.Snapshot snapshotProtos) {
        int i = snapshotProtos.header.gestureType;
        if (i == 4) {
        } else if (i == 1) {
            mCompleteGestures.addSnapshot(snapshotProtos, System.currentTimeMillis());
        } else {
            mIncompleteGestures.addSnapshot(snapshotProtos, System.currentTimeMillis());
        }
    }

    private void sendGestureProgress(GestureSensor gestureSensor, float f, int i) {
        Listener listener = mGestureListener;
        if (listener != null) {
            listener.onGestureProgress(gestureSensor, f, i);
        }
        SnapshotController snapshotController = mSnapshotController;
        if (snapshotController != null) {
            snapshotController.onGestureProgress(gestureSensor, f, i);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (mChassis != null) {
            for (int i = 0; i < mChassis.sensors.length; i++) {
                printWriter.print("sensors {");
                StringBuilder sb = new StringBuilder();
                sb.append("  source: ");
                sb.append(mChassis.sensors[i].source);
                printWriter.print(sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  gain: ");
                sb2.append(mChassis.sensors[i].gain);
                printWriter.print(sb2.toString());
                StringBuilder sb3 = new StringBuilder();
                sb3.append("  sensitivity: ");
                sb3.append(mChassis.sensors[i].sensitivity);
                printWriter.print(sb3.toString());
                printWriter.print("}");
            }
            printWriter.println();
        }
        boolean z = false;
        boolean z2 = false;
        for (String str : strArr) {
            if (str.equals("GoogleServices")) {
                z = true;
            } else if (str.equals("proto")) {
                z2 = true;
            }
        }

        StringBuilder sb4 = new StringBuilder();
        sb4.append("user_sensitivity: ");
        sb4.append(mGestureConfiguration.getSensitivity());
        printWriter.println(sb4.toString());
    }
}
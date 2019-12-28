package com.google.android.systemui.elmyra;

import android.annotation.SuppressLint;
import android.content.Context;
import android.metrics.LogMaker;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dumpable;
import com.google.android.systemui.elmyra.actions.Action;
import com.google.android.systemui.elmyra.feedback.FeedbackEffect;
import com.google.android.systemui.elmyra.gates.Gate;
import com.google.android.systemui.elmyra.sensors.GestureSensor;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ElmyraService implements Dumpable {
    private Context mContext;
    private GestureSensor.Listener mGestureListener = new GestureListener();
    private static GestureSensor mGestureSensor;
    private static List<Gate> mGates;
    private Action mLastActiveAction;
    private static long mLastPrimedGesture;
    private static int mLastStage;
    private static MetricsLogger mLogger;
    public static PowerManager mPowerManager;
    public static PowerManager.WakeLock mWakeLock;
    private static List<FeedbackEffect> mFeedbackEffects;
    private static List<Action> mActions;

    private Action.Listener mActionListener = action -> updateSensorListener();
    private Gate.Listener mGateListener = gate -> updateSensorListener();

    private class GestureListener implements GestureSensor.Listener {
        private GestureListener() {
        }

        public void onGestureProgress(GestureSensor gestureSensor, float f, int i) {
            Action access = updateActiveAction();
            if (access != null) {
                access.onProgress(f, i);
                for (int i2 = 0; i2 < ElmyraService.mFeedbackEffects.size(); i2++) {
                    ElmyraService.mFeedbackEffects.get(i2).onProgress(f, i);
                }
            }
            if (i != ElmyraService.mLastStage) {
                long uptimeMillis = SystemClock.uptimeMillis();
                if (i == 2) {
                    ElmyraService.mLogger.action(998);
                    long unused = ElmyraService.mLastPrimedGesture = uptimeMillis;
                } else if (i == 0 && ElmyraService.mLastPrimedGesture != 0) {
                    ElmyraService.mLogger.write(new LogMaker(
                            997).setType(4).setLatency(uptimeMillis - ElmyraService.mLastPrimedGesture));
                }
                int unused2 = ElmyraService.mLastStage = i;
            }
        }

        public void onGestureDetected(GestureSensor gestureSensor, GestureSensor.DetectionProperties detectionProperties) {
            ElmyraService.mWakeLock.acquire(2000);
            boolean isInteractive = ElmyraService.mPowerManager.isInteractive();
            LogMaker latency = new LogMaker(999).setType(4).setSubtype(
                    (detectionProperties == null ||
                            !detectionProperties.isHostSuspended()) ?
                            !isInteractive ? 2 : 1 : 3).setLatency(isInteractive ?
                    SystemClock.uptimeMillis() - ElmyraService.mLastPrimedGesture : 0);
            long unused = ElmyraService.mLastPrimedGesture = 0;
            Action access = updateActiveAction();
            if (access != null) {
                Log.i("Elmyra/ElmyraService", "Triggering " + access);
                access.onTrigger(detectionProperties);
                for (int i = 0; i < ElmyraService.mFeedbackEffects.size(); i++) {
                    ElmyraService.mFeedbackEffects.get(i).onResolve(detectionProperties);
                }
                latency.setPackageName(access.getClass().getName());
            }
            ElmyraService.mLogger.write(latency);
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    public ElmyraService(Context context, ServiceConfiguration serviceConfiguration) {
        mContext = context;
        mLogger = new MetricsLogger();
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(1, "Elmyra/ElmyraService");
        mActions = new ArrayList<>(serviceConfiguration.getActions());
        Consumer action = obj -> ElmyraActionService( (Action) obj );
        for (Action mAction : mActions) {
            action.accept( mAction );
        }

        mFeedbackEffects = new ArrayList<>(serviceConfiguration.getFeedbackEffects());
        mGates = new ArrayList<>(serviceConfiguration.getGates());
        Consumer action1 = obj -> ElmyraGateService( (Gate) obj );
        for (Gate mGate : mGates) {
            action1.accept( mGate );
        }

        mGestureSensor = serviceConfiguration.getGestureSensor();
        GestureSensor gestureSensor = mGestureSensor;
        if (gestureSensor != null) {
            gestureSensor.setGestureListener(mGestureListener);
        }
        updateSensorListener();
    }

    private void ElmyraActionService(Action action) {
        action.setListener(mActionListener);
    }

    private void ElmyraGateService(Gate gate) {
        gate.setListener(mGateListener);
    }

    private static void activateGates() {
        for (int i = 0; i < mGates.size(); i++) {
            mGates.get(i).activate();
        }
    }

    private static void deactivateGates() {
        for (int i = 0; i < mGates.size(); i++) {
            mGates.get(i).deactivate();
        }
    }

    private static Gate blockingGate() {
        for (int i = 0; i < mGates.size(); i++) {
            if (mGates.get(i).isBlocking()) {
                return mGates.get(i);
            }
        }
        return null;
    }

    private static Action firstAvailableAction() {
        for (int i = 0; i < mActions.size(); i++) {
            if (mActions.get(i).isAvailable()) {
                return mActions.get(i);
            }
        }
        return null;
    }

    private static void startListening() {
        GestureSensor gestureSensor = mGestureSensor;
        if (gestureSensor != null && !gestureSensor.isListening()) {
            mGestureSensor.startListening();
        }
    }

    private void stopListening() {
        GestureSensor gestureSensor = mGestureSensor;
        if (gestureSensor != null && gestureSensor.isListening()) {
            mGestureSensor.stopListening();
            for (int i = 0; i < mFeedbackEffects.size(); i++) {
                mFeedbackEffects.get(i).onRelease();
            }
            Action updateActiveAction = updateActiveAction();
            if (updateActiveAction != null) {
                updateActiveAction.onProgress(0.0f, 0);
            }
        }
    }

    public Action updateActiveAction() {
        Action firstAvailableAction = firstAvailableAction();
        Action action = mLastActiveAction;
        if (!(action == null || firstAvailableAction == action)) {
            Log.i("Elmyra/ElmyraService", "Switching action from "
                    + mLastActiveAction + " to " + firstAvailableAction);
            mLastActiveAction.onProgress(0.0f, 0);
        }
        mLastActiveAction = firstAvailableAction;
        return firstAvailableAction;
    }

    public void updateSensorListener() {
        Action updateActiveAction = updateActiveAction();
        if (updateActiveAction == null) {
            Log.i("Elmyra/ElmyraService", "No available actions");
            deactivateGates();
            stopListening();
            return;
        }
        activateGates();
        Gate blockingGate = blockingGate();
        if (blockingGate != null) {
            Log.i("Elmyra/ElmyraService", "Gated by " + blockingGate);
            stopListening();
            return;
        }
        Log.i("Elmyra/ElmyraService", "Unblocked; current action: " + updateActiveAction);
        startListening();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        String str2;
        printWriter.println(ElmyraService.class.getSimpleName() + " state:");
        printWriter.println("  Gates:");
        int i = 0;
        while (true) {
            str = "X ";
            if (i >= mGates.size()) {
                break;
            }
            printWriter.print("    ");
            if (mGates.get(i).isActive()) {
                if (!mGates.get(i).isBlocking()) {
                    str = "O ";
                }
                printWriter.print(str);
            } else {
                printWriter.print("- ");
            }
            printWriter.println(mGates.get(i).toString());
            i++;
        }
        printWriter.println("  Actions:");
        for (int i2 = 0; i2 < mActions.size(); i2++) {
            printWriter.print("    ");
            if (mActions.get(i2).isAvailable()) {
                str2 = "O ";
            } else {
                str2 = str;
            }
            printWriter.print(str2);
            printWriter.println(mActions.get(i2).toString());
        }
        printWriter.println("  Active: " + mLastActiveAction);
        printWriter.println("  Feedback Effects:");
        for (int i3 = 0; i3 < mFeedbackEffects.size(); i3++) {
            printWriter.print("    ");
            printWriter.println(mFeedbackEffects.get(i3).toString());
        }
        printWriter.println("  Gesture Sensor: " + mGestureSensor.toString());
        GestureSensor gestureSensor = mGestureSensor;
        if (gestureSensor instanceof Dumpable) {
            ((Dumpable) gestureSensor).dump(fileDescriptor, printWriter, strArr);
        }
    }
}


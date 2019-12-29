package com.google.android.systemui.elmyra;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.android.systemui.elmyra.proto.nano.SnapshotProtos.SnapshotHeader;
import com.google.android.systemui.elmyra.sensors.GestureSensor;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import java.util.Random;

public final class SnapshotController implements com.google.android.systemui.elmyra.sensors.GestureSensor.Listener {
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            if (message.what == 1) {
                requestSnapshot((SnapshotHeader) message.obj);
            }
        }
    };
    private int mLastGestureStage = 0;
    private final int mSnapshotDelayAfterGesture;
    private Listener mSnapshotListener;

    public interface Listener {
        void onSnapshotRequested(SnapshotHeader snapshotProtos);
    }

    public SnapshotController(SnapshotConfiguration snapshotConfiguration) {
        mSnapshotDelayAfterGesture = snapshotConfiguration.getSnapshotDelayAfterGesture();
    }

    public void onGestureProgress(GestureSensor gestureSensor, float f, int i) {
        if (mLastGestureStage == 2 && i != 2) {
            SnapshotHeader snapshotProtos = new SnapshotHeader();
            snapshotProtos.identifier = new Random().nextLong();
            snapshotProtos.gestureType = 2;
            requestSnapshot(snapshotProtos);
        }
        mLastGestureStage = i;
    }

    public void onGestureDetected(GestureSensor gestureSensor, DetectionProperties detectionProperties) {
        SnapshotHeader snapshotProtos = new SnapshotHeader();
        snapshotProtos.gestureType = 1;
        snapshotProtos.identifier = detectionProperties != null ? detectionProperties.getActionId() : 0;
        mLastGestureStage = 0;
        Handler handler = mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1, snapshotProtos), (long) mSnapshotDelayAfterGesture);
    }

    public void onWestworldPull() {
        SnapshotHeader snapshotProtos = new SnapshotHeader();
        snapshotProtos.gestureType = 4;
        snapshotProtos.identifier = 0;
        Handler handler = mHandler;
        handler.sendMessage(handler.obtainMessage(1, snapshotProtos));
    }

    public void setListener(Listener listener) {
        mSnapshotListener = listener;
    }

    private void requestSnapshot(SnapshotHeader snapshotProtos) {
        Listener listener = mSnapshotListener;
        if (listener != null) {
            listener.onSnapshotRequested(snapshotProtos);
        }
    }
}
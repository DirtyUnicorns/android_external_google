package com.google.android.systemui.elmyra.sensors;

import android.content.Context;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubManager;
import android.hardware.location.ContextHubManager.Callback;
import android.hardware.location.ContextHubMessage;
import android.hardware.location.NanoAppFilter;
import android.util.Log;
import android.util.TypedValue;
import com.android.systemui.R;
import com.google.android.systemui.elmyra.SnapshotConfiguration;
import com.google.android.systemui.elmyra.SnapshotController;
import com.google.android.systemui.elmyra.proto.nano.CHREMessages.MessageV1;
import com.google.android.systemui.elmyra.proto.nano.ElmyraGestureDetector.AggregateDetector;
import com.google.android.systemui.elmyra.proto.nano.ElmyraGestureDetector.SlopeDetector;
import com.google.android.systemui.elmyra.proto.nano.SnapshotMessages.SnapshotHeader;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import com.google.android.systemui.elmyra.sensors.config.GestureConfiguration;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CHREGestureSensor implements GestureSensor {
    private final Context mContext;
    private Callback mContextHubCallback = new ContextHubCallback();
    private int mContextHubHandle;
    private final ContextHubManager mContextHubManager;
    private final AssistGestureController mController;
    private int mFindNanoAppRetries;
    private final GestureConfiguration mGestureConfiguration;
    private boolean mIsListening;
    private boolean mNanoAppFound;
    private final boolean mNanoAppFoundOnBoot;
    private int mNanoAppHandle;
    private final float mProgressDetectThreshold;
    private final SnapshotController.Listener mSnapshotListener = new SnapshotControllerListenerImpl(this);

    class ContextHubCallback extends Callback {
        ContextHubCallback() {
        }

        public void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage) {
            if (i2 == mNanoAppHandle) {
                if (mNanoAppFound) {
                    try {
                        if (contextHubMessage.getMsgType() == 1) {
                            MessageV1 parseFrom = MessageV1.parseFrom(contextHubMessage.getData());
                            if (parseFrom.hasGestureDetected()) {
                                mController.onGestureDetected(new DetectionProperties(
                                        parseFrom.getGestureDetected().hapticConsumed, parseFrom.getGestureDetected().hostSuspended, /* longSqueeze */ false));
                                return;
                            } else if (parseFrom.hasGestureProgress()) {
                                mController.onGestureProgress(parseFrom.getGestureProgress());
                                return;
                            } else if (parseFrom.hasSnapshot()) {
                                mController.onSnapshotReceived(parseFrom.getSnapshot());
                                return;
                            } else if (parseFrom.hasChassis()) {
                                mController.storeChassisConfiguration(parseFrom.getChassis());
                                return;
                            } else {
                                return;
                            }
                        }
                        return;
                    } catch (Throwable e) {
                        return;
                    }
                }
            }
        }
    }

    public CHREGestureSensor(Context context, GestureConfiguration gestureConfiguration, SnapshotConfiguration snapshotConfiguration) {
        mContext = context;
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.elmyra_progress_detect_threshold, typedValue, true);
        mProgressDetectThreshold = typedValue.getFloat();
        mController = new AssistGestureController(context, this, snapshotConfiguration);
        mController.setSnapshotListener(mSnapshotListener);
        mGestureConfiguration = gestureConfiguration;
        mGestureConfiguration.setListener(new GestureConfigurationListenerImpl(this));
        mContextHubManager = (ContextHubManager) mContext.getSystemService(Context.CONTEXTHUB_SERVICE);
        mNanoAppFoundOnBoot = findNanoApp();
    }

    private byte[] buildGestureDetectorMessage() throws IOException {
        SlopeDetector slopeDetector = new SlopeDetector();
        slopeDetector.sensitivity = mGestureConfiguration.getSlopeSensitivity();
        slopeDetector.upperThreshold = mGestureConfiguration.getUpperThreshold();
        slopeDetector.lowerThreshold = mGestureConfiguration.getLowerThreshold();
        slopeDetector.releaseThreshold = slopeDetector.upperThreshold * 0.1f;
        slopeDetector.timeThreshold = (long) mGestureConfiguration.getTimeWindow();
        AggregateDetector aggregateDetector = new AggregateDetector();
        aggregateDetector.count = 6;
        aggregateDetector.detector = slopeDetector;
        MessageV1 messageV1 = new MessageV1();
        messageV1.setAggregateDetector(aggregateDetector);
        return serializeProtobuf(messageV1);
    }

    private byte[] buildProgressReportThresholdMessage() throws IOException {
        MessageV1 messageV1 = new MessageV1();
        messageV1.setProgressReportThreshold(mProgressDetectThreshold);
        return serializeProtobuf(messageV1);
    }

    private byte[] buildRecognizerStartMessage(boolean z) throws IOException {
        MessageV1 messageV1 = new MessageV1();
        messageV1.setRecognizerStart(z);
        return serializeProtobuf(messageV1);
    }

    private byte[] buildRequestCalibrationMessage() throws IOException {
        MessageV1 messageV1 = new MessageV1();
        messageV1.setCalibrationRequest(true);
        return serializeProtobuf(messageV1);
    }

    private byte[] buildRequestSnapshotMessage(SnapshotHeader snapshotHeader) throws IOException {
        MessageV1 messageV1 = new MessageV1();
        messageV1.setSnapshotRequest(snapshotHeader);
        return serializeProtobuf(messageV1);
    }

    private boolean findNanoApp() {
        if (mNanoAppFound) {
            return true;
        }
        mFindNanoAppRetries++;
        List contextHubs = mContextHubManager.getContextHubs();
        if (contextHubs.size() == 0) {
            return false;
        }
        mContextHubHandle = ((ContextHubInfo) contextHubs.get(0)).getId();
        try {
            mContextHubManager.queryNanoApps((ContextHubInfo) contextHubs.get(0)).waitForResponse(5, TimeUnit.SECONDS);
            int[] findNanoAppOnHub = mContextHubManager.findNanoAppOnHub(-1, new NanoAppFilter(5147455389092024334L, 1, 0, 306812249964L));
            if (findNanoAppOnHub.length != 1) {
                return false;
            }
            mNanoAppFound = true;
            mNanoAppHandle = findNanoAppOnHub[0];
            return true;
        } catch (InterruptedException e) {
            return false;
        } catch (TimeoutException e2) {
            return false;
        }
    }

    private void requestCalibration() {
        try {
            sendMessageToNanoApp(new ContextHubMessage(1, -1, buildRequestCalibrationMessage()));
        } catch (Throwable suppress) { /* do nothing */ }
    }

    protected void requestSnapshot(SnapshotHeader snapshotHeader) {
        try {
            sendMessageToNanoApp(new ContextHubMessage(1, -1, buildRequestSnapshotMessage(snapshotHeader)));
        } catch (Throwable e) { /* do nothing */ }
    }

    private byte[] serializeProtobuf(MessageV1 messageV1) throws IOException {
        byte[] bArr = new byte[messageV1.getSerializedSize()];
        messageV1.writeTo(CodedOutputByteBufferNano.newInstance(bArr));
        return bArr;
    }

    protected void updateSensorConfiguration() {
        if (!mNanoAppFound && !findNanoApp()) {
            return;
        }
        try {
            sendMessageToNanoApp(new ContextHubMessage(1, -1, buildGestureDetectorMessage()));
            sendMessageToNanoApp(new ContextHubMessage(1, -1, buildProgressReportThresholdMessage()));
        } catch (Throwable suppressed) { /* do nothing */ }
    }

    public boolean isListening() {
        return mIsListening;
    }

    void sendMessageToNanoApp(ContextHubMessage contextHubMessage) {
        mContextHubManager.sendMessage(mContextHubHandle, mNanoAppHandle, contextHubMessage);
    }

    public void setGestureListener(GestureSensor.Listener listener) {
        mController.setGestureListener(listener);
    }

    public void startListening() {
        if (!mNanoAppFound || !findNanoApp()) {
            return;
        }
        if (!mIsListening) {
            updateSensorConfiguration();
            try {
                sendMessageToNanoApp(new ContextHubMessage(1, -1, buildRecognizerStartMessage(true)));
                mIsListening = true;
                mContextHubManager.registerCallback(mContextHubCallback);
            } catch (Throwable suppress) { /* do nothing */ }
            if (mController.getChassisConfiguration() == null) {
                requestCalibration();
            }
        }
    }

    public void stopListening() {
        if (!mNanoAppFound || !findNanoApp()) {
            return;
        }
        if (mIsListening) {
            try {
                sendMessageToNanoApp(new ContextHubMessage(1, -1, buildRecognizerStartMessage(false)));
                mContextHubManager.unregisterCallback(mContextHubCallback);
                mIsListening = false;
            } catch (Throwable suppress) { /* do nothing */ }
        }
    }

    private class GestureConfigurationListenerImpl implements GestureConfiguration.Listener {
        private final CHREGestureSensor cHREGestureSensor;

        public GestureConfigurationListenerImpl(CHREGestureSensor cHREGestureSens) {
            cHREGestureSensor = cHREGestureSens;
        }

        @Override
        public final void onGestureConfigurationChanged(GestureConfiguration gestureConfiguration) {
            cHREGestureSensor.updateSensorConfiguration();
        }
    }

    private class SnapshotControllerListenerImpl implements SnapshotController.Listener {
        private final CHREGestureSensor cHREGestureSensor;

        public SnapshotControllerListenerImpl(CHREGestureSensor cHREGestureSens) {
            cHREGestureSensor = cHREGestureSens;
        }

        @Override
        public final void onSnapshotRequested(SnapshotHeader snapshotHeader) {
            cHREGestureSensor.requestSnapshot(snapshotHeader);
        }
    }
}
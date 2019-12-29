package com.google.android.systemui.elmyra.sensors;

import android.content.Context;
import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubClientCallback;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubManager;
import android.hardware.location.NanoAppMessage;
import android.util.Log;
import android.util.TypedValue;
import com.android.systemui.R;
import com.android.systemui.Dumpable;
import com.google.android.systemui.elmyra.SnapshotConfiguration;
import com.google.android.systemui.elmyra.SnapshotController;
import com.google.android.systemui.elmyra.proto.nano.ChassisProtos.Chassis;
import com.google.android.systemui.elmyra.proto.nano.ContextHubMessages;
import com.google.android.systemui.elmyra.proto.nano.ContextHubMessages.GestureDetected;
import com.google.android.systemui.elmyra.proto.nano.ContextHubMessages.GestureProgress;
import com.google.android.systemui.elmyra.proto.nano.ContextHubMessages.RecognizerStart;
import com.google.android.systemui.elmyra.proto.nano.ContextHubMessages.SensitivityUpdate;
import com.google.android.systemui.elmyra.proto.nano.SnapshotProtos;
import com.google.android.systemui.elmyra.proto.nano.SnapshotProtos.Snapshot;
import com.google.android.systemui.elmyra.proto.nano.SnapshotProtos.SnapshotHeader;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import com.google.android.systemui.elmyra.sensors.config.GestureConfiguration;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class CHREGestureSensor implements Dumpable, GestureSensor {
    private final Context mContext;
    private ContextHubClient mContextHubClient;
    private final ContextHubClientCallback mContextHubClientCallback = new ContextHubClientCallback() {
        public void onMessageFromNanoApp(ContextHubClient contextHubClient, NanoAppMessage nanoAppMessage) {
            String str = "Elmyra/GestureSensor";
            if (nanoAppMessage.getNanoAppId() == 5147455389092024334L) {
                try {
                    int messageType = nanoAppMessage.getMessageType();
                    if (messageType != 1) {
                        switch (messageType) {
                            case 300:
                                mController.onGestureProgress(GestureProgress.parseFrom(nanoAppMessage.getMessageBody()).progress);
                                break;
                            case 301:
                                GestureDetected parseFrom = GestureDetected.parseFrom(nanoAppMessage.getMessageBody());
                                mController.onGestureDetected(new DetectionProperties(parseFrom.hapticConsumed, parseFrom.hostSuspended, /* longSqueeze */ false));
                                break;
                            case 302:
                                Snapshot parseFrom2 = Snapshot.parseFrom(nanoAppMessage.getMessageBody());
                                parseFrom2.sensitivitySetting = mGestureConfiguration.getSensitivity();
                                mController.onSnapshotReceived(parseFrom2);
                                break;
                            case 303:
                                mController.storeChassisConfiguration(Chassis.parseFrom(nanoAppMessage.getMessageBody()));
                                break;
                            case 304:
                            case 305:
                                break;
                            default:
                                StringBuilder sb = new StringBuilder();
                                sb.append("Unknown message type: ");
                                sb.append(nanoAppMessage.getMessageType());
                                Log.e(str, sb.toString());
                                break;
                        }
                    } else if (mIsListening) {
                        startRecognizer();
                    }
                } catch (InvalidProtocolBufferNanoException e) {
                    Log.e(str, "Invalid protocol buffer", e);
                }
            }
        }

        public void onHubReset(ContextHubClient contextHubClient) {
            StringBuilder sb = new StringBuilder();
            sb.append("HubReset: ");
            sb.append(contextHubClient.getAttachedHub().getId());
            Log.d("Elmyra/GestureSensor", sb.toString());
        }

        public void onNanoAppAborted(ContextHubClient contextHubClient, long j, int i) {
            if (j == 5147455389092024334L) {
                StringBuilder sb = new StringBuilder();
                sb.append("Nanoapp aborted, code: ");
                sb.append(i);
                Log.e("Elmyra/GestureSensor", sb.toString());
            }
        }
    };
    private int mContextHubRetryCount;
    private final AssistGestureController mController;
    private final GestureConfiguration mGestureConfiguration;
    private boolean mIsListening;
    private final float mProgressDetectThreshold;

    public CHREGestureSensor(Context context, GestureConfiguration gestureConfiguration, SnapshotConfiguration snapshotConfiguration) {
        mContext = context;
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.elmyra_progress_detect_threshold, typedValue, true);
        mProgressDetectThreshold = typedValue.getFloat();
        mController = new AssistGestureController(context, this, gestureConfiguration, snapshotConfiguration);
        mController.setSnapshotListener((snapshotProtos) -> sendMessageToNanoApp(203, MessageNano.toByteArray(snapshotProtos)));
        mGestureConfiguration = gestureConfiguration;
        mGestureConfiguration.setListener(new GestureConfiguration.Listener() {
            public final void onGestureConfigurationChanged(GestureConfiguration gestureConfiguration) {
                updateSensitivity(gestureConfiguration);
            }
        });
        initializeContextHubClientIfNull();
    }

    public void startListening() {
        mIsListening = true;
        startRecognizer();
    }

    public boolean isListening() {
        return mIsListening;
    }

    public void stopListening() {
        sendMessageToNanoApp(201, new byte[0]);
        mIsListening = false;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        sb.append(CHREGestureSensor.class.getSimpleName());
        sb.append(" state:");
        printWriter.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  mIsListening: ");
        sb2.append(mIsListening);
        printWriter.println(sb2.toString());
        if (mContextHubClient == null) {
            printWriter.println("  mContextHubClient is null. Likely no context hubs were found");
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append("  mContextHubRetryCount: ");
        sb3.append(mContextHubRetryCount);
        printWriter.println(sb3.toString());
    }

    public void setGestureListener(GestureSensor.Listener listener) {
        mController.setGestureListener(listener);
    }

    private void initializeContextHubClientIfNull() {
        if (mContextHubClient == null) {
            ContextHubManager contextHubManager = (ContextHubManager) mContext.getSystemService(Context.CONTEXTHUB_SERVICE);
            List contextHubs = contextHubManager.getContextHubs();
            if (contextHubs.size() == 0) {
                Log.e("Elmyra/GestureSensor", "No context hubs found");
            } else {
                mContextHubClient = contextHubManager.createClient((ContextHubInfo) contextHubs.get(0), mContextHubClientCallback);
                mContextHubRetryCount++;
            }
        }
    }

    private void updateSensitivity(GestureConfiguration gestureConfiguration) {
        SensitivityUpdate contextHubMessages = new SensitivityUpdate();
        contextHubMessages.sensitivity = gestureConfiguration.getSensitivity();
        sendMessageToNanoApp(202, MessageNano.toByteArray(contextHubMessages));
    }

    private void startRecognizer() {
        RecognizerStart contextHubMessages = new RecognizerStart();
        contextHubMessages.progressReportThreshold = mProgressDetectThreshold;
        contextHubMessages.sensitivity = mGestureConfiguration.getSensitivity();
        sendMessageToNanoApp(200, MessageNano.toByteArray(contextHubMessages));
        if (mController.getChassisConfiguration() == null) {
            sendMessageToNanoApp(204, new byte[0]);
        }
    }

    private void sendMessageToNanoApp(int i, byte[] bArr) {
        initializeContextHubClientIfNull();
        String str = "Elmyra/GestureSensor";
        if (mContextHubClient == null) {
            Log.e(str, "ContextHubClient null");
            return;
        }
        int sendMessageToNanoApp = mContextHubClient.sendMessageToNanoApp(NanoAppMessage.createMessageToNanoApp(5147455389092024334L, i, bArr));
        if (sendMessageToNanoApp != 0) {
            Log.e(str, String.format("Unable to send message %d to nanoapp, error code %d", new Object[]{Integer.valueOf(i), Integer.valueOf(sendMessageToNanoApp)}));
        }
    }
}
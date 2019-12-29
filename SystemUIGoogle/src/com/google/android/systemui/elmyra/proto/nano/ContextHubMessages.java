package com.google.android.systemui.elmyra.proto.nano;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;

public interface ContextHubMessages {

    public static final class GestureDetected extends MessageNano {
        public boolean hapticConsumed;
        public boolean hostSuspended;

        public GestureDetected() {
            clear();
        }

        public GestureDetected clear() {
            hostSuspended = false;
            hapticConsumed = false;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            boolean z = hostSuspended;
            if (z) {
                codedOutputByteBufferNano.writeBool(1, z);
            }
            boolean z2 = hapticConsumed;
            if (z2) {
                codedOutputByteBufferNano.writeBool(2, z2);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            boolean z = hostSuspended;
            if (z) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(1, z);
            }
            boolean z2 = hapticConsumed;
            return z2 ? computeSerializedSize + CodedOutputByteBufferNano.computeBoolSize(2, z2) : computeSerializedSize;
        }

        public GestureDetected mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 8) {
                    hostSuspended = codedInputByteBufferNano.readBool();
                } else if (readTag == 16) {
                    hapticConsumed = codedInputByteBufferNano.readBool();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }

        public static GestureDetected parseFrom(byte[] bArr) throws InvalidProtocolBufferNanoException {
            GestureDetected contextHubMessages$GestureDetected = new GestureDetected();
            MessageNano.mergeFrom(contextHubMessages$GestureDetected, bArr);
            return contextHubMessages$GestureDetected;
        }
    }
    public static final class GestureProgress extends MessageNano {
        public float progress;

        public GestureProgress() {
            clear();
        }

        public GestureProgress clear() {
            progress = 0.0f;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (Float.floatToIntBits(progress) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(1, progress);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            return Float.floatToIntBits(progress) != Float.floatToIntBits(0.0f) ? computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(1, progress) : computeSerializedSize;
        }

        public GestureProgress mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 13) {
                    progress = codedInputByteBufferNano.readFloat();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }

        public static GestureProgress parseFrom(byte[] bArr) throws InvalidProtocolBufferNanoException {
            GestureProgress contextHubMessages$GestureProgress = new GestureProgress();
            MessageNano.mergeFrom(contextHubMessages$GestureProgress, bArr);
            return contextHubMessages$GestureProgress;
        }
    }

    public static  final class RecognizerStart extends MessageNano {
        public float progressReportThreshold;
        public float sensitivity;

        public RecognizerStart() {
            clear();
        }

        public RecognizerStart clear() {
            progressReportThreshold = 0.0f;
            sensitivity = 0.0f;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (Float.floatToIntBits(progressReportThreshold) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(1, progressReportThreshold);
            }
            if (Float.floatToIntBits(sensitivity) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(2, sensitivity);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if (Float.floatToIntBits(progressReportThreshold) != Float.floatToIntBits(0.0f)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(1, progressReportThreshold);
            }
            return Float.floatToIntBits(sensitivity) != Float.floatToIntBits(0.0f) ? computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(2, sensitivity) : computeSerializedSize;
        }

        public RecognizerStart mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 13) {
                    progressReportThreshold = codedInputByteBufferNano.readFloat();
                } else if (readTag == 21) {
                    sensitivity = codedInputByteBufferNano.readFloat();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    public static final class SensitivityUpdate extends MessageNano {
        public float sensitivity;

        public SensitivityUpdate() {
            clear();
        }

        public SensitivityUpdate clear() {
            sensitivity = 0.0f;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (Float.floatToIntBits(sensitivity) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(1, sensitivity);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            return Float.floatToIntBits(sensitivity) != Float.floatToIntBits(0.0f) ? computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(1, sensitivity) : computeSerializedSize;
        }

        public SensitivityUpdate mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 13) {
                    sensitivity = codedInputByteBufferNano.readFloat();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

}
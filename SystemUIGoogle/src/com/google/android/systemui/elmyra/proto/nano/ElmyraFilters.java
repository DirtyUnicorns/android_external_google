package com.google.android.systemui.elmyra.proto.nano;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;


public interface ElmyraFilters {

    final class FIRFilter extends MessageNano {
        public float[] coefficients;

        public FIRFilter() {
            clear();
        }

        public FIRFilter clear() {
            coefficients = WireFormatNano.EMPTY_FLOAT_ARRAY;
            cachedSize = -1;
            return this;
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            return (coefficients == null || coefficients.length <= 0) ? computeSerializedSize : (computeSerializedSize + (coefficients.length * 4)) + (coefficients.length * 1);
        }

        public FIRFilter mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                int pushLimit;
                if (readTag == 0) {
                    break;
                } else if (readTag == 10) {
                    readTag = codedInputByteBufferNano.readRawVarint32();
                    pushLimit = codedInputByteBufferNano.pushLimit(readTag);
                    int i = readTag / 4;
                    readTag = coefficients == null ? 0 : coefficients.length;
                    float[] obj = new float[(i + readTag)];
                    if (readTag != 0) {
                        System.arraycopy(coefficients, 0, obj, 0, readTag);
                    }
                    while (readTag < obj.length) {
                        obj[readTag] = codedInputByteBufferNano.readFloat();
                        readTag++;
                    }
                    coefficients = obj;
                    codedInputByteBufferNano.popLimit(pushLimit);
                } else if (readTag == 13) {
                    pushLimit = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 13);
                    readTag = coefficients == null ? 0 : coefficients.length;
                    float[] obj2 = new float[(pushLimit + readTag)];
                    if (readTag != 0) {
                        System.arraycopy(coefficients, 0, obj2, 0, readTag);
                    }
                    while (readTag < obj2.length - 1) {
                        obj2[readTag] = codedInputByteBufferNano.readFloat();
                        codedInputByteBufferNano.readTag();
                        readTag++;
                    }
                    obj2[readTag] = codedInputByteBufferNano.readFloat();
                    coefficients = obj2;
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    break;
                }
            }
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (coefficients != null && coefficients.length > 0) {
                for (float writeFloat : coefficients) {
                    codedOutputByteBufferNano.writeFloat(1, writeFloat);
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    final class ElmyraSnapshot extends MessageNano {
        public ChassisProtos.Chassis chassis;
        public SnapshotProtos.Snapshot snapshot;

        public ElmyraSnapshot() {
            clear();
        }

        public ElmyraSnapshot clear() {
            snapshot = null;
            chassis = null;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            SnapshotProtos.Snapshot snapshotProtos$Snapshot = snapshot;
            if (snapshotProtos$Snapshot != null) {
                codedOutputByteBufferNano.writeMessage(1, snapshotProtos$Snapshot);
            }
            ChassisProtos.Chassis chassisProtos$Chassis = chassis;
            if (chassisProtos$Chassis != null) {
                codedOutputByteBufferNano.writeMessage(2, chassisProtos$Chassis);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            SnapshotProtos.Snapshot snapshotProtos$Snapshot = snapshot;
            if (snapshotProtos$Snapshot != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, snapshotProtos$Snapshot);
            }
            ChassisProtos.Chassis chassisProtos$Chassis = chassis;
            return chassisProtos$Chassis != null ? computeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(2, chassisProtos$Chassis) : computeSerializedSize;
        }

        public ElmyraSnapshot mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 10) {
                    if (snapshot == null) {
                        snapshot = new SnapshotProtos.Snapshot();
                    }
                    codedInputByteBufferNano.readMessage(snapshot);
                } else if (readTag == 18) {
                    if (chassis == null) {
                        chassis = new ChassisProtos.Chassis();
                    }
                    codedInputByteBufferNano.readMessage(chassis);
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    final class Filter extends MessageNano {
        private static volatile Filter[] _emptyArray;
        private int parametersCase_ = 0;
        private Object parameters_;

        public Filter() {
            clear();
        }

        public static Filter[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Filter[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Filter clearParameters() {
            parametersCase_ = 0;
            parameters_ = null;
            return this;
        }

        public Filter clear() {
            clearParameters();
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (parametersCase_ == 1) {
                codedOutputByteBufferNano.writeMessage(1, (MessageNano) parameters_);
            }
            if (parametersCase_ == 2) {
                codedOutputByteBufferNano.writeMessage(2, (MessageNano) parameters_);
            }
            if (parametersCase_ == 3) {
                codedOutputByteBufferNano.writeMessage(3, (MessageNano) parameters_);
            }
            if (parametersCase_ == 4) {
                codedOutputByteBufferNano.writeMessage(4, (MessageNano) parameters_);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if (parametersCase_ == 1) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, (MessageNano) parameters_);
            }
            if (parametersCase_ == 2) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, (MessageNano) parameters_);
            }
            if (parametersCase_ == 3) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, (MessageNano) parameters_);
            }
            return parametersCase_ == 4 ? computeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(4, (MessageNano) parameters_) : computeSerializedSize;
        }

        public Filter mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 10) {
                    if (parametersCase_ != 1) {
                        parameters_ = new FIRFilter();
                    }
                    codedInputByteBufferNano.readMessage((MessageNano) parameters_);
                    parametersCase_ = 1;
                } else if (readTag == 18) {
                    if (parametersCase_ != 2) {
                        parameters_ = new HighpassFilter();
                    }
                    codedInputByteBufferNano.readMessage((MessageNano) parameters_);
                    parametersCase_ = 2;
                } else if (readTag == 26) {
                    if (parametersCase_ != 3) {
                        parameters_ = new LowpassFilter();
                    }
                    codedInputByteBufferNano.readMessage((MessageNano) parameters_);
                    parametersCase_ = 3;
                } else if (readTag == 34) {
                    if (parametersCase_ != 4) {
                        parameters_ = new MedianFilter();
                    }
                    codedInputByteBufferNano.readMessage((MessageNano) parameters_);
                    parametersCase_ = 4;
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    final class HighpassFilter extends MessageNano {
        public float cutoff;
        public float rate;

        public HighpassFilter() {
            clear();
        }

        public HighpassFilter clear() {
            cutoff = 0.0f;
            rate = 0.0f;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (Float.floatToIntBits(cutoff) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(1, cutoff);
            }
            if (Float.floatToIntBits(rate) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(2, rate);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        protected int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if (Float.floatToIntBits(cutoff) != Float.floatToIntBits(0.0f)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(1, cutoff);
            }
            return Float.floatToIntBits(rate) != Float.floatToIntBits(0.0f) ? computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(2, rate) : computeSerializedSize;
        }

        public HighpassFilter mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 13) {
                    cutoff = codedInputByteBufferNano.readFloat();
                } else if (readTag == 21) {
                    rate = codedInputByteBufferNano.readFloat();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    final class LowpassFilter extends MessageNano {
        public float cutoff;
        public float rate;

        public LowpassFilter() {
            clear();
        }

        public LowpassFilter clear() {
            cutoff = 0.0f;
            rate = 0.0f;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (Float.floatToIntBits(cutoff) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(1, cutoff);
            }
            if (Float.floatToIntBits(rate) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(2, rate);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if (Float.floatToIntBits(cutoff) != Float.floatToIntBits(0.0f)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(1, cutoff);
            }
            return Float.floatToIntBits(rate) != Float.floatToIntBits(0.0f) ? computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(2, rate) : computeSerializedSize;
        }

        public LowpassFilter mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 13) {
                    cutoff = codedInputByteBufferNano.readFloat();
                } else if (readTag == 21) {
                    rate = codedInputByteBufferNano.readFloat();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    final class MedianFilter extends MessageNano {
        public int windowSize;

        public MedianFilter() {
            clear();
        }

        public MedianFilter clear() {
            windowSize = 0;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            int i = windowSize;
            if (i != 0) {
                codedOutputByteBufferNano.writeUInt32(1, i);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            int i = windowSize;
            return i != 0 ? computeSerializedSize + CodedOutputByteBufferNano.computeUInt32Size(1, i) : computeSerializedSize;
        }

        public MedianFilter mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 8) {
                    windowSize = codedInputByteBufferNano.readUInt32();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }
}
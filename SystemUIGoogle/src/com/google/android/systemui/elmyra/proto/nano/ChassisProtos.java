package com.google.android.systemui.elmyra.proto.nano;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;

public interface ChassisProtos {

    public static final class Chassis extends MessageNano {

    public ElmyraFilters.Filter[] defaultFilters;
    public Sensor[] sensors;

    public Chassis() {
        clear();
    }

    public Chassis clear() {
        sensors = Sensor.emptyArray();
        defaultFilters = ElmyraFilters.Filter.emptyArray();
        cachedSize = -1;
        return this;
    }

    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        Sensor[] chassisProtos$SensorArr = sensors;
        int i = 0;
        if (chassisProtos$SensorArr != null && chassisProtos$SensorArr.length > 0) {
            int i2 = 0;
            while (true) {
                Sensor[] chassisProtos$SensorArr2 = sensors;
                if (i2 >= chassisProtos$SensorArr2.length) {
                    break;
                }
                Sensor chassisProtos$Sensor = chassisProtos$SensorArr2[i2];
                if (chassisProtos$Sensor != null) {
                    codedOutputByteBufferNano.writeMessage(1, chassisProtos$Sensor);
                }
                i2++;
            }
        }
        ElmyraFilters.Filter[] elmyraFilters$FilterArr = defaultFilters;
        if (elmyraFilters$FilterArr != null && elmyraFilters$FilterArr.length > 0) {
            while (true) {
                ElmyraFilters.Filter[] elmyraFilters$FilterArr2 = defaultFilters;
                if (i >= elmyraFilters$FilterArr2.length) {
                    break;
                }
                ElmyraFilters.Filter elmyraFilters$Filter = elmyraFilters$FilterArr2[i];
                if (elmyraFilters$Filter != null) {
                    codedOutputByteBufferNano.writeMessage(2, elmyraFilters$Filter);
                }
                i++;
            }
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        Sensor[] chassisProtos$SensorArr = sensors;
        int i = 0;
        if (chassisProtos$SensorArr != null && chassisProtos$SensorArr.length > 0) {
            int i2 = computeSerializedSize;
            int i3 = 0;
            while (true) {
                Sensor[] chassisProtos$SensorArr2 = sensors;
                if (i3 >= chassisProtos$SensorArr2.length) {
                    break;
                }
                Sensor chassisProtos$Sensor = chassisProtos$SensorArr2[i3];
                if (chassisProtos$Sensor != null) {
                    i2 += CodedOutputByteBufferNano.computeMessageSize(1, chassisProtos$Sensor);
                }
                i3++;
            }
            computeSerializedSize = i2;
        }
        ElmyraFilters.Filter[] elmyraFilters$FilterArr = defaultFilters;
        if (elmyraFilters$FilterArr != null && elmyraFilters$FilterArr.length > 0) {
            while (true) {
                ElmyraFilters.Filter[] elmyraFilters$FilterArr2 = defaultFilters;
                if (i >= elmyraFilters$FilterArr2.length) {
                    break;
                }
                ElmyraFilters.Filter elmyraFilters$Filter = elmyraFilters$FilterArr2[i];
                if (elmyraFilters$Filter != null) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, elmyraFilters$Filter);
                }
                i++;
            }
        }
        return computeSerializedSize;
    }

    public Chassis mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            if (readTag == 0) {
                return this;
            }
            if (readTag == 10) {
                int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 10);
                Sensor[] chassisProtos$SensorArr = sensors;
                int length = chassisProtos$SensorArr == null ? 0 : chassisProtos$SensorArr.length;
                Sensor[] chassisProtos$SensorArr2 = new Sensor[(repeatedFieldArrayLength + length)];
                if (length != 0) {
                    System.arraycopy(sensors, 0, chassisProtos$SensorArr2, 0, length);
                }
                while (length < chassisProtos$SensorArr2.length - 1) {
                    chassisProtos$SensorArr2[length] = new Sensor();
                    codedInputByteBufferNano.readMessage(chassisProtos$SensorArr2[length]);
                    codedInputByteBufferNano.readTag();
                    length++;
                }
                chassisProtos$SensorArr2[length] = new Sensor();
                codedInputByteBufferNano.readMessage(chassisProtos$SensorArr2[length]);
                sensors = chassisProtos$SensorArr2;
            } else if (readTag == 18) {
                int repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 18);
                ElmyraFilters.Filter[] elmyraFilters$FilterArr = defaultFilters;
                int length2 = elmyraFilters$FilterArr == null ? 0 : elmyraFilters$FilterArr.length;
                ElmyraFilters.Filter[] elmyraFilters$FilterArr2 = new ElmyraFilters.Filter[(repeatedFieldArrayLength2 + length2)];
                if (length2 != 0) {
                    System.arraycopy(defaultFilters, 0, elmyraFilters$FilterArr2, 0, length2);
                }
                while (length2 < elmyraFilters$FilterArr2.length - 1) {
                    elmyraFilters$FilterArr2[length2] = new ElmyraFilters.Filter();
                    codedInputByteBufferNano.readMessage(elmyraFilters$FilterArr2[length2]);
                    codedInputByteBufferNano.readTag();
                    length2++;
                }
                elmyraFilters$FilterArr2[length2] = new ElmyraFilters.Filter();
                codedInputByteBufferNano.readMessage(elmyraFilters$FilterArr2[length2]);
                defaultFilters = elmyraFilters$FilterArr2;
            } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                return this;
            }
        }
    }

    public static Chassis parseFrom(byte[] bArr) throws InvalidProtocolBufferNanoException {
        Chassis chassisProtos$Chassis = new Chassis();
        MessageNano.mergeFrom(chassisProtos$Chassis, bArr);
        return chassisProtos$Chassis;
    }
}

    public static final class Sensor extends MessageNano {
        private static volatile Sensor[] _emptyArray;
        public ElmyraFilters.Filter[] filters;
        public int gain;
        public float sensitivity;
        public int source;

        public static Sensor[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Sensor[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Sensor() {
            clear();
        }

        public Sensor clear() {
            source = 0;
            gain = 0;
            sensitivity = 0.0f;
            filters = ElmyraFilters.Filter.emptyArray();
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            int i = source;
            if (i != 0) {
                codedOutputByteBufferNano.writeUInt32(1, i);
            }
            int i2 = gain;
            if (i2 != 0) {
                codedOutputByteBufferNano.writeInt32(2, i2);
            }
            if (Float.floatToIntBits(sensitivity) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(3, sensitivity);
            }
            ElmyraFilters.Filter[] elmyraFilters$FilterArr = filters;
            if (elmyraFilters$FilterArr != null && elmyraFilters$FilterArr.length > 0) {
                int i3 = 0;
                while (true) {
                    ElmyraFilters.Filter[] elmyraFilters$FilterArr2 = filters;
                    if (i3 >= elmyraFilters$FilterArr2.length) {
                        break;
                    }
                    ElmyraFilters.Filter elmyraFilters$Filter = elmyraFilters$FilterArr2[i3];
                    if (elmyraFilters$Filter != null) {
                        codedOutputByteBufferNano.writeMessage(4, elmyraFilters$Filter);
                    }
                    i3++;
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            int i = source;
            if (i != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt32Size(1, i);
            }
            int i2 = gain;
            if (i2 != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            if (Float.floatToIntBits(sensitivity) != Float.floatToIntBits(0.0f)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(3, sensitivity);
            }
            ElmyraFilters.Filter[] elmyraFilters$FilterArr = filters;
            if (elmyraFilters$FilterArr != null && elmyraFilters$FilterArr.length > 0) {
                int i3 = 0;
                while (true) {
                    ElmyraFilters.Filter[] elmyraFilters$FilterArr2 = filters;
                    if (i3 >= elmyraFilters$FilterArr2.length) {
                        break;
                    }
                    ElmyraFilters.Filter elmyraFilters$Filter = elmyraFilters$FilterArr2[i3];
                    if (elmyraFilters$Filter != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, elmyraFilters$Filter);
                    }
                    i3++;
                }
            }
            return computeSerializedSize;
        }

        public Sensor mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 8) {
                    source = codedInputByteBufferNano.readUInt32();
                } else if (readTag == 16) {
                    gain = codedInputByteBufferNano.readInt32();
                } else if (readTag == 29) {
                    sensitivity = codedInputByteBufferNano.readFloat();
                } else if (readTag == 34) {
                    int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 34);
                    ElmyraFilters.Filter[] elmyraFilters$FilterArr = filters;
                    int length = elmyraFilters$FilterArr == null ? 0 : elmyraFilters$FilterArr.length;
                    ElmyraFilters.Filter[] elmyraFilters$FilterArr2 = new ElmyraFilters.Filter[(repeatedFieldArrayLength + length)];
                    if (length != 0) {
                        System.arraycopy(filters, 0, elmyraFilters$FilterArr2, 0, length);
                    }
                    while (length < elmyraFilters$FilterArr2.length - 1) {
                        elmyraFilters$FilterArr2[length] = new ElmyraFilters.Filter();
                        codedInputByteBufferNano.readMessage(elmyraFilters$FilterArr2[length]);
                        codedInputByteBufferNano.readTag();
                        length++;
                    }
                    elmyraFilters$FilterArr2[length] = new ElmyraFilters.Filter();
                    codedInputByteBufferNano.readMessage(elmyraFilters$FilterArr2[length]);
                    filters = elmyraFilters$FilterArr2;
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }
    public static final class SensorEvent extends MessageNano {
        public long timestamp;
        public float[] values;

        public SensorEvent() {
            clear();
        }

        public SensorEvent clear() {
            timestamp = 0;
            values = WireFormatNano.EMPTY_FLOAT_ARRAY;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            long j = timestamp;
            if (j != 0) {
                codedOutputByteBufferNano.writeUInt64(1, j);
            }
            float[] fArr = values;
            if (fArr != null && fArr.length > 0) {
                int i = 0;
                while (true) {
                    float[] fArr2 = values;
                    if (i >= fArr2.length) {
                        break;
                    }
                    codedOutputByteBufferNano.writeFloat(2, fArr2[i]);
                    i++;
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            long j = timestamp;
            if (j != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(1, j);
            }
            float[] fArr = values;
            return (fArr == null || fArr.length <= 0) ? computeSerializedSize : computeSerializedSize + (fArr.length * 4) + (fArr.length * 1);
        }

        public SensorEvent mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 8) {
                    timestamp = codedInputByteBufferNano.readUInt64();
                } else if (readTag == 18) {
                    int readRawVarint32 = codedInputByteBufferNano.readRawVarint32();
                    int pushLimit = codedInputByteBufferNano.pushLimit(readRawVarint32);
                    int i = readRawVarint32 / 4;
                    float[] fArr = values;
                    int length = fArr == null ? 0 : fArr.length;
                    float[] fArr2 = new float[(i + length)];
                    if (length != 0) {
                        System.arraycopy(values, 0, fArr2, 0, length);
                    }
                    while (length < fArr2.length) {
                        fArr2[length] = codedInputByteBufferNano.readFloat();
                        length++;
                    }
                    values = fArr2;
                    codedInputByteBufferNano.popLimit(pushLimit);
                } else if (readTag == 21) {
                    int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 21);
                    float[] fArr3 = values;
                    int length2 = fArr3 == null ? 0 : fArr3.length;
                    float[] fArr4 = new float[(repeatedFieldArrayLength + length2)];
                    if (length2 != 0) {
                        System.arraycopy(values, 0, fArr4, 0, length2);
                    }
                    while (length2 < fArr4.length - 1) {
                        fArr4[length2] = codedInputByteBufferNano.readFloat();
                        codedInputByteBufferNano.readTag();
                        length2++;
                    }
                    fArr4[length2] = codedInputByteBufferNano.readFloat();
                    values = fArr4;
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

}
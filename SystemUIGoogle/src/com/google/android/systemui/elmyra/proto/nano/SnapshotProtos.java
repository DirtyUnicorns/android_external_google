package com.google.android.systemui.elmyra.proto.nano;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;

public interface SnapshotProtos {

    final class Snapshot extends MessageNano {
        private static volatile Snapshot[] _emptyArray;
        public Event[] events;
        public SnapshotHeader header;
        public float sensitivitySetting;

        public Snapshot() {
            clear();
        }

        public static Snapshot[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Snapshot[0];
                    }
                }
            }
            return _emptyArray;
        }

        public static Snapshot parseFrom(byte[] bArr) throws InvalidProtocolBufferNanoException {
            Snapshot snapshotProtos$Snapshot = new Snapshot();
            MessageNano.mergeFrom(snapshotProtos$Snapshot, bArr);
            return snapshotProtos$Snapshot;
        }

        public Snapshot clear() {
            header = null;
            events = Event.emptyArray();
            sensitivitySetting = 0.0f;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            SnapshotHeader snapshotProtos$SnapshotHeader = header;
            if (snapshotProtos$SnapshotHeader != null) {
                codedOutputByteBufferNano.writeMessage(1, snapshotProtos$SnapshotHeader);
            }
            Event[] EventArr = events;
            if (EventArr != null && EventArr.length > 0) {
                int i = 0;
                while (true) {
                    Event[] EventArr2 = events;
                    if (i >= EventArr2.length) {
                        break;
                    }
                    Event snapshotProtos$Event = EventArr2[i];
                    if (snapshotProtos$Event != null) {
                        codedOutputByteBufferNano.writeMessage(2, snapshotProtos$Event);
                    }
                    i++;
                }
            }
            if (Float.floatToIntBits(sensitivitySetting) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(3, sensitivitySetting);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            SnapshotHeader snapshotProtos$SnapshotHeader = header;
            if (snapshotProtos$SnapshotHeader != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, snapshotProtos$SnapshotHeader);
            }
            Event[] EventArr = events;
            if (EventArr != null && EventArr.length > 0) {
                int i = 0;
                while (true) {
                    Event[] EventArr2 = events;
                    if (i >= EventArr2.length) {
                        break;
                    }
                    Event snapshotProtos$Event = EventArr2[i];
                    if (snapshotProtos$Event != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, snapshotProtos$Event);
                    }
                    i++;
                }
            }
            return Float.floatToIntBits(sensitivitySetting) != Float.floatToIntBits(0.0f) ? computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(3, sensitivitySetting) : computeSerializedSize;
        }

        public Snapshot mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 10) {
                    if (header == null) {
                        header = new SnapshotHeader();
                    }
                    codedInputByteBufferNano.readMessage(header);
                } else if (readTag == 18) {
                    int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 18);
                    Event[] EventArr = events;
                    int length = EventArr == null ? 0 : EventArr.length;
                    Event[] EventArr2 = new Event[(repeatedFieldArrayLength + length)];
                    if (length != 0) {
                        System.arraycopy(events, 0, EventArr2, 0, length);
                    }
                    while (length < EventArr2.length - 1) {
                        EventArr2[length] = new Event();
                        codedInputByteBufferNano.readMessage(EventArr2[length]);
                        codedInputByteBufferNano.readTag();
                        length++;
                    }
                    EventArr2[length] = new Event();
                    codedInputByteBufferNano.readMessage(EventArr2[length]);
                    events = EventArr2;
                } else if (readTag == 29) {
                    sensitivitySetting = codedInputByteBufferNano.readFloat();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    final class SnapshotHeader extends MessageNano {
        public int feedback;
        public int gestureType;
        public long identifier;

        public SnapshotHeader() {
            clear();
        }

        public SnapshotHeader clear() {
            identifier = 0;
            gestureType = 0;
            feedback = 0;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            long j = identifier;
            if (j != 0) {
                codedOutputByteBufferNano.writeInt64(1, j);
            }
            int i = gestureType;
            if (i != 0) {
                codedOutputByteBufferNano.writeInt32(2, i);
            }
            int i2 = feedback;
            if (i2 != 0) {
                codedOutputByteBufferNano.writeInt32(3, i2);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            long j = identifier;
            if (j != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            int i = gestureType;
            if (i != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, i);
            }
            int i2 = feedback;
            return i2 != 0 ? computeSerializedSize + CodedOutputByteBufferNano.computeInt32Size(3, i2) : computeSerializedSize;
        }

        public SnapshotHeader mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 8) {
                    identifier = codedInputByteBufferNano.readInt64();
                } else if (readTag == 16) {
                    int readInt32 = codedInputByteBufferNano.readInt32();
                    if (readInt32 == 0 || readInt32 == 1 || readInt32 == 2 || readInt32 == 3 || readInt32 == 4) {
                        gestureType = readInt32;
                    }
                } else if (readTag == 24) {
                    int readInt322 = codedInputByteBufferNano.readInt32();
                    if (readInt322 == 0 || readInt322 == 1 || readInt322 == 2) {
                        feedback = readInt322;
                    }
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    final class Event extends MessageNano {
        private static volatile Event[] _emptyArray;
        private int typesCase_ = 0;
        private Object types_;

        public Event() {
            clear();
        }

        public static Event[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Event[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Event clearTypes() {
            typesCase_ = 0;
            types_ = null;
            return this;
        }

        public boolean hasSensorEvent() {
            return typesCase_ == 1;
        }

        public ChassisProtos.SensorEvent getSensorEvent() {
            if (typesCase_ == 1) {
                return (ChassisProtos.SensorEvent) types_;
            }
            return null;
        }

        public boolean hasGestureStage() {
            return typesCase_ == 2;
        }

        public int getGestureStage() {
            if (typesCase_ == 2) {
                return ((Integer) types_).intValue();
            }
            return 0;
        }

        public Event clear() {
            clearTypes();
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (typesCase_ == 1) {
                codedOutputByteBufferNano.writeMessage(1, (MessageNano) types_);
            }
            if (typesCase_ == 2) {
                codedOutputByteBufferNano.writeEnum(2, ((Integer) types_).intValue());
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if (typesCase_ == 1) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, (MessageNano) types_);
            }
            return typesCase_ == 2 ? computeSerializedSize + CodedOutputByteBufferNano.computeEnumSize(2, ((Integer) types_).intValue()) : computeSerializedSize;
        }

        public Event mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 10) {
                    if (typesCase_ != 1) {
                        types_ = new ChassisProtos.SensorEvent();
                    }
                    codedInputByteBufferNano.readMessage((MessageNano) types_);
                    typesCase_ = 1;
                } else if (readTag == 16) {
                    types_ = Integer.valueOf(codedInputByteBufferNano.readEnum());
                    typesCase_ = 2;
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }

    final class Snapshots extends MessageNano {
        public Snapshot[] snapshots;

        public Snapshots() {
            clear();
        }

        public Snapshots clear() {
            snapshots = Snapshot.emptyArray();
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Snapshot[] SnapshotArr = snapshots;
            if (SnapshotArr != null && SnapshotArr.length > 0) {
                int i = 0;
                while (true) {
                    Snapshot[] SnapshotArr2 = snapshots;
                    if (i >= SnapshotArr2.length) {
                        break;
                    }
                    Snapshot snapshotProtos$Snapshot = SnapshotArr2[i];
                    if (snapshotProtos$Snapshot != null) {
                        codedOutputByteBufferNano.writeMessage(1, snapshotProtos$Snapshot);
                    }
                    i++;
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            Snapshot[] SnapshotArr = snapshots;
            if (SnapshotArr != null && SnapshotArr.length > 0) {
                int i = 0;
                while (true) {
                    Snapshot[] SnapshotArr2 = snapshots;
                    if (i >= SnapshotArr2.length) {
                        break;
                    }
                    Snapshot snapshotProtos$Snapshot = SnapshotArr2[i];
                    if (snapshotProtos$Snapshot != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, snapshotProtos$Snapshot);
                    }
                    i++;
                }
            }
            return computeSerializedSize;
        }

        public Snapshots mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                if (readTag == 0) {
                    return this;
                }
                if (readTag == 10) {
                    int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 10);
                    Snapshot[] SnapshotArr = snapshots;
                    int length = SnapshotArr == null ? 0 : SnapshotArr.length;
                    Snapshot[] SnapshotArr2 = new Snapshot[(repeatedFieldArrayLength + length)];
                    if (length != 0) {
                        System.arraycopy(snapshots, 0, SnapshotArr2, 0, length);
                    }
                    while (length < SnapshotArr2.length - 1) {
                        SnapshotArr2[length] = new Snapshot();
                        codedInputByteBufferNano.readMessage(SnapshotArr2[length]);
                        codedInputByteBufferNano.readTag();
                        length++;
                    }
                    SnapshotArr2[length] = new Snapshot();
                    codedInputByteBufferNano.readMessage(SnapshotArr2[length]);
                    snapshots = SnapshotArr2;
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                    return this;
                }
            }
        }
    }


}

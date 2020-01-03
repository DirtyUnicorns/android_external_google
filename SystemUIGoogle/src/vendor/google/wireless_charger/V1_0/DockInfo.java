package vendor.google.wireless_charger.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.Objects;

public final class DockInfo {
    public boolean isGetInfoSupported;
    public String manufacturer = new String();
    public int maxFwSize;
    public String model = new String();
    public byte orientation;
    public String serial = new String();
    public byte type;
    public final FirmwareVersion version = new FirmwareVersion();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DockInfo.class) {
            return false;
        }
        DockInfo other = (DockInfo) otherObject;
        if (HidlSupport.deepEquals(this.manufacturer, other.manufacturer) &&
                HidlSupport.deepEquals(this.model, other.model) &&
                HidlSupport.deepEquals(this.serial, other.serial) &&
                this.maxFwSize == other.maxFwSize &&
                this.isGetInfoSupported == other.isGetInfoSupported &&
                HidlSupport.deepEquals(this.version, other.version) &&
                this.orientation == other.orientation && this.type == other.type) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(
                this.manufacturer)), Integer.valueOf(HidlSupport.deepHashCode(
                this.model)), Integer.valueOf(HidlSupport.deepHashCode(
                this.serial)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(
                this.maxFwSize))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(
                this.isGetInfoSupported))), Integer.valueOf(HidlSupport.deepHashCode(
                this.version)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(
                this.orientation))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(
                this.type)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".manufacturer = ");
        builder.append(this.manufacturer);
        builder.append(", .model = ");
        builder.append(this.model);
        builder.append(", .serial = ");
        builder.append(this.serial);
        builder.append(", .maxFwSize = ");
        builder.append(this.maxFwSize);
        builder.append(", .isGetInfoSupported = ");
        builder.append(this.isGetInfoSupported);
        builder.append(", .version = ");
        builder.append(this.version);
        builder.append(", .orientation = ");
        builder.append(Orientation.toString(this.orientation));
        builder.append(", .type = ");
        builder.append(DockType.toString(this.type));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(88), 0);
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob,
                                             long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.manufacturer = hwBlob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.manufacturer.getBytes().length + 1),
                _hidl_blob.handle(), (_hidl_offset + 0) + 0, false);
        this.model = hwBlob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.model.getBytes().length + 1),
                _hidl_blob.handle(), (_hidl_offset + 16) + 0, false);
        this.serial = hwBlob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.serial.getBytes().length + 1),
                _hidl_blob.handle(), (_hidl_offset + 32) + 0, false);
        this.maxFwSize = hwBlob.getInt32(_hidl_offset + 48);
        this.isGetInfoSupported = hwBlob.getBool(_hidl_offset + 52);
        this.version.readEmbeddedFromParcel(parcel, hwBlob, _hidl_offset + 56);
        this.orientation = hwBlob.getInt8(_hidl_offset + 80);
        this.type = hwBlob.getInt8(_hidl_offset + 81);
    }
}

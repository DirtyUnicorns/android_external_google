package vendor.google.wireless_charger.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.Objects;

public final class FirmwareVersion {
    public String extra = new String();
    public int major;
    public int minor;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != FirmwareVersion.class) {
            return false;
        }
        FirmwareVersion other = (FirmwareVersion) otherObject;
        if (this.major == other.major && this.minor == other.minor && HidlSupport.deepEquals(
                this.extra, other.extra)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(
                this.major))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(
                this.minor))), Integer.valueOf(HidlSupport.deepHashCode(this.extra))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".major = ");
        builder.append(this.major);
        builder.append(", .minor = ");
        builder.append(this.minor);
        builder.append(", .extra = ");
        builder.append(this.extra);
        builder.append("}");
        return builder.toString();
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.major = hwBlob.getInt32(_hidl_offset + 0);
        this.minor = hwBlob.getInt32(_hidl_offset + 4);
        this.extra = hwBlob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.extra.getBytes().length + 1), _hidl_blob.handle(),
                (_hidl_offset + 8) + 0, false);
    }
}

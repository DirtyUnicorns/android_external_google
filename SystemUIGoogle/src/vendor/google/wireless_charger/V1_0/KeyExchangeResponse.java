package vendor.google.wireless_charger.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class KeyExchangeResponse {
    public byte dockId;
    public final ArrayList<Byte> dockPublicKey = new ArrayList();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != KeyExchangeResponse.class) {
            return false;
        }
        KeyExchangeResponse other = (KeyExchangeResponse) otherObject;
        if (this.dockId == other.dockId && HidlSupport.deepEquals(this.dockPublicKey, other.dockPublicKey)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(
                this.dockId))), Integer.valueOf(HidlSupport.deepHashCode(this.dockPublicKey))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".dockId = ");
        builder.append(this.dockId);
        builder.append(", .dockPublicKey = ");
        builder.append(this.dockPublicKey);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob,
                                             long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.dockId = hwBlob.getInt8(_hidl_offset + 0);
        int _hidl_vec_size = hwBlob.getInt32((_hidl_offset + 8) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1),
                _hidl_blob.handle(), (_hidl_offset + 8) + 0, true);
        this.dockPublicKey.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.dockPublicKey.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }
}

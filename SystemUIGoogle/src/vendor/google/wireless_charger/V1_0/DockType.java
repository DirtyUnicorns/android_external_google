package vendor.google.wireless_charger.V1_0;

public final class DockType {
    public static final String toString(byte o) {
        if (o == (byte) 0) {
            return "DESKTOP_DOCK";
        }
        if (o == (byte) 1) {
            return "DESKTOP_PAD";
        }
        if (o == (byte) 2) {
            return "AUTOMOBILE_DOCK";
        }
        if (o == (byte) 3) {
            return "AUTOMOBILE_PAD";
        }
        if (o == (byte) 15) {
            return "UNKNOWN";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        stringBuilder.append(Integer.toHexString(Byte.toUnsignedInt(o)));
        return stringBuilder.toString();
    }
}
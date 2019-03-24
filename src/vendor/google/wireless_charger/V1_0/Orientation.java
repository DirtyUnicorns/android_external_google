package vendor.google.wireless_charger.V1_0;

public final class Orientation {
    public static final String toString(byte o) {
        if (o == (byte) 0) {
            return "ARBITRARY";
        }
        if (o == (byte) 1) {
            return "LANDSCAPE";
        }
        if (o == (byte) 2) {
            return "PORTRAIT";
        }
        if (o == (byte) 3) {
            return "BOTH";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        stringBuilder.append(Integer.toHexString(Byte.toUnsignedInt(o)));
        return stringBuilder.toString();
    }
}
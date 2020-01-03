package com.google.android.systemui.dreamliner;

import android.os.Bundle;

public class DockInfo {
    private int accessoryType = -1;
    private String manufacturer = "";
    private String model = "";
    private String serialNumber = "";

    public DockInfo(String manufacturer, String model, String serialNumber, int accessoryType) {
        manufacturer = manufacturer;
        model = model;
        serialNumber = serialNumber;
        accessoryType = accessoryType;
    }

    /* Access modifiers changed, original: 0000 */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("manufacturer", manufacturer);
        bundle.putString("model", model);
        bundle.putString("serialNumber", serialNumber);
        bundle.putInt("accessoryType", accessoryType);
        return bundle;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(manufacturer);
        sb.append(", ");
        sb.append(model);
        sb.append(", ");
        sb.append(serialNumber);
        sb.append(", ");
        sb.append(accessoryType);
        return sb.toString();
    }
}
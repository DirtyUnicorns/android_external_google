package com.google.android.systemui.dreamliner;

import android.os.Bundle;

public class DockInfo {
    private int accessoryType = -1;
    private String manufacturer = "";
    private String model = "";
    private String serialNumber = "";

    public DockInfo(String manufacturer, String model, String serialNumber, int accessoryType) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.accessoryType = accessoryType;
    }

    /* Access modifiers changed, original: 0000 */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("manufacturer", this.manufacturer);
        bundle.putString("model", this.model);
        bundle.putString("serialNumber", this.serialNumber);
        bundle.putInt("accessoryType", this.accessoryType);
        return bundle;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.manufacturer);
        stringBuilder.append(", ");
        stringBuilder.append(this.model);
        stringBuilder.append(", ");
        stringBuilder.append(this.serialNumber);
        stringBuilder.append(", ");
        stringBuilder.append(this.accessoryType);
        return stringBuilder.toString();
    }
}
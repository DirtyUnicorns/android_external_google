package com.google.android.settings.connecteddevice.dock;

public class DockDevice {
    private String mId;
    private String mName;

    private DockDevice() {
    }

    DockDevice(String id, String name) {
        this.mId = id;
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public String getId() {
        return this.mId;
    }
}
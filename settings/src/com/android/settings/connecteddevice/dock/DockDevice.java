package com.google.android.settings.connecteddevice.dock;

public class DockDevice {
    private String mId;
    private String mName;

    private DockDevice() {
    }

    DockDevice(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }
}
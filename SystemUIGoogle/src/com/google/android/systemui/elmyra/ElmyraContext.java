package com.google.android.systemui.elmyra;

import android.content.Context;

public class ElmyraContext {
    private Context mContext;

    public ElmyraContext(Context context) {
        mContext = context;
    }

    public boolean isAvailable() {
        return mContext.getPackageManager().hasSystemFeature("android.hardware.sensor.assist");
    }
}

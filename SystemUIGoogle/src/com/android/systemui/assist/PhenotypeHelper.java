package com.android.systemui.assist;

import android.provider.DeviceConfig;
import java.util.concurrent.Executor;

public class PhenotypeHelper {
    public long getLong(String str, long j) {
        return DeviceConfig.getLong("systemui", str, j);
    }

    public int getInt(String str, int i) {
        return DeviceConfig.getInt("systemui", str, i);
    }

    public String getString(String str, String str2) {
        return DeviceConfig.getString("systemui", str, str2);
    }

    public boolean getBoolean(String str, boolean z) {
        return DeviceConfig.getBoolean("systemui", str, z);
    }

    public void addOnPropertiesChangedListener(Executor executor, DeviceConfig.OnPropertiesChangedListener onPropertiesChangedListener) {
        DeviceConfig.addOnPropertiesChangedListener("systemui", executor, onPropertiesChangedListener);
    }
}

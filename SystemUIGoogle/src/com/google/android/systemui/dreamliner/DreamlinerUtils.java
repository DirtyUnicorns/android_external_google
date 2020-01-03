package com.google.android.systemui.dreamliner;

import android.content.Context;
import android.text.TextUtils;
import com.android.systemui.R;

public final class DreamlinerUtils {
    public static WirelessCharger getInstance(Context context) {
        if (context == null) {
            return null;
        }
        // Get dock class name.
        String clsName = context.getString(R.string.config_dockComponent);
        // Try to load instance.
        try {
            return (WirelessCharger) context.getClassLoader().loadClass(clsName).newInstance();
        } catch (Throwable th) {
            return null;
        }
    }
}
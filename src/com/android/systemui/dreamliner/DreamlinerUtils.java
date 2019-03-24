package com.google.android.systemui.dreamliner;

import android.content.Context;
import android.text.TextUtils;
import com.android.systemui.R;

public final class DreamlinerUtils {
    public static WirelessCharger getInstance(Context context) {
        if (context == null) {
            return null;
        }
        // TODO: this is a possible way of overlaying the dock component
        // for Pixel 3/XL.
        String clsName = context.getString(R.string.config_dockComponent);
        // if (TextUtils.isEmpty(clsName)) {
        //     return null;
        // }
        try {
            return (WirelessCharger) context.getClassLoader().loadClass(clsName).newInstance();
        } catch (Throwable th) {
            return null;
        }
    }
}
package com.google.android.systemui.assist;

import android.content.Context;
import android.os.UserManager;
import android.view.View;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.ArrayList;

// FIXME: Bloody decompiler and its z-booleans.
public class OpaEnabledDispatcher implements OpaEnabledListener {

    public void onOpaEnabledReceived(Context context, boolean z, boolean z2, boolean z3) {
        dispatchUnchecked(context, (z && z2) || UserManager.isDeviceInDemoMode(context));
    }

    private void dispatchUnchecked(Context context, boolean z) {
        StatusBar statusBar = (StatusBar) ((SystemUIApplication) context.getApplicationContext()).getComponent(StatusBar.class);
        if (statusBar != null && statusBar.getNavigationBarView() != null) {
            ArrayList<View> views = statusBar.getNavigationBarView().getHomeButton().getViews();
            for (View v: views) {
                if (v instanceof OpaLayout) {
                    ((OpaLayout) v).setOpaEnabled(z);
                }
            }
        }
    }
}

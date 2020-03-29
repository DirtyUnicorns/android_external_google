package com.google.android.systemui.assist;

import android.content.Context;
import android.os.UserManager;
import android.view.View;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.ArrayList;

public class OpaEnabledDispatcher implements OpaEnabledListener {
    public void onOpaEnabledReceived(Context context, boolean z, boolean z2, boolean z3) {
        dispatchUnchecked(context, (z && z2) || UserManager.isDeviceInDemoMode(context));
    }

    private void dispatchUnchecked(Context context, boolean z) {
        StatusBar statusBar = (StatusBar) SysUiServiceProvider.getComponent(context, StatusBar.class);
        if (statusBar != null && statusBar.getNavigationBarView() != null) {
            ArrayList<View> views = statusBar.getNavigationBarView().getHomeButton().getViews();
            for (int i = 0; i < views.size(); i++) {
                ((OpaLayout) views.get(i)).setOpaEnabled(z);
            }
        }
    }
}

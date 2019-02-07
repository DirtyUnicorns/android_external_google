package com.google.android.systemui;

import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.VendorServices;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBar;
// import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;
// import com.google.android.systemui.ambientmusic.AmbientIndicationService;
// import com.google.android.systemui.dreamliner.DockObserver;
import com.google.android.systemui.elmyra.ElmyraContext;
import com.google.android.systemui.elmyra.ElmyraService;
import com.google.android.systemui.elmyra.ServiceConfigurationGoogle;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GoogleServices extends VendorServices {
    private ArrayList<Object> mServices = new ArrayList();

    private void addService(Object obj) {
        if (obj != null) {
            this.mServices.add(obj);
        }
    }

    public void start() {
        StatusBar statusBar = (StatusBar) SysUiServiceProvider.getComponent(this.mContext, StatusBar.class);
        // AmbientIndicationContainer ambientIndicationContainer = (AmbientIndicationContainer) statusBar.getStatusBarWindow().findViewById(R.id.ambient_indication_container);
        // ambientIndicationContainer.initializeView(statusBar);
        // addService(new AmbientIndicationService(this.mContext, ambientIndicationContainer));
        // addService(new DisplayCutoutEmulationAdapter(this.mContext));
        // addService(new DockObserver(this.mContext));
        if (new ElmyraContext(this.mContext).isAvailable()) {
            addService(new ElmyraService(this.mContext, new ServiceConfigurationGoogle(this.mContext)));
        }
    }
}

package com.google.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.VendorServices;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBar;
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

    // Small broadcast receiver for helping with Application actions
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                // Get packageName from Uri
                String packageName = intent.getData().getSchemeSpecificPart();
                // Get package names currently set as default
                String shortPackageName = Settings.Secure.getStringForUser(context.getContentResolver(),
                        Settings.Secure.SHORT_SQUEEZE_CUSTOM_APP,
                        UserHandle.USER_CURRENT);
                String longPackageName = Settings.Secure.getStringForUser(context.getContentResolver(),
                        Settings.Secure.LONG_SQUEEZE_CUSTOM_APP,
                        UserHandle.USER_CURRENT);
                // if the package name equals to some set value
                if(packageName.equals(shortPackageName)) {
                    // The short application action has to be reset
                    resetApplicationAction(/* isShortAction */ true);
                }
                if (packageName.equals(longPackageName)) {
                    // The long application action has to be reset
                    resetApplicationAction(/* isShortAction */ false);
                }
            }
        }
    };

    public void start() {
        StatusBar statusBar = (StatusBar) SysUiServiceProvider.getComponent(mContext, StatusBar.class);
        if (new ElmyraContext(mContext).isAvailable()) {
            addService(new ElmyraService(mContext, new ServiceConfigurationGoogle(mContext)));
        }
        // Intent for applications that get uninstalled
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        // Register our BroadcastReceiver
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    /* Helper that sets to default values, this gets called when a package
       used as action gets removed.*/
    private void resetApplicationAction(boolean isShortAction) {
        if (isShortAction) {
            // Remove stored values
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                    Settings.Secure.SHORT_SQUEEZE_SELECTION, /* no action */ 0,
                    UserHandle.USER_CURRENT);
            Settings.Secure.putStringForUser(mContext.getContentResolver(),
                    Settings.Secure.SHORT_SQUEEZE_CUSTOM_APP_FR_NAME, /* none */ "",
                    UserHandle.USER_CURRENT);
        } else {
            // Remove stored values
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                    Settings.Secure.LONG_SQUEEZE_SELECTION, /* no action */ 0,
                    UserHandle.USER_CURRENT);
            Settings.Secure.putStringForUser(mContext.getContentResolver(),
                    Settings.Secure.LONG_SQUEEZE_CUSTOM_APP_FR_NAME, /* none */ "",
                    UserHandle.USER_CURRENT);
        }
    }
}

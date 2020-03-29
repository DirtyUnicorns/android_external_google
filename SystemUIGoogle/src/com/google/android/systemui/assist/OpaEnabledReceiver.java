package com.google.android.systemui.assist;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.widget.ILockSettings;
import java.util.ArrayList;
import java.util.List;

public class OpaEnabledReceiver {
    private final BroadcastReceiver mBroadcastReceiver = new OpaEnabledBroadcastReceiver();
    private final ContentObserver mContentObserver;
    private final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mIsAGSAAssistant;
    private boolean mIsOpaEligible;
    private boolean mIsOpaEnabled;
    private final List<OpaEnabledListener> mListeners = new ArrayList();
    /* access modifiers changed from: private */
    public final ILockSettings mLockSettings;

    public OpaEnabledReceiver(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mContentObserver = new AssistantContentObserver(this.mContext);
        this.mLockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
        updateOpaEnabledState(this.mContext);
        registerContentObserver();
        registerEnabledReceiver(-2);
    }

    public void addOpaEnabledListener(OpaEnabledListener opaEnabledListener) {
        this.mListeners.add(opaEnabledListener);
        opaEnabledListener.onOpaEnabledReceived(this.mContext, this.mIsOpaEligible, this.mIsAGSAAssistant, this.mIsOpaEnabled);
    }

    public void onUserSwitching(int i) {
        updateOpaEnabledState(this.mContext);
        dispatchOpaEnabledState(this.mContext);
        this.mContentResolver.unregisterContentObserver(this.mContentObserver);
        registerContentObserver();
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        registerEnabledReceiver(i);
    }

    private boolean isOpaEligible(Context context) {
        if (Settings.Secure.getIntForUser(context.getContentResolver(), "systemui.google.opa_enabled", 0, -2) != 0) {
            return true;
        }
        return false;
    }

    private boolean isOpaEnabled(Context context) {
        try {
            return this.mLockSettings.getBoolean("systemui.google.opa_user_enabled", false, -2);
        } catch (RemoteException e) {
            Log.e("OpaEnabledReceiver", "isOpaEnabled RemoteException", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void updateOpaEnabledState(Context context) {
        this.mIsOpaEligible = isOpaEligible(context);
        this.mIsAGSAAssistant = OpaUtils.isAGSACurrentAssistant(context);
        this.mIsOpaEnabled = isOpaEnabled(context);
    }

    public void dispatchOpaEnabledState() {
        dispatchOpaEnabledState(this.mContext);
    }

    /* access modifiers changed from: private */
    public void dispatchOpaEnabledState(Context context) {
        Log.i("OpaEnabledReceiver", "Dispatching OPA eligble = " + this.mIsOpaEligible + "; AGSA = " + this.mIsAGSAAssistant + "; OPA enabled = " + this.mIsOpaEnabled);
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onOpaEnabledReceived(context, this.mIsOpaEligible, this.mIsAGSAAssistant, this.mIsOpaEnabled);
        }
    }

    private void registerContentObserver() {
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("assistant"), false, this.mContentObserver, -2);
    }

    private void registerEnabledReceiver(int i) {
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, new UserHandle(i), new IntentFilter("com.google.android.systemui.OPA_ENABLED"), null, null);
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, new UserHandle(i), new IntentFilter("com.google.android.systemui.OPA_USER_ENABLED"), null, null);
    }

    private class AssistantContentObserver extends ContentObserver {
        public AssistantContentObserver(Context context) {
            super(new Handler(context.getMainLooper()));
        }

        public void onChange(boolean z, Uri uri) {
            OpaEnabledReceiver opaEnabledReceiver = OpaEnabledReceiver.this;
            opaEnabledReceiver.updateOpaEnabledState(opaEnabledReceiver.mContext);
            OpaEnabledReceiver opaEnabledReceiver2 = OpaEnabledReceiver.this;
            opaEnabledReceiver2.dispatchOpaEnabledState(opaEnabledReceiver2.mContext);
        }
    }

    private class OpaEnabledBroadcastReceiver extends BroadcastReceiver {
        private OpaEnabledBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.google.android.systemui.OPA_ENABLED")) {
                Settings.Secure.putIntForUser(context.getContentResolver(), "systemui.google.opa_enabled", intent.getBooleanExtra("OPA_ENABLED", false) ? 1 : 0, -2);
            } else if (intent.getAction().equals("com.google.android.systemui.OPA_USER_ENABLED")) {
                try {
                    OpaEnabledReceiver.this.mLockSettings.setBoolean("systemui.google.opa_user_enabled", intent.getBooleanExtra("OPA_USER_ENABLED", false), -2);
                } catch (RemoteException e) {
                    Log.e("OpaEnabledReceiver", "RemoteException on OPA_USER_ENABLED", e);
                }
            }
            OpaEnabledReceiver.this.updateOpaEnabledState(context);
            OpaEnabledReceiver.this.dispatchOpaEnabledState(context);
        }
    }
}

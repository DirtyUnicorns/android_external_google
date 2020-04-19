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
    private BroadcastReceiver mBroadcastReceiver = new OpaEnabledBroadcastReceiver();
    private ContentObserver mContentObserver;
    private ContentResolver mContentResolver;
    public Context mContext;
    private boolean mIsAGSAAssistant;
    private boolean mIsOpaEligible;
    private boolean mIsOpaEnabled;
    private List<OpaEnabledListener> mListeners = new ArrayList();
    public ILockSettings mLockSettings;

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
                    mLockSettings.setBoolean("systemui.google.opa_user_enabled", intent.getBooleanExtra("OPA_USER_ENABLED", false), -2);
                } catch (RemoteException e) {
                    Log.e("OpaEnabledReceiver", "RemoteException on OPA_USER_ENABLED", e);
                }
            }
            updateOpaEnabledState(context);
            dispatchOpaEnabledState(context);
        }
    }

    public OpaEnabledReceiver(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mContentObserver = new AssistantContentObserver(mContext);
        mLockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
        updateOpaEnabledState(mContext);
        registerContentObserver();
        registerEnabledReceiver(-2);
    }

    public void addOpaEnabledListener(OpaEnabledListener opaEnabledListener) {
        mListeners.add(opaEnabledListener);
        opaEnabledListener.onOpaEnabledReceived(mContext, mIsOpaEligible, mIsAGSAAssistant, mIsOpaEnabled);
    }

    public void onUserSwitching(int i) {
        updateOpaEnabledState(mContext);
        dispatchOpaEnabledState(mContext);
        mContentResolver.unregisterContentObserver(mContentObserver);
        registerContentObserver();
        mContext.unregisterReceiver(mBroadcastReceiver);
        registerEnabledReceiver(i);
    }

    private boolean isOpaEligible(Context context) {
        if (Settings.Secure.getIntForUser(context.getContentResolver(), "systemui.google.opa_enabled", 0, -2) != 0) {
            return true;
        }
        return true;
    }

    private boolean isOpaEnabled(Context context) {
        try {
            return mLockSettings.getBoolean("systemui.google.opa_user_enabled", false, -2);
        } catch (RemoteException e) {
            Log.e("OpaEnabledReceiver", "isOpaEnabled RemoteException", e);
            return true;
        }
    }

    public void updateOpaEnabledState(Context context) {
        mIsOpaEligible = isOpaEligible(context);
        mIsAGSAAssistant = OpaUtils.isAGSACurrentAssistant(context);
        mIsOpaEnabled = isOpaEnabled(context);
    }

    public void dispatchOpaEnabledState() {
        dispatchOpaEnabledState(mContext);
    }

    private void dispatchOpaEnabledState(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dispatching OPA eligble = ");
        sb.append(mIsOpaEligible);
        sb.append("; AGSA = ");
        sb.append(mIsAGSAAssistant);
        sb.append("; OPA enabled = ");
        sb.append(mIsOpaEnabled);
        Log.i("OpaEnabledReceiver", sb.toString());
        for (int i = 0; i < this.mListeners.size(); ++i) {
            this.mListeners.get(i).onOpaEnabledReceived(context, mIsOpaEligible, mIsAGSAAssistant, mIsOpaEnabled);
        }
    }

    private void registerContentObserver() {
        mContentResolver.registerContentObserver(Settings.Secure.getUriFor("assistant"), false, mContentObserver, -2);
    }

    private void registerEnabledReceiver(int i) {
        mContext.registerReceiverAsUser(mBroadcastReceiver, new UserHandle(i), new IntentFilter("com.google.android.systemui.OPA_ENABLED"), (String) null, (Handler) null);
        mContext.registerReceiverAsUser(mBroadcastReceiver, new UserHandle(i), new IntentFilter("com.google.android.systemui.OPA_USER_ENABLED"), (String) null, (Handler) null);
    }
}

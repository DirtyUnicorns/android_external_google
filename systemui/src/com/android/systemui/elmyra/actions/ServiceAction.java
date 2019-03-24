package com.google.android.systemui.elmyra.actions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.util.Log;
import com.google.android.systemui.elmyra.ElmyraServiceProxy;
import com.google.android.systemui.elmyra.IElmyraService;
import com.google.android.systemui.elmyra.IElmyraService.Stub;
import com.google.android.systemui.elmyra.IElmyraServiceGestureListener;
import com.google.android.systemui.elmyra.IElmyraServiceListener;
import com.google.android.systemui.elmyra.feedback.FeedbackEffect;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class ServiceAction extends Action implements DeathRecipient {
    private IElmyraService mElmyraService;
    private final ElmyraServiceConnection mElmyraServiceConnection = new ElmyraServiceConnection();
    private IElmyraServiceGestureListener mElmyraServiceGestureListener;
    private final ElmyraServiceListener mElmyraServiceListener = new ElmyraServiceListener();
    private final IBinder mToken = new Binder();

    private class ElmyraServiceConnection implements ServiceConnection {
        private ElmyraServiceConnection() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mElmyraService = Stub.asInterface(iBinder);
            try {
                mElmyraService.registerServiceListener(mToken, mElmyraServiceListener);
            } catch (Throwable suppressed) { /* do nothing */ }
            ServiceAction.this.onServiceConnected();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mElmyraService = null;
            ServiceAction.this.onServiceDisconnected();
        }
    }

    private class ElmyraServiceListener extends IElmyraServiceListener.Stub {
        private ElmyraServiceListener() {
        }

        public void setListener(IBinder iBinder, IBinder iBinder2) {
            if (!ServiceAction.this.checkSupportedCaller()) {
                return;
            }
            if (iBinder2 != null || mElmyraServiceGestureListener != null) {
                IElmyraServiceGestureListener asInterface = IElmyraServiceGestureListener.Stub.asInterface(iBinder2);
                if (asInterface != mElmyraServiceGestureListener) {
                    mElmyraServiceGestureListener = asInterface;
                    notifyListener();
                }
                if (iBinder == null) {
                    return;
                }
                if (iBinder2 != null) {
                    try {
                        iBinder.linkToDeath(ServiceAction.this, 0);
                        return;
                    } catch (Throwable e) {
                        return;
                    }
                }
                iBinder.unlinkToDeath(ServiceAction.this, 0);
            }
        }

        public void triggerAction() {
            if (ServiceAction.this.checkSupportedCaller()) {
                ServiceAction.this.triggerAction();
            }
        }
    }

    public ServiceAction(Context context, List<FeedbackEffect> list) {
        super(context, list);
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(getContext(), ElmyraServiceProxy.class));
            getContext().bindService(intent, mElmyraServiceConnection, 1);
        } catch (Throwable suppressed) { /* do nothing */ }
    }

    public void binderDied() {
        mElmyraServiceGestureListener = null;
        notifyListener();
    }

    protected abstract boolean checkSupportedCaller();

    protected boolean checkSupportedCaller(String str) {
        String[] packagesForUid = getContext().getPackageManager().getPackagesForUid(Binder.getCallingUid());
        return packagesForUid == null ? false : Arrays.asList(packagesForUid).contains(str);
    }

    @Override
	public boolean isAvailable() {
        return mElmyraServiceGestureListener != null;
    }

    @Override
	public void onProgress(float f, int i) {
        if (mElmyraServiceGestureListener != null) {
            updateFeedbackEffects(f, i);
            try {
                mElmyraServiceGestureListener.onGestureProgress(f, i);
            } catch (Throwable e) {
                mElmyraServiceGestureListener = null;
                notifyListener();
            }
        }
    }

    protected void onServiceConnected() {
    }

    protected void onServiceDisconnected() {
    }

    public void onTrigger(DetectionProperties detectionProperties) {
        if (mElmyraServiceGestureListener != null) {
            triggerFeedbackEffects(detectionProperties);
            try {
                mElmyraServiceGestureListener.onGestureDetected();
            } catch (Throwable e) {
                mElmyraServiceGestureListener = null;
                notifyListener();
            }
        }
    }

    protected void triggerAction() {
    }
}

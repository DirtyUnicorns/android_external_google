package com.google.android.systemui.elmyra;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import com.google.android.systemui.elmyra.IElmyraService.Stub;
import java.util.ArrayList;
import java.util.List;

public class ElmyraServiceProxy extends Service {
    private final Stub mBinder = new ElmyraServiceStub();
    private final List<ElmyraServiceListener> mElmyraServiceListeners = new ArrayList();

    /* renamed from: com.google.android.systemui.elmyra.ElmyraServiceProxy$1 */
    class ElmyraServiceStub extends Stub {
        ElmyraServiceStub() {
        }

        public void registerGestureListener(IBinder iBinder, IBinder iBinder2) {
            ElmyraServiceProxy.this.checkPermission();
            try {
                for (int size = ElmyraServiceProxy.this.mElmyraServiceListeners.size() - 1; size >= 0; size--) {
                    IElmyraServiceListener listener = ((ElmyraServiceListener) ElmyraServiceProxy.this.mElmyraServiceListeners.get(size)).getListener();
                    if (listener == null) {
                        ElmyraServiceProxy.this.mElmyraServiceListeners.remove(size);
                    } else {
                        listener.setListener(iBinder, iBinder2);
                    }
                }
            } catch (Throwable suppressed) { /* do nothing */ }
        }

        public void registerServiceListener(IBinder iBinder, IBinder iBinder2) {
            ElmyraServiceProxy.this.checkPermission();
            if (iBinder2 == null) {
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= ElmyraServiceProxy.this.mElmyraServiceListeners.size()) {
                        return;
                    }
                    if (iBinder.equals(((ElmyraServiceListener) ElmyraServiceProxy.this.mElmyraServiceListeners.get(i2)).getToken())) {
                        ((ElmyraServiceListener) ElmyraServiceProxy.this.mElmyraServiceListeners.get(i2)).unlinkToDeath();
                        ElmyraServiceProxy.this.mElmyraServiceListeners.remove(i2);
                        return;
                    }
                    i = i2 + 1;
                }
            } else {
                ElmyraServiceProxy.this.mElmyraServiceListeners.add(new ElmyraServiceListener(iBinder, IElmyraServiceListener.Stub.asInterface(iBinder2)));
            }
        }

        public void triggerAction() {
            ElmyraServiceProxy.this.checkPermission();
            try {
                for (int size = ElmyraServiceProxy.this.mElmyraServiceListeners.size() - 1; size >= 0; size--) {
                    IElmyraServiceListener listener = ((ElmyraServiceListener) ElmyraServiceProxy.this.mElmyraServiceListeners.get(size)).getListener();
                    if (listener == null) {
                        ElmyraServiceProxy.this.mElmyraServiceListeners.remove(size);
                    } else {
                        listener.triggerAction();
                    }
                }
            } catch (Throwable suppressed) { /* do nothing */ }
        }
    }

    private class ElmyraServiceListener implements DeathRecipient {
        private IElmyraServiceListener mListener;
        private IBinder mToken;

        ElmyraServiceListener(IBinder iBinder, IElmyraServiceListener iElmyraServiceListener) {
            this.mToken = iBinder;
            this.mListener = iElmyraServiceListener;
            linkToDeath();
        }

        private void linkToDeath() {
            if (this.mToken != null) {
                try {
                    this.mToken.linkToDeath(this, 0);
                } catch (Throwable suppressed) { /* do nothing */ }
            }
        }

        public void binderDied() {
            this.mToken = null;
            this.mListener = null;
        }

        public IElmyraServiceListener getListener() {
            return this.mListener;
        }

        public IBinder getToken() {
            return this.mToken;
        }

        public void unlinkToDeath() {
            if (this.mToken != null) {
                this.mToken.unlinkToDeath(this, 0);
            }
        }
    }

    private void checkPermission() {
        enforceCallingOrSelfPermission("com.google.android.elmyra.permission.CONFIGURE_ASSIST_GESTURE", 
                "Must have com.google.android.elmyra.permission.CONFIGURE_ASSIST_GESTURE permission");
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        return 0;
    }
}

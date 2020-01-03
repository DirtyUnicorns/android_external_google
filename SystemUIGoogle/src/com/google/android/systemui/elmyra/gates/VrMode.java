package com.google.android.systemui.elmyra.gates;

import android.content.Context;
import android.os.ServiceManager;
import android.service.vr.IVrManager;
import android.service.vr.IVrManager.Stub;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;

public class VrMode extends Gate {
    private boolean mInVrMode;
    private final IVrManager mVrManager = Stub.asInterface(ServiceManager.getService("vrmanager"));
    private final IVrStateCallbacks mVrStateCallbacks = new VrStateCallback();

    /* renamed from: com.google.android.systemui.elmyra.gates.VrMode$1 */
    class VrStateCallback extends IVrStateCallbacks.Stub {
        VrStateCallback() {
        }

        public void onVrStateChanged(boolean z) {
            if (z != VrMode.this.mInVrMode) {
                VrMode.this.mInVrMode = z;
                VrMode.this.notifyListener();
            }
        }
    }

    public VrMode(Context context) {
        super(context);
    }

    protected boolean isBlocked() {
        return this.mInVrMode;
    }

    protected void onActivate() {
        if (this.mVrManager != null) {
            try {
                this.mInVrMode = this.mVrManager.getVrModeState();
                this.mVrManager.registerListener(this.mVrStateCallbacks);
            } catch (Throwable e) {
                this.mInVrMode = false;
            }
        }
    }

    protected void onDeactivate() {
        if (this.mVrManager != null) {
            try {
                this.mVrManager.unregisterListener(this.mVrStateCallbacks);
            } catch (Throwable e) {
                this.mInVrMode = false;
            }
        }
    }
}

package com.google.android.systemui.elmyra.gates;

import android.content.Context;
import android.os.Handler;

public abstract class Gate {
    private boolean mActive = false;
    private final Context mContext;
    private Listener mListener;
    private final Handler mNotifyHandler;

    public interface Listener {
        void onGateChanged(Gate gate);
    }

    public Gate(Context context) {
        this.mContext = context;
        this.mNotifyHandler = new Handler(context.getMainLooper());
    }

    public void activate() {
        if (!isActive()) {
            this.mActive = true;
            onActivate();
        }
    }

    public void deactivate() {
        if (isActive()) {
            this.mActive = false;
            onDeactivate();
        }
    }

    protected Context getContext() {
        return this.mContext;
    }

    public final boolean isActive() {
        return this.mActive;
    }

    protected abstract boolean isBlocked();

    public final boolean isBlocking() {
        return isActive() && isBlocked();
    }

    protected void notifyListener() {
        if (isActive() && this.mListener != null) {
            this.mNotifyHandler.post(new LambdaGateNotify(this));
        }
    }

    protected abstract void onActivate();

    protected abstract void onDeactivate();

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public String toString() {
        return getClass().getSimpleName();
    }

    private class LambdaGateNotify implements Runnable {
        private Gate gate;

        public LambdaGateNotify(Gate gateParam) {
            gate = gateParam;
        }

        public void lambdaNotifyListener(Gate gateParam) {
            if (gateParam.mListener != null) {
                gateParam.mListener.onGateChanged(gate);
            }
        }

        public final void run() {
            lambdaNotifyListener(gate);
        }
    }
}

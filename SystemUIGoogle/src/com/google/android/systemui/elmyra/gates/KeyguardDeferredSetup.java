package com.google.android.systemui.elmyra.gates;

import android.content.Context;
import android.provider.Settings.Secure;
import com.google.android.systemui.elmyra.UserContentObserver;
import com.google.android.systemui.elmyra.actions.Action;
import com.google.android.systemui.elmyra.gates.Gate.Listener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KeyguardDeferredSetup extends Gate {
    private boolean mDeferredSetupComplete;
    private final List<Action> mExceptions;
    private final KeyguardVisibility mKeyguardGate;
    private final Listener mKeyguardGateListener = new KeyguardDeferredSetupListener();
    private final UserContentObserver mSettingsObserver;

    private class KeyguardDeferredSetupListener implements Listener {
        KeyguardDeferredSetupListener() {
        }

        public void onGateChanged(Gate gate) {
            notifyListener();
        }
    }

    public KeyguardDeferredSetup(Context context, List<Action> list) {
        super(context);
        mExceptions = new ArrayList(list);
        mKeyguardGate = new KeyguardVisibility(context);
        mKeyguardGate.setListener(mKeyguardGateListener);
        mSettingsObserver = new UserContentObserver(context, Secure.getUriFor("assist_gesture_setup_complete"), 
            new KeyguardDeferredSetupConsumer(this), false);
    }

    private boolean isDeferredSetupComplete() {
        return Secure.getIntForUser(getContext().getContentResolver(),
         "assist_gesture_setup_complete", 0, -2) != 0;
    }

    protected void updateSetupComplete() {
        boolean isDeferredSetupComplete = isDeferredSetupComplete();
        if (mDeferredSetupComplete != isDeferredSetupComplete) {
            mDeferredSetupComplete = isDeferredSetupComplete;
            notifyListener();
        }
    }

    protected boolean isBlocked() {
        for (int i = 0; i < mExceptions.size(); i++) {
            if (((Action) mExceptions.get(i)).isAvailable()) {
                return false;
            }
        }
        return !mDeferredSetupComplete && mKeyguardGate.isBlocking();
    }

    public boolean isSuwComplete() {
        return mDeferredSetupComplete;
    }

    protected void onActivate() {
        mKeyguardGate.activate();
        mDeferredSetupComplete = isDeferredSetupComplete();
        mSettingsObserver.activate();
    }

    protected void onDeactivate() {
        mKeyguardGate.deactivate();
        mSettingsObserver.deactivate();
    }

    private class KeyguardDeferredSetupConsumer implements Consumer {
        private KeyguardDeferredSetup keyguardDeferredSetup;

        public KeyguardDeferredSetupConsumer(KeyguardDeferredSetup keyDefSetup) {
            keyguardDeferredSetup = keyDefSetup;
        }

        public final void accept(Object keygDefSetup) {
            updateSetupComplete();
        }
    }
}

package com.google.android.systemui.assist;

import android.content.ComponentName;
import android.content.Context;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.app.IVoiceInteractionSessionListener;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.google.android.systemui.assist.uihints.GoogleDefaultUiController;
import com.google.android.systemui.assist.uihints.NgaUiController;
import java.util.Objects;

public class AssistManagerGoogle extends AssistManager {
    private boolean mCheckAssistantStatus = true;
    private final GoogleDefaultUiController mDefaultUiController;
    private boolean mIsGoogleAssistant = false;
    private int mNavigationMode;
    /* access modifiers changed from: private */
    public boolean mNgaPresent = false;
    /* access modifiers changed from: private */
    public final NgaUiController mNgaUiController;
    private final OpaEnabledDispatcher mOpaEnabledDispatcher = new OpaEnabledDispatcher();
    /* access modifiers changed from: private */
    public final OpaEnabledReceiver mOpaEnabledReceiver = new OpaEnabledReceiver(super.mContext);
    private boolean mSqueezeSetUp = false;
    /* access modifiers changed from: private */
    public AssistManager.UiController mUiController;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final KeyguardUpdateMonitorCallback mUserSwitchCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.google.android.systemui.assist.AssistManagerGoogle.C15411 */

        public void onUserSwitching(int i) {
            AssistManagerGoogle.this.mOpaEnabledReceiver.onUserSwitching(i);
        }
    };

    public boolean shouldShowOrb() {
        return false;
    }

    public AssistManagerGoogle(DeviceProvisionedController deviceProvisionedController, Context context) {
        // FIXME: Properly pass these parameters.
        super(deviceProvisionedController, context, null, null);
        addOpaEnabledListener(this.mOpaEnabledDispatcher);
        KeyguardUpdateMonitor.getInstance(super.mContext).registerCallback(this.mUserSwitchCallback);
        this.mNgaUiController = new NgaUiController(context);
        this.mDefaultUiController = new GoogleDefaultUiController(context);
        this.mUiController = this.mDefaultUiController;
        this.mNavigationMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(new NavigationModeController.ModeChangedListener() {
            /* class com.google.android.systemui.assist.$$Lambda$AssistManagerGoogle$k2PE_qPUIsmOHQ2_0jIJz3IebA */

            public final void onNavigationModeChanged(int i) {
                AssistManagerGoogle.this.lambda$new$0$AssistManagerGoogle(i);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$AssistManagerGoogle(int i) {
        this.mNavigationMode = i;
    }

    public boolean shouldUseHomeButtonAnimations() {
        return !QuickStepContract.isGesturalMode(this.mNavigationMode);
    }

    /* access modifiers changed from: protected */
    public void registerVoiceInteractionSessionListener() {
        super.mAssistUtils.registerVoiceInteractionSessionListener(new IVoiceInteractionSessionListener.Stub() {
            /* class com.google.android.systemui.assist.AssistManagerGoogle.C15422 */

            public void onVoiceSessionHidden() throws RemoteException {
            }

            public void onVoiceSessionShown() throws RemoteException {
            }

            public void onSetUiHints(Bundle bundle) {
                if (AssistManagerGoogle.this.mNgaUiController.extractNga(bundle) != AssistManagerGoogle.this.mNgaPresent) {
                    AssistManagerGoogle.this.checkAssistantStatus(bundle);
                }
                if (AssistManagerGoogle.this.mNgaPresent) {
                    AssistManagerGoogle.this.mUiController.processBundle(bundle);
                } else {
                    Log.e("AssistManagerGoogle", "Got a uiHints bundle, but NGA is not active");
                }
            }
        });
    }

    public void onInvocationProgress(int i, float f) {
        if (f == 0.0f || f == 1.0f) {
            this.mCheckAssistantStatus = true;
            if (i == 2) {
                checkSqueezeGestureStatus();
            }
        }
        if (this.mCheckAssistantStatus) {
            checkAssistantStatus(null);
        }
        if (i != 2 || this.mSqueezeSetUp) {
            this.mUiController.onInvocationProgress(i, f);
        }
    }

    public void onGestureCompletion(float f) {
        this.mCheckAssistantStatus = true;
        this.mUiController.onGestureCompletion(f / super.mContext.getResources().getDisplayMetrics().density);
    }

    public void logStartAssist(int i, int i2) {
        checkAssistantStatus(null);
        MetricsLogger.action(new LogMaker(1716).setType(1).setSubtype((((!this.mNgaPresent || !this.mIsGoogleAssistant) ? 0 : 1) << 8) | toLoggingSubType(i, i2)));
    }

    public void addOpaEnabledListener(OpaEnabledListener opaEnabledListener) {
        this.mOpaEnabledReceiver.addOpaEnabledListener(opaEnabledListener);
    }

    public boolean isActiveAssistantNga() {
        return this.mNgaPresent;
    }

    public void dispatchOpaEnabledState() {
        this.mOpaEnabledReceiver.dispatchOpaEnabledState();
    }

    /* access modifiers changed from: private */
    public void checkAssistantStatus(Bundle bundle) {
        ComponentName assistComponentForUser = super.mAssistUtils.getAssistComponentForUser(-2);
        boolean z = assistComponentForUser != null && "com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService".equals(assistComponentForUser.flattenToString());
        boolean extractNga = bundle == null ? this.mNgaPresent : this.mNgaUiController.extractNga(bundle);
        if (!(z == this.mIsGoogleAssistant && extractNga == this.mNgaPresent)) {
            if (!z || !extractNga) {
                if (z) {
                    // FIXME: Decompiler code smell?
                    Objects.requireNonNull(mUiHandler);
                    mUiHandler.post(() -> mUiController.hide());

                    GoogleDefaultUiController googleDefaultUiController = this.mDefaultUiController;
                    this.mUiController = googleDefaultUiController;
                    googleDefaultUiController.setGoogleAssistant(true);
                } else {
                    // FIXME: Decompiler code smell?
                    Objects.requireNonNull(mUiHandler);
                    mUiHandler.post(() -> mUiController.hide());

                    GoogleDefaultUiController googleDefaultUiController2 = this.mDefaultUiController;
                    this.mUiController = googleDefaultUiController2;
                    googleDefaultUiController2.setGoogleAssistant(false);
                }
            } else if (this.mUiController.equals(this.mDefaultUiController)) {
                // FIXME: Decompiler code smell?
                Objects.requireNonNull(mUiHandler);
                mUiHandler.post(() -> mUiController.hide());

                this.mUiController = this.mNgaUiController;
            }
            this.mNgaPresent = extractNga;
            this.mIsGoogleAssistant = z;
        }
        this.mCheckAssistantStatus = false;
    }

    private void checkSqueezeGestureStatus() {
        boolean z = false;
        if (Settings.Secure.getInt(super.mContext.getContentResolver(), "assist_gesture_setup_complete", 0) == 1) {
            z = true;
        }
        this.mSqueezeSetUp = z;
    }
}

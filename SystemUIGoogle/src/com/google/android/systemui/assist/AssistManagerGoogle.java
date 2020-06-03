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

import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionListener;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.google.android.systemui.assist.uihints.GoogleDefaultUiController;
import com.google.android.systemui.assist.uihints.NgaUiController;

import java.util.Objects;

public class AssistManagerGoogle extends AssistManager {
    private boolean mCheckAssistantStatus;
    private final GoogleDefaultUiController mDefaultUiController;
    private boolean mIsGoogleAssistant;
    private int mNavigationMode;
    private boolean mNgaPresent;
    private final NgaUiController mNgaUiController;
    private final OpaEnabledDispatcher mOpaEnabledDispatcher;
    private final OpaEnabledReceiver mOpaEnabledReceiver;
    private boolean mSqueezeSetUp;
    private UiController mUiController;
    private Handler mUiHandler;
    private final KeyguardUpdateMonitorCallback mUserSwitchCallback = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onUserSwitching(int i) {
            mOpaEnabledReceiver.onUserSwitching(i);
        }
    };

    public AssistManagerGoogle(DeviceProvisionedController deviceProvisionedController, Context context,
                               AssistUtils assistUtils, AssistHandleBehaviorController assistHandleBehaviorController,
                               ConfigurationController configurationController, OverviewProxyService overviewProxyService) {
        super(deviceProvisionedController, context, assistUtils, assistHandleBehaviorController, configurationController, overviewProxyService);
        mCheckAssistantStatus = true;
        mIsGoogleAssistant = false;
        mNgaPresent = false;
        mSqueezeSetUp = false;
        mUiHandler = new Handler(Looper.getMainLooper());
        mOpaEnabledReceiver = new OpaEnabledReceiver(mContext);
        mOpaEnabledDispatcher = new OpaEnabledDispatcher();
        addOpaEnabledListener(mOpaEnabledDispatcher);
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUserSwitchCallback);
        mNgaUiController = new NgaUiController(context);
        mDefaultUiController = new GoogleDefaultUiController(context);
        mUiController = mDefaultUiController;
        mNavigationMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(
                new NavigationModeController.ModeChangedListener() {
            public final void onNavigationModeChanged(int i) {
                mNavigationMode = i;
            }
        });
    }

    public boolean shouldUseHomeButtonAnimations() {
        return !QuickStepContract.isGesturalMode(mNavigationMode);
    }

    @Override
    protected void registerVoiceInteractionSessionListener() {
        mAssistUtils.registerVoiceInteractionSessionListener(new IVoiceInteractionSessionListener.Stub() {

            public void onVoiceSessionHidden() throws RemoteException {
            }

            public void onVoiceSessionShown() throws RemoteException {
            }

            public void onSetUiHints(Bundle bundle) {
                final String string = bundle.getString("action");
                if ("show_assist_handles".equals(string)) {
                    requestAssistHandles();
                    return;
                }
                if ("set_assist_gesture_constrained".equals(string)) {
                    mOverviewProxyService.setSystemUiStateFlag(4096, bundle.getBoolean("should_constrain", false), 0);
                    return;
                }
                if (mNgaUiController.extractNga(bundle) != mNgaPresent) {
                    checkAssistantStatus(bundle);
                }
                if (mNgaPresent) {
                    mUiController.processBundle(bundle);
                } else {
                    Log.e("AssistManagerGoogle", "Got a uiHints bundle, but NGA is not active");
                }
            }
        });
    }

    public void onInvocationProgress(int i, float f) {
        if (f == 0.0f || f == 1.0f) {
            mCheckAssistantStatus = true;
            if (i == 2) {
                checkSqueezeGestureStatus();
            }
        }
        if (mCheckAssistantStatus) {
            checkAssistantStatus(null);
        }
        if (i != 2 || mSqueezeSetUp) {
            mUiController.onInvocationProgress(i, f);
        }
    }

    public void onGestureCompletion(float f) {
        mCheckAssistantStatus = true;
        mUiController.onGestureCompletion(f / mContext.getResources().getDisplayMetrics().density);
    }

    public void logStartAssist(int i, int i2) {
        checkAssistantStatus(null);
        MetricsLogger.action(new LogMaker(1716).setType(1).setSubtype((((!mNgaPresent || !mIsGoogleAssistant) ? 0 : 1) << 8) | toLoggingSubType(i, i2)));
    }

    public void addOpaEnabledListener(OpaEnabledListener opaEnabledListener) {
        mOpaEnabledReceiver.addOpaEnabledListener(opaEnabledListener);
    }

    public boolean isActiveAssistantNga() {
        return mNgaPresent;
    }

    public void dispatchOpaEnabledState() {
        mOpaEnabledReceiver.dispatchOpaEnabledState();
    }

    private void checkAssistantStatus(Bundle bundle) {
        ComponentName assistComponentForUser = mAssistUtils.getAssistComponentForUser(-2);
        boolean isGoogleAssistant = assistComponentForUser != null && "com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService".equals(assistComponentForUser.flattenToString());
        boolean ngaPresent = bundle == null ? mNgaPresent : mNgaUiController.extractNga(bundle);
        if (isGoogleAssistant != mIsGoogleAssistant || ngaPresent != mNgaPresent) {
            if (isGoogleAssistant && ngaPresent) {
                if (mUiController.equals(mDefaultUiController)) {
                    Objects.requireNonNull(mUiController);
                    mUiHandler.post(() -> mUiController.hide());
                    mUiController = mNgaUiController;
                }
            } else if (isGoogleAssistant) {
                Objects.requireNonNull(mUiController);
                mUiHandler.post(() -> mUiController.hide());
                mUiController = mDefaultUiController;
                mDefaultUiController.setGoogleAssistant(true);
            } else {
                Objects.requireNonNull(mUiController);
                mUiHandler.post(() -> mUiController.hide());
                mUiController = mDefaultUiController;
                mDefaultUiController.setGoogleAssistant(false);
            }
            mNgaPresent = ngaPresent;
            mIsGoogleAssistant = isGoogleAssistant;
        }
        mCheckAssistantStatus = false;
    }

    public boolean shouldShowOrb() {
        return false;
    }

    // FIXME: For animation squeeze integration.
    private void checkSqueezeGestureStatus() {
        boolean z = false;
        if (Settings.Secure.getInt(mContext.getContentResolver(), "assist_gesture_setup_complete", 0) == 1) {
            z = true;
        }
        mSqueezeSetUp = z;
    }
}

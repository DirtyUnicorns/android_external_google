package com.google.android.systemui.elmyra;

import android.content.Context;
import com.google.android.systemui.elmyra.actions.Action;
import com.google.android.systemui.elmyra.actions.CustomActions;
import com.google.android.systemui.elmyra.feedback.FeedbackEffect;
import com.google.android.systemui.elmyra.feedback.HapticClick;
import com.google.android.systemui.elmyra.feedback.NavUndimEffect;
import com.google.android.systemui.elmyra.feedback.SquishyNavigationButtons;
import com.google.android.systemui.elmyra.feedback.UserActivity;
import com.google.android.systemui.elmyra.gates.ChargingState;
import com.google.android.systemui.elmyra.gates.Gate;
import com.google.android.systemui.elmyra.gates.KeyguardDeferredSetup;
import com.google.android.systemui.elmyra.gates.KeyguardProximity;
import com.google.android.systemui.elmyra.gates.NavigationBarVisibility;
import com.google.android.systemui.elmyra.gates.PowerSaveState;
import com.google.android.systemui.elmyra.gates.SystemKeyPress;
import com.google.android.systemui.elmyra.gates.TelephonyActivity;
import com.google.android.systemui.elmyra.gates.UsbState;
import com.google.android.systemui.elmyra.gates.VrMode;
import com.google.android.systemui.elmyra.gates.WakeMode;
import com.google.android.systemui.elmyra.sensors.CHREGestureSensor;
import com.google.android.systemui.elmyra.sensors.GestureSensor;
import com.google.android.systemui.elmyra.sensors.JNIGestureSensor;
import com.google.android.systemui.elmyra.sensors.config.GestureConfiguration;
import com.google.android.systemui.elmyra.sensors.config.ScreenStateAdjustment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceConfigurationGoogle implements ServiceConfiguration {
    private final List<Action> mActions = new ArrayList();
    private final Context mContext;
    private final List<FeedbackEffect> mFeedbackEffects;
    private final List<Gate> mGates;
    private final GestureSensor mGestureSensor;

    public ServiceConfigurationGoogle(Context context) {
        this.mContext = context;
        this.mActions.add(new CustomActions(context));
        this.mFeedbackEffects = new ArrayList();
        this.mFeedbackEffects.add(new HapticClick(context));
        this.mFeedbackEffects.add(new SquishyNavigationButtons(context));
        this.mFeedbackEffects.add(new NavUndimEffect(context));
        this.mFeedbackEffects.add(new UserActivity(context));
        this.mGates = new ArrayList();
        this.mGates.add(new WakeMode(context));
        this.mGates.add(new ChargingState(context));
        this.mGates.add(new UsbState(context));
        this.mGates.add(new KeyguardProximity(context));
        this.mGates.add(new NavigationBarVisibility(context, mActions));
        this.mGates.add(new SystemKeyPress(context));
        this.mGates.add(new TelephonyActivity(context));
        this.mGates.add(new VrMode(context));
        this.mGates.add(new KeyguardDeferredSetup(context, mActions));
        this.mGates.add(new PowerSaveState(context));
        List arrayList = new ArrayList();
        arrayList.add(new ScreenStateAdjustment(context));
        GestureConfiguration gestureConfiguration = new GestureConfiguration(context, arrayList);
        if (JNIGestureSensor.isAvailable(context)) {
            this.mGestureSensor = new JNIGestureSensor(context, gestureConfiguration);
        } else {
            this.mGestureSensor = new CHREGestureSensor(context, gestureConfiguration, new SnapshotConfiguration(context));
        }
    }

    @Override
    public List<Action> getActions() {
        return this.mActions;
    }

    @Override
    public List<FeedbackEffect> getFeedbackEffects() {
        return this.mFeedbackEffects;
    }

    @Override
    public List<Gate> getGates() {
        return this.mGates;
    }

    @Override
    public GestureSensor getGestureSensor() {
        return this.mGestureSensor;
    }
}

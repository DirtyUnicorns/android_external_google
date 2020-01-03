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
        mContext = context;
        mActions.add(new CustomActions(context));
        mFeedbackEffects = new ArrayList();
        mFeedbackEffects.add(new HapticClick(context));
        mFeedbackEffects.add(new SquishyNavigationButtons(context));
        mFeedbackEffects.add(new NavUndimEffect());
        mFeedbackEffects.add(new UserActivity(context));
        mGates = new ArrayList();
        mGates.add(new WakeMode(context));
        mGates.add(new ChargingState(context));
        mGates.add(new UsbState(context));
        mGates.add(new KeyguardProximity(context));
        mGates.add(new NavigationBarVisibility(context, mActions));
        mGates.add(new SystemKeyPress(context));
        mGates.add(new TelephonyActivity(context));
        mGates.add(new VrMode(context));
        mGates.add(new KeyguardDeferredSetup(context, mActions));
        mGates.add(new PowerSaveState(context));
        List arrayList = new ArrayList();
        arrayList.add(new ScreenStateAdjustment(context));
        GestureConfiguration gestureConfiguration = new GestureConfiguration(context, arrayList);
        mGestureSensor = new CHREGestureSensor(context, gestureConfiguration, new SnapshotConfiguration(context));
    }

    @Override
    public List<Action> getActions() {
        return mActions;
    }

    @Override
    public List<FeedbackEffect> getFeedbackEffects() {
        return mFeedbackEffects;
    }

    @Override
    public List<Gate> getGates() {
        return mGates;
    }

    @Override
    public GestureSensor getGestureSensor() {
        return mGestureSensor;
    }
}

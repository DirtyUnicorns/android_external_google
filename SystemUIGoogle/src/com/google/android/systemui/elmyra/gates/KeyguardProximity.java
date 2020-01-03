package com.google.android.systemui.elmyra.gates;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.util.AsyncSensorManager;
import com.google.android.systemui.elmyra.gates.Gate.Listener;

public class KeyguardProximity extends Gate {
    private final Listener mGateListener = new KeyguardProximityListenerImpl();
    private boolean mIsListening = false;
    private final KeyguardVisibility mKeyguardGate;
    private boolean mProximityBlocked = false;
    private final float mProximityThreshold;
    private final SensorManager mSensorManager = ((SensorManager) Dependency.get(AsyncSensorManager.class));
    private final Sensor mSensor = mSensorManager.getDefaultSensor(8);
    private final SensorEventListener mSensorListener = new SensorListenerImpl();

    private class KeyguardProximityListenerImpl implements Listener {
        KeyguardProximityListenerImpl() {
        }

        public void onGateChanged(Gate gate) {
            updateProximityListener();
        }
    }

    class SensorListenerImpl implements SensorEventListener {
        SensorListenerImpl() {
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            boolean z = false;
            if (sensorEvent.values[0] < KeyguardProximity.this.mProximityThreshold) {
                z = true;
            }
            if (mIsListening && z != mProximityBlocked) {
                mProximityBlocked = z;
                notifyListener();
            }
        }
    }

    public KeyguardProximity(Context context) {
        super(context);
        if (mSensor == null) {
            mProximityThreshold = 0.0f;
            mKeyguardGate = null;
            return;
        }
        mProximityThreshold = Math.min(mSensor.getMaximumRange(), (float) context.getResources().getInteger(R.integer.elmyra_keyguard_proximity_threshold));
        mKeyguardGate = new KeyguardVisibility(context);
        mKeyguardGate.setListener(mGateListener);
        updateProximityListener();
    }

    private void updateProximityListener() {
        if (mProximityBlocked) {
            mProximityBlocked = false;
            notifyListener();
        }
        if (!isActive() || !mKeyguardGate.isKeyguardShowing() || mKeyguardGate.isKeyguardOccluded()) {
            mSensorManager.unregisterListener(mSensorListener);
            mIsListening = false;
        } else if (!mIsListening) {
            mSensorManager.registerListener(mSensorListener, mSensor, 3);
            mIsListening = true;
        }
    }

    protected boolean isBlocked() {
        return mIsListening && mProximityBlocked;
    }

    protected void onActivate() {
        if (mSensor != null) {
            mKeyguardGate.activate();
            updateProximityListener();
        }
    }

    protected void onDeactivate() {
        if (mSensor != null) {
            mKeyguardGate.deactivate();
            updateProximityListener();
        }
    }
}
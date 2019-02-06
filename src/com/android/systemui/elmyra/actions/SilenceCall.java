package com.google.android.systemui.elmyra.actions;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.google.android.systemui.elmyra.UserContentObserver;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class SilenceCall extends Action {
    private boolean mIsPhoneRinging;
    private final PhoneStateListener mPhoneStateListener = new C15921();
    private final UserContentObserver mSettingsObserver;
    private boolean mSilenceSettingEnabled;
    private final TelephonyManager mTelephonyManager = ((TelephonyManager) getContext().getSystemService("phone"));

    /* renamed from: com.google.android.systemui.elmyra.actions.SilenceCall$1 */
    class C15921 extends PhoneStateListener {
        C15921() {
        }

        public void onCallStateChanged(int i, String str) {
            boolean access$000 = isPhoneRinging(i);
            if (mIsPhoneRinging != access$000) {
                mIsPhoneRinging = access$000;
                notifyListener();
            }
        }
    }

    public SilenceCall(Context context) {
        super(context, null);
        updatePhoneStateListener();
        mSettingsObserver = new UserContentObserver(getContext(), Secure.getUriFor("assist_gesture_silence_alerts_enabled"), new _$$Lambda$SilenceCall$P91IyaoSIoRZpeDIyPp8173JrBg(this));
    }

    private boolean isPhoneRinging(int i) {
        return i == 1;
    }

    protected void updatePhoneStateListener() {
        boolean z = true;
        int i = 0;
        if (Secure.getIntForUser(getContext().getContentResolver(), "assist_gesture_silence_alerts_enabled", 1, -2) == 0) {
            z = false;
        }
        if (z != mSilenceSettingEnabled) {
            mSilenceSettingEnabled = z;
            if (mSilenceSettingEnabled) {
                i = 32;
            }
            mTelephonyManager.listen(mPhoneStateListener, i);
            mIsPhoneRinging = isPhoneRinging(mTelephonyManager.getCallState());
            notifyListener();
        }
    }

    @Override
	public boolean isAvailable() {
        return mSilenceSettingEnabled ? mIsPhoneRinging : false;
    }

    public void onTrigger(DetectionProperties detectionProperties) {
        mTelephonyManager.silenceRinger();
    }

    @Override
	public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append(" [mSilenceSettingEnabled -> ");
        stringBuilder.append(mSilenceSettingEnabled);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}

package com.google.android.systemui.elmyra.actions;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.google.android.systemui.elmyra.UserContentObserver;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class UnpinNotifications extends Action {
    private boolean mHasPinnedHeadsUp;
    private final OnHeadsUpChangedListener mHeadsUpChangedListener = new C15931();
    private final HeadsUpManager mHeadsUpManager;
    private final UserContentObserver mSettingsObserver;
    private boolean mSilenceSettingEnabled;

    /* renamed from: com.google.android.systemui.elmyra.actions.UnpinNotifications$1 */
    class C15931 implements OnHeadsUpChangedListener {
        C15931() {
        }

        public void onHeadsUpPinnedModeChanged(boolean z) {
            if (mHasPinnedHeadsUp != z) {
                mHasPinnedHeadsUp = z;
                notifyListener();
            }
        }
    }

    public UnpinNotifications(Context context) {
        super(context, null);
        mHeadsUpManager = (HeadsUpManager) SysUiServiceProvider.getComponent(context, HeadsUpManager.class);
        if (mHeadsUpManager != null) {
            updateHeadsUpListener();
            mSettingsObserver = new UserContentObserver(getContext(), Secure.getUriFor("assist_gesture_silence_alerts_enabled"), new _$$Lambda$UnpinNotifications$Coju1I9MwFJHZmrlRAr_VaZtdE4(this));
            return;
        }
        mSettingsObserver = null;
        Log.w("Elmyra/UnpinNotifications", "No HeadsUpManager");
    }

    protected void updateHeadsUpListener() {
        boolean z = true;
        if (Secure.getIntForUser(getContext().getContentResolver(), "assist_gesture_silence_alerts_enabled", 1, -2) == 0) {
            z = false;
        }
        if (mSilenceSettingEnabled != z) {
            mSilenceSettingEnabled = z;
            if (mSilenceSettingEnabled) {
                mHasPinnedHeadsUp = mHeadsUpManager.hasPinnedHeadsUp();
                mHeadsUpManager.addListener(mHeadsUpChangedListener);
            } else {
                mHasPinnedHeadsUp = false;
                mHeadsUpManager.removeListener(mHeadsUpChangedListener);
            }
            notifyListener();
        }
    }

    @Override
	public boolean isAvailable() {
        return mSilenceSettingEnabled ? mHasPinnedHeadsUp : false;
    }

    public void onTrigger(DetectionProperties detectionProperties) {
        if (mHeadsUpManager != null) {
            mHeadsUpManager.unpinAll();
        }
    }

    @Override
	public String toString() {
        return super.toString();
    }
}

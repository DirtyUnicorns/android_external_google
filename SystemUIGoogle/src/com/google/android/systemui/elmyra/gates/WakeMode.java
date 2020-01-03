package com.google.android.systemui.elmyra.gates;

import android.content.Context;
import android.provider.Settings.Secure;
import com.google.android.systemui.elmyra.UserContentObserver;
import java.util.function.Consumer;

public class WakeMode extends PowerState {
    private final UserContentObserver mSettingsObserver = new UserContentObserver(getContext(), Secure.getUriFor("assist_gesture_wake_enabled"),
     new WakeModeConsumer(this), false);
    private boolean mWakeSettingEnabled;

    public WakeMode(Context context) {
        super(context);
    }

    private boolean isWakeSettingEnabled() {
        return Secure.getIntForUser(getContext().getContentResolver(), "assist_gesture_wake_enabled", 1, -2) != 0;
    }

    protected void updateWakeSetting() {
        boolean isWakeSettingEnabled = isWakeSettingEnabled();
        if (isWakeSettingEnabled != this.mWakeSettingEnabled) {
            this.mWakeSettingEnabled = isWakeSettingEnabled;
            notifyListener();
        }
    }

    protected boolean isBlocked() {
        return this.mWakeSettingEnabled ? false : super.isBlocked();
    }

    protected void onActivate() {
        this.mWakeSettingEnabled = isWakeSettingEnabled();
        this.mSettingsObserver.activate();
    }

    protected void onDeactivate() {
        this.mSettingsObserver.deactivate();
    }

    private class WakeModeConsumer implements Consumer {
        private WakeMode wakeMode;

        public WakeModeConsumer(WakeMode wM) {
            wakeMode = wM;
        }

        public final void accept(Object wM) {
            wakeMode.updateWakeSetting();
        }
    }
}

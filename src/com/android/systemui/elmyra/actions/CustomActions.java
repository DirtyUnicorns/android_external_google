package com.google.android.systemui.elmyra.actions;

import android.content.Context;
import android.content.ContentResolver;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.internal.util.du.ActionUtils;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class CustomActions extends Action {

    private int mActionSelection;

    public CustomActions(Context context) {
        super(context, null);
    }

    public boolean isAvailable() {
        return true;
    }

    public void onTrigger(DetectionProperties detectionProperties) {
        final ContentResolver resolver = getContext().getContentResolver();

        mActionSelection = Settings.System.getIntForUser(resolver,
                Settings.System.SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT);

        switch (mActionSelection) {
            case 0:
            default:
                break;
            case 1:
                ActionUtils.switchScreenOff(getContext());
                break;
            case 2:
                ActionUtils.toggleCameraFlash();
                break;
            case 3:
                ActionUtils.toggleVolumePanel(getContext());
                break;
            case 4:
                ActionUtils.clearAllNotifications();
                break;
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append(" [CustomAction Enabled -> ");
        stringBuilder.append(true);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}

package com.google.android.systemui.elmyra.actions;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.internal.util.du.ActionUtils;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistManager;

import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class CustomActions extends Action {

    private AssistManager mAssistManager;
    private PowerManager mPm;
    private ContentResolver mResolver;

    public CustomActions(Context context) {
        super(context, null);
        mResolver = context.getContentResolver();
        mAssistManager = Dependency.get(AssistManager.class);
        mPm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public void onTrigger(DetectionProperties detectionProperties) {
	    // Get action to activate based on the squeeze type
        int mActionSelection = dispatchAction(detectionProperties.isLongSqueeze());

        // Check if the screen is turned on
        boolean isScreenOn = mPm.isScreenOn();

        switch (mActionSelection) {
            case 0: // No action
            default:
                break;
            case 1: // Assistant
                ActionUtils.switchScreenOn(getContext());
                mAssistManager.startAssist(new Bundle() /* args */);
                break;
            case 2: // Voice search
                if (isScreenOn) {
                    ActionUtils.launchVoiceSearch(getContext());
                }
                break;
            case 3: // Camera
                ActionUtils.switchScreenOn(getContext());
                ActionUtils.launchCamera(getContext());
                break;
            case 4: // Flashlight
                ActionUtils.toggleCameraFlash();
                break;
            case 5: // Application
                if (isScreenOn) {
                    launchApp(getContext(), detectionProperties.isLongSqueeze());
                }
                break;
            case 6: // Volume panel
                if (isScreenOn) {
                    ActionUtils.toggleVolumePanel(getContext());
                }
                break;
            case 7: // Screen off
                if (isScreenOn) {
                    ActionUtils.switchScreenOff(getContext());
                }
                break;
            case 8: // Screenshot
                if (isScreenOn) {
                    ActionUtils.takeScreenshot(true);
                }
                break;
            case 9: // Notification panel
                if (isScreenOn) {
                    ActionUtils.toggleNotifications();
                }
                break;
            case 10: // QS panel
                if (isScreenOn) {
                    ActionUtils.toggleQsPanel();
                }
                break;
            case 11: // Clear notifications
                ActionUtils.clearAllNotifications();
                break;
            case 12: // Ringer modes
                ActionUtils.toggleRingerModes(getContext());
                break;
        }
    }

    private int dispatchAction(boolean longSqueeze) {
        if (longSqueeze) {
            return Settings.Secure.getIntForUser(mResolver,
                    Settings.Secure.LONG_SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT);
        } else {
            return Settings.Secure.getIntForUser(mResolver,
                    Settings.Secure.SHORT_SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT);
        }
    }

    private void launchApp(Context context, boolean isLongSqueeze) {
        Intent intent = null;
        String packageName = Settings.Secure.getStringForUser(context.getContentResolver(),
                isLongSqueeze ? Settings.Secure.LONG_SQUEEZE_CUSTOM_APP
                : Settings.Secure.SHORT_SQUEEZE_CUSTOM_APP,
                UserHandle.USER_CURRENT);
        String activity = Settings.Secure.getStringForUser(context.getContentResolver(),
                isLongSqueeze ? Settings.Secure.LONG_SQUEEZE_CUSTOM_ACTIVITY
                : Settings.Secure.SHORT_SQUEEZE_CUSTOM_ACTIVITY,
                UserHandle.USER_CURRENT);
        boolean launchActivity = activity != null && !TextUtils.equals("NONE", activity);
        try {
            if (launchActivity) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName(packageName, activity);
            } else {
                intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }
}

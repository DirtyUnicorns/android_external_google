package com.google.android.systemui.elmyra.actions;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;

import com.android.internal.util.du.ActionUtils;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistManager;

import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class CustomActions extends Action {

    private int mActionSelection;
    protected AssistManager mAssistManager;

    public CustomActions(Context context) {
        super(context, null);
        mAssistManager = Dependency.get(AssistManager.class);
    }

    @Override
	public boolean isAvailable() {
        return true;
    }

    public void onTrigger(DetectionProperties detectionProperties) {
        final ContentResolver resolver = getContext().getContentResolver();

        mActionSelection = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT);

        switch (mActionSelection) {
            case 0: // No action
            default:
                break;
            case 1: // Assistant
                mAssistManager.startAssist(new Bundle() /* args */);
                break;
            case 2: // Voice search
                launchVoiceSearch(getContext());
                break;
            case 3: // Camera
                launchCamera(getContext());
                break;
            case 4: // Flashlight
                ActionUtils.toggleCameraFlash();
                break;
            case 5: // Clear notifications
                ActionUtils.clearAllNotifications();
                break;
            case 6: // Volume panel
                ActionUtils.toggleVolumePanel(getContext());
                break;
            case 7: // Screen off
                ActionUtils.switchScreenOff(getContext());
                break;
            case 8: // Notification panel
                ActionUtils.toggleNotifications();
                break;
            case 9: // Screenshot
                ActionUtils.takeScreenshot(true);
                break;
            case 10: // QS panel
                ActionUtils.toggleQsPanel();
                break;
        }
    }

    private static void launchCamera(Context context) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private void launchVoiceSearch(Context context) {
        Intent intent = new Intent(Intent.ACTION_SEARCH_LONG_PRESS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

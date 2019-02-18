package com.google.android.systemui.elmyra.feedback;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.os.PowerManager;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class HapticClick implements FeedbackEffect {
    private static final AudioAttributes SONIFICATION_AUDIO_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private int mLastGestureStage;
    private final VibrationEffect mProgressVibrationEffect = VibrationEffect.get(5);
    private final VibrationEffect mResolveVibrationEffect = VibrationEffect.get(0);
    private final Vibrator mVibrator;
    private ContentResolver resolver;
    private PowerManager pm;

    public HapticClick(Context context) {
        resolver = context.getContentResolver();
        mVibrator = (Vibrator) context.getSystemService("vibrator");
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onProgress(float f, int i) {
        /* Disable the vibration for certain actions while the screen
         * is turned off and/or for when there's no action used.*/
        int squeezeSelection = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT);

        // Check if the screen is turned on
        if (pm == null) return;
        boolean isScreenOn = pm.isScreenOn();

        switch (squeezeSelection) {
            case 0: // No action
            default:
                return;
            case 1: // Assistant
                break;
            case 2: // Voice search
                if (!isScreenOn) {
                    return;
                }
                break;
            case 3: // Camera
                break;
            case 4: // Flashlight
                break;
            case 5: // Clear notifications
                break;
            case 6: // Volume panel
                if (!isScreenOn) {
                    return;
                }
                break;
            case 7: // Screen off
                if (!isScreenOn) {
                    return;
                }
                break;
            case 8: // Notification panel
                if (!isScreenOn) {
                    return;
                }
                break;
            case 9: // Screenshot
                if (!isScreenOn) {
                    return;
                }
                break;
            case 10: // QS panel
                if (!isScreenOn) {
                    return;
                }
                break;
            case 11: // Application
                if (!isScreenOn) {
                    return;
                }
                break;
            case 12: // Ringer modes
                break;
        }
        if (!(mLastGestureStage == 2 || i != 2 || mVibrator == null)) {
            mVibrator.vibrate(mProgressVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES);
        }
        mLastGestureStage = i;
    }

    @Override
    public void onRelease() {
    }

    public void onResolve(DetectionProperties detectionProperties) {
        /* Disable the vibration for certain actions while the screen
         * is turned off and/or for when there's no action used.*/
        int squeezeSelection = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT);

        // Check if the screen is turned on
        boolean isScreenOn = pm.isScreenOn();

        switch (squeezeSelection) {
            case 0: // No action
            default:
                return;
            case 1: // Assistant
                break;
            case 2: // Voice search
                if (!isScreenOn) {
                    return;
                }
                break;
            case 3: // Camera
                break;
            case 4: // Flashlight
                break;
            case 5: // Clear notifications
                break;
            case 6: // Volume panel
                if (!isScreenOn) {
                    return;
                }
                break;
            case 7: // Screen off
                if (!isScreenOn) {
                    return;
                }
                break;
            case 8: // Notification panel
                if (!isScreenOn) {
                    return;
                }
                break;
            case 9: // Screenshot
                if (!isScreenOn) {
                    return;
                }
                break;
            case 10: // QS panel
                if (!isScreenOn) {
                    return;
                }
                break;
            case 11: // Application
                if (!isScreenOn) {
                    return;
                }
                break;
            case 12: // Ringer modes
                break;
        }
        if ((detectionProperties == null || !detectionProperties.isHapticConsumed()) && mVibrator != null) {
            mVibrator.vibrate(mResolveVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES);
        }
    }
}

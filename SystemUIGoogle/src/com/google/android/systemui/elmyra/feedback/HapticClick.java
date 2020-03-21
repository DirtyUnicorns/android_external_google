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
    private ContentResolver mResolver;
    private PowerManager mPm;

    public HapticClick(Context context) {
        mResolver = context.getContentResolver();
        mVibrator = (Vibrator) context.getSystemService("vibrator");
        mPm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onProgress(float f, int i) {
        /* Disable the vibration for certain actions while the screen is turned off.
           We just have to check for shortSqueezeSelection in this case because
           the longSqueeze is dependant on shortSqueeze vibration. The final
           vibration will be handled by onResolve.*/
        int shortSqueezeSelection = Settings.Secure.getIntForUser(mResolver,
                Settings.Secure.SHORT_SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT);

        boolean longSqueezeSelection = Settings.Secure.getIntForUser(mResolver,
                Settings.Secure.LONG_SQUEEZE_SELECTION, 0, UserHandle.USER_CURRENT) == 0;

        if (shortSqueezeSelection == 0 && longSqueezeSelection) {
            return;
        }

        // Check whether the screen is on or off
        boolean isScreenOn = mPm.isScreenOn();

        switch (shortSqueezeSelection) {
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
            case 13: // Kill app
            case 14: // Skip song
            case 15: // Previous song
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
        int squeezeSelection = dispatchAction(detectionProperties.isLongSqueeze());

        // Check if the screen is turned on
        boolean isScreenOn = mPm.isScreenOn();

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
            case 13: // Kill app
            case 14: // Skip song
            case 15: // Previous song
        }
        if ((!detectionProperties.isHapticConsumed()) && mVibrator != null) {
            mVibrator.vibrate(mResolveVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES);
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
}

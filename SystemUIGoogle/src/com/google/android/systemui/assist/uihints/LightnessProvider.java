package com.google.android.systemui.assist.uihints;

import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.CompositionSamplingListener;

public final class LightnessProvider {
    private boolean mCardVisible = false;
    private int mColorMode = 0;
    private final CompositionSamplingListener mColorMonitor = new CompositionSamplingListener(LightnessProviderLambda.INSTANCE) {
        public void onSampleCollected(float f) {
            mUiHandler.post(() -> lambdaLightnessProvider(f));
        }
        private /* synthetic */ void lambdaLightnessProvider(float f) {
            if (mMuted) {
                return;
            }
            if (!mCardVisible || mColorMode == 0) {
                mListener.onLightnessUpdate(f);
            }
        }
    };
    private boolean mIsMonitoringColor = false;
    private final LightnessListener mListener;
    private boolean mMuted = false;
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    LightnessProvider(LightnessListener lightnessListener) {
        mListener = lightnessListener;
    }

    public void setMuted(boolean z) {
        mMuted = z;
    }

    void enableColorMonitoring(boolean z, Rect rect, IBinder iBinder) {
        if (mIsMonitoringColor != z) {
            mIsMonitoringColor = z;
            if (mIsMonitoringColor) {
                CompositionSamplingListener.register(mColorMonitor, 0, iBinder, rect);
            } else {
                CompositionSamplingListener.unregister(mColorMonitor);
            }
        }
    }

    public void setCardVisible(boolean visible, int mode) {
        mCardVisible = visible;
        mColorMode = mode;
        if (!mCardVisible) {
            return;
        }
        if (mode == 1) {
            mListener.onLightnessUpdate(0.0f);
        } else if (mode == 2) {
            mListener.onLightnessUpdate(1.0f);
        }
    }
}

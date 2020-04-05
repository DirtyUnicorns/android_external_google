package com.google.android.systemui.assist.uihints;

import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.CompositionSamplingListener;
import com.google.android.systemui.assist.uihints.LightnessProvider;

public final class LightnessProvider {
    /* access modifiers changed from: private */
    public boolean mCardVisible = false;
    /* access modifiers changed from: private */
    public int mColorMode = 0;
    private final CompositionSamplingListener mColorMonitor = new CompositionSamplingListener($$Lambda$_14QHG018Z6p13d3hzJuGTWnNeo.INSTANCE) {
        /* class com.google.android.systemui.assist.uihints.LightnessProvider.C15641 */

        public void onSampleCollected(float f) {
            mUiHandler.post(() -> lambdaLightnessProvider(f));
        }

        // FIXME
        public /* synthetic */ void lambdaLightnessProvider(float f) {
            if (LightnessProvider.this.mMuted) {
                return;
            }
            if (!LightnessProvider.this.mCardVisible || LightnessProvider.this.mColorMode == 0) {
                LightnessProvider.this.mListener.onLightnessUpdate(f);
            }
        }
    };
    private boolean mIsMonitoringColor = false;
    /* access modifiers changed from: private */
    public final LightnessListener mListener;
    /* access modifiers changed from: private */
    public boolean mMuted = false;
    /* access modifiers changed from: private */
    public final Handler mUiHandler = new Handler(Looper.getMainLooper());

    LightnessProvider(LightnessListener lightnessListener) {
        this.mListener = lightnessListener;
    }

    public void setMuted(boolean z) {
        this.mMuted = z;
    }

    /* access modifiers changed from: package-private */
    public void enableColorMonitoring(boolean z, Rect rect, IBinder iBinder) {
        if (this.mIsMonitoringColor != z) {
            this.mIsMonitoringColor = z;
            if (this.mIsMonitoringColor) {
                CompositionSamplingListener.register(this.mColorMonitor, 0, iBinder, rect);
            } else {
                CompositionSamplingListener.unregister(this.mColorMonitor);
            }
        }
    }

    public void setCardVisible(boolean z, int i) {
        this.mCardVisible = z;
        this.mColorMode = i;
        if (!this.mCardVisible) {
            return;
        }
        if (i == 1) {
            this.mListener.onLightnessUpdate(0.0f);
        } else if (i == 2) {
            this.mListener.onLightnessUpdate(1.0f);
        }
    }
}

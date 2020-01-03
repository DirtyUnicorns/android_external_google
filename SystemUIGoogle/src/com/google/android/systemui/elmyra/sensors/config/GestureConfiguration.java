package com.google.android.systemui.elmyra.sensors.config;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings.Secure;
import android.util.Range;
import com.android.systemui.R;
import com.google.android.systemui.elmyra.UserContentObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GestureConfiguration {
    private static final Range<Float> SENSITIVITY_RANGE = Range.create(0.0f, 1.0f);
    protected final Consumer<Adjustment> mAdjustmentCallback = new AdjustmentCallbackConsumer(this);
    private final List<Adjustment> mAdjustments;
    private final Context mContext;
    private Listener mListener;
    private int[] mLowerThreshold;
    private float mSensitivity;
    private int[] mSlopeSensitivity;
    private int[] mTimeWindow;
    private int[] mUpperThreshold;

    public interface Listener {
        void onGestureConfigurationChanged(GestureConfiguration gestureConfiguration);
    }

    public GestureConfiguration(Context context, List<Adjustment> list) {
        mContext = context;
        mAdjustments = new ArrayList(list);
        mAdjustments.forEach(adjustment -> adjustment.setCallback(mAdjustmentCallback));
        Resources resources = context.getResources();
        new UserContentObserver(mContext, Secure.getUriFor(
                "assist_gesture_sensitivity"), new AdjustmentCallbackConsumer(this));
        mUpperThreshold = resources.getIntArray(R.array.elmyra_upper_threshold);
        mSlopeSensitivity = resources.getIntArray(R.array.elmyra_slope_sensitivity);
        mLowerThreshold = resources.getIntArray(R.array.elmyra_lower_threshold);
        mTimeWindow = resources.getIntArray(R.array.elmyra_time_window);
        mSensitivity = getUserSensitivity();
    }

    private float calculateFraction(float f, float f2, float f3) {
        return ((f2 - f) * f3) + f;
    }

    private float calculateFraction(int[] iArr, float f) {
        return calculateFraction((float) iArr[1], (float) iArr[0], f);
    }

    private float getUserSensitivity() {
        float floatForUser = Secure.getFloatForUser(
                mContext.getContentResolver(), "assist_gesture_sensitivity", 0.5f, -2);
        return floatForUser / 8f;
    }

    public float getLowerThreshold() {
        return calculateFraction(mLowerThreshold, mSensitivity);
    }

    public float getSensitivity() {
        //TODO: improve logic for adjustments, for now they are bypassed as we don't wanna use them.

        /*int i = 0;
        float f = mSensitivity;
        while (true) {
            int i2 = i;
            if (i2 >= mAdjustments.size()) {
                return f;
            }
            f = SENSITIVITY_RANGE.clamp(Float.valueOf(((
                    Adjustment) mAdjustments.get(i2)).adjustSensitivity(f))).floatValue();
            i = i2 + 1;
        }*/
        return mSensitivity;
    }

    public float getSlopeSensitivity() {
        return calculateFraction(mSlopeSensitivity, mSensitivity) / 100.0f;
    }

    public int getTimeWindow() {
        return (int) calculateFraction(mTimeWindow, mSensitivity);
    }

    public float getUpperThreshold() {
        return calculateFraction(mUpperThreshold, mSensitivity);
    }

    /* renamed from: onSensitivityChanged */
    public void onSensitivityChanged() {
        mSensitivity = getUserSensitivity();
        if (mListener != null) {
            mListener.onGestureConfigurationChanged(this);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private class AdjustmentCallbackConsumer implements Consumer {
        private final GestureConfiguration gestureConfiguration;

        public AdjustmentCallbackConsumer(GestureConfiguration gestureConfig) {
            gestureConfiguration = gestureConfig;
        }

        @Override
        public final void accept(Object obj) {
            gestureConfiguration.onSensitivityChanged();
        }
    };
}

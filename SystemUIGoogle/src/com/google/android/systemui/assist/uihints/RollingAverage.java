package com.google.android.systemui.assist.uihints;

public class RollingAverage {
    private int mIndex;
    private float[] mSamples;
    private int mSize;
    private float mTotal = 0.0f;

    public RollingAverage(int i) {
        mIndex = 0;
        mSize = i;
        mSamples = new float[i];
        for (int i2 = 0; i2 < i; i2++) {
            mSamples[i2] = 0.0f;
        }
    }

    public void add(float f) {
        int i = mSize;
        if (i > 0) {
            float f2 = mTotal;
            float[] fArr = mSamples;
            int i2 = mIndex;
            mTotal = f2 - fArr[i2];
            fArr[i2] = f;
            mTotal += f;
            int i3 = i2 + 1;
            mIndex = i3;
            if (i3 == i) {
                mIndex = 0;
            }
        }
    }

    public double getAverage() {
        return (double) (mTotal / ((float) mSize));
    }
}

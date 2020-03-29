package com.google.android.systemui.assist.uihints;

public class RollingAverage {
    private int mIndex;
    private float[] mSamples;
    private int mSize;
    private float mTotal = 0.0f;

    public RollingAverage(int i) {
        this.mIndex = 0;
        this.mSize = i;
        this.mSamples = new float[i];
        for (int i2 = 0; i2 < i; i2++) {
            this.mSamples[i2] = 0.0f;
        }
    }

    public void add(float f) {
        int i = this.mSize;
        if (i > 0) {
            float f2 = this.mTotal;
            float[] fArr = this.mSamples;
            int i2 = this.mIndex;
            this.mTotal = f2 - fArr[i2];
            fArr[i2] = f;
            this.mTotal += f;
            int i3 = i2 + 1;
            this.mIndex = i3;
            if (i3 == i) {
                this.mIndex = 0;
            }
        }
    }

    public double getAverage() {
        return (double) (this.mTotal / ((float) this.mSize));
    }
}

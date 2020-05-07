package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.android.internal.graphics.ColorUtils;

public final class GradientView extends View {
    private int mBottomColor;
    private int[] mColors;
    private final Paint mGradientPaint;
    private final PathInterpolator mInterpolator;
    private final float[] mStops;
    private int mTopColor;

    public GradientView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GradientView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public GradientView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        mInterpolator = new PathInterpolator(0.5f, 0.5f, 0.7f, 1.0f);
        mColors = new int[100];
        mTopColor = 0;
        mBottomColor = 0;
        mGradientPaint = new Paint();
        mGradientPaint.setDither(true);
        mStops = new float[100];
        for (int i3 = 0; i3 < 100; i3++) {
            mStops[i3] = ((float) i3) / 100.0f;
        }
        updateGradient();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect((float) getLeft(), (float) getTop(), (float) getWidth(), (float) getHeight(), mGradientPaint);
    }

    @Override
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        updateGradient();
    }

    private void updateGradient() {
        for (int i = 0; i < 100; i++) {
            mColors[i] = ColorUtils.blendARGB(mBottomColor, mTopColor, mInterpolator.getInterpolation(mStops[i]));
        }
        mGradientPaint.setShader(new LinearGradient(0.0f, (float) getBottom(), 0.0f, (float) getTop(), mColors, mStops, Shader.TileMode.CLAMP));
        invalidate();
    }
}

package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.assist.ui.EdgeLight;
import com.google.android.systemui.assist.uihints.BlurProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

public final class GlowView extends FrameLayout {
    private ImageView mBlueGlow;
    private BlurProvider mBlurProvider;
    private int mBlurRadius;
    private EdgeLight[] mEdgeLights;
    private RectF mGlowImageCropRegion;
    private final Matrix mGlowImageMatrix;
    private ArrayList<ImageView> mGlowImageViews;
    private float mGlowWidthRatio;
    private ImageView mGreenGlow;
    private int mMinimumHeightPx;
    private final Paint mPaint;
    private ImageView mRedGlow;
    private int mTranslationY;
    private ImageView mYellowGlow;

    public GlowView(Context context) {
        this(context, null);
    }

    public GlowView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GlowView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public GlowView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        mGlowImageCropRegion = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        mGlowImageMatrix = new Matrix();
        mBlurRadius = 0;
        mPaint = new Paint();
        mBlurProvider = new BlurProvider(context, context.getResources().getDrawable(R.drawable.glow_vector, null));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBlueGlow = findViewById(R.id.blue_glow);
        mRedGlow = findViewById(R.id.red_glow);
        mYellowGlow = findViewById(R.id.yellow_glow);
        mGreenGlow = findViewById(R.id.green_glow);
        mGlowImageViews = new ArrayList<>(Arrays.asList(mBlueGlow, mRedGlow, mYellowGlow, mGreenGlow));
    }

    public void setVisibility(int i) {
        int visibility = getVisibility();
        if (visibility != i) {
            super.setVisibility(i);
            if (visibility == 8) {
                setBlurredImageOnViews(mBlurRadius);
            }
        }
    }

    public int getBlurRadius() {
        return mBlurRadius;
    }

    public void setBlurRadius(int i) {
        if (mBlurRadius != i) {
            setBlurredImageOnViews(i);
        }
    }

    private void setBlurredImageOnViews(int i) {
        mBlurRadius = i;
        BlurProvider.BlurResult blurResult = mBlurProvider.get(mBlurRadius);
        mGlowImageCropRegion = blurResult.cropRegion;
        updateGlowImageMatrix();
        mGlowImageViews.forEach(imageView -> {
            imageView.setImageDrawable(blurResult.drawable.getConstantState().newDrawable().mutate());
            imageView.setImageMatrix(mGlowImageMatrix);
        });
    }

    private void updateGlowImageMatrix() {
        mGlowImageMatrix.setRectToRect(mGlowImageCropRegion, new RectF(0.0f, 0.0f, (float) getGlowWidth(), (float) getGlowHeight()), Matrix.ScaleToFit.FILL);
    }

    public void setGlowsY(int i, int i2, EdgeLight[] edgeLightArr) {
        mTranslationY = i;
        mMinimumHeightPx = i2;
        mEdgeLights = edgeLightArr;
        int i3 = 0;
        if (edgeLightArr == null || edgeLightArr.length != 4) {
            while (i3 < 4) {
                mGlowImageViews.get(i3).setTranslationY((float) (getHeight() - i));
                i3++;
            }
            return;
        }
        int i4 = (i - i2) * 4;
        float length = edgeLightArr[0].getLength() + edgeLightArr[1].getLength() + edgeLightArr[2].getLength() + edgeLightArr[3].getLength();
        while (i3 < 4) {
            mGlowImageViews.get(i3).setTranslationY((float) (getHeight() - (((int) ((edgeLightArr[i3].getLength() * ((float) i4)) / length)) + i2)));
            i3++;
        }
    }

    @Override
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        post(() -> {
            updateGlowSizes();
            post(() -> setGlowsY(mTranslationY, mMinimumHeightPx, mEdgeLights));
        });
    }

    public void distributeEvenly() {
        float width = (float) getWidth();
        float f = mGlowWidthRatio / 2.0f;
        float f2 = 0.96f * f;
        float cornerRadiusBottom = (((float) DisplayUtils.getCornerRadiusBottom(mContext)) / width) - f;
        float f3 = cornerRadiusBottom + f2;
        float f4 = f3 + f2;
        mBlueGlow.setX(cornerRadiusBottom * width);
        mRedGlow.setX(f3 * width);
        mYellowGlow.setX(f4 * width);
        mGreenGlow.setX(width * (f2 + f4));
    }

    private void updateGlowSizes() {
        int glowWidth = getGlowWidth();
        int glowHeight = getGlowHeight();
        updateGlowImageMatrix();
        for (ImageView next : mGlowImageViews) {
            LayoutParams layoutParams = (LayoutParams) next.getLayoutParams();
            layoutParams.width = glowWidth;
            layoutParams.height = glowHeight;
            next.setLayoutParams(layoutParams);
            next.setImageMatrix(mGlowImageMatrix);
        }
        distributeEvenly();
    }

    private float getGlowImageAspectRatio() {
        if (mGlowImageCropRegion.width() == 0.0f) {
            return 0.0f;
        }
        return mGlowImageCropRegion.height() / mGlowImageCropRegion.width();
    }

    private int getGlowWidth() {
        return (int) Math.ceil(mGlowWidthRatio * ((float) getWidth()));
    }

    private int getGlowHeight() {
        return (int) Math.ceil(((float) getGlowWidth()) * getGlowImageAspectRatio());
    }

    public void setGlowWidthRatio(float f) {
        if (mGlowWidthRatio != f) {
            mGlowWidthRatio = f;
            updateGlowSizes();
            distributeEvenly();
        }
    }

    public float getGlowWidthRatio() {
        return mGlowWidthRatio;
    }

    public void setGlowsBlendMode(PorterDuff.Mode mode) {
        mPaint.setXfermode(new PorterDuffXfermode(mode));
        for (ImageView mGlowImageView : mGlowImageViews) {
            mGlowImageView.setLayerPaint(mPaint);
        }
    }

    public void clearCaches() {
        for (ImageView mGlowImageView : mGlowImageViews) {
            mGlowImageView.setImageDrawable(null);
        }
        mBlurProvider.clearCaches();
    }
}

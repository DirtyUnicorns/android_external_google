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
import com.android.systemui.C1734R$drawable;
import com.android.systemui.C1735R$id;
import com.android.systemui.assist.p003ui.EdgeLight;
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
        this.mGlowImageCropRegion = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        this.mGlowImageMatrix = new Matrix();
        this.mBlurRadius = 0;
        this.mPaint = new Paint();
        this.mBlurProvider = new BlurProvider(context, context.getResources().getDrawable(C1734R$drawable.glow_vector, null));
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        this.mBlueGlow = (ImageView) findViewById(C1735R$id.blue_glow);
        this.mRedGlow = (ImageView) findViewById(C1735R$id.red_glow);
        this.mYellowGlow = (ImageView) findViewById(C1735R$id.yellow_glow);
        this.mGreenGlow = (ImageView) findViewById(C1735R$id.green_glow);
        this.mGlowImageViews = new ArrayList<>(Arrays.asList(this.mBlueGlow, this.mRedGlow, this.mYellowGlow, this.mGreenGlow));
    }

    public void setVisibility(int i) {
        int visibility = getVisibility();
        if (visibility != i) {
            super.setVisibility(i);
            if (visibility == 8) {
                setBlurredImageOnViews(this.mBlurRadius);
            }
        }
    }

    public int getBlurRadius() {
        return this.mBlurRadius;
    }

    public void setBlurRadius(int i) {
        if (this.mBlurRadius != i) {
            setBlurredImageOnViews(i);
        }
    }

    private void setBlurredImageOnViews(int i) {
        this.mBlurRadius = i;
        BlurProvider.BlurResult blurResult = this.mBlurProvider.get(this.mBlurRadius);
        this.mGlowImageCropRegion = blurResult.cropRegion;
        updateGlowImageMatrix();
        this.mGlowImageViews.forEach(new Consumer(blurResult) {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$GlowView$O7UE3OJn3jgHUZXGo46cwtdzlU */
            private final /* synthetic */ BlurProvider.BlurResult f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                GlowView.this.lambda$setBlurredImageOnViews$0$GlowView(this.f$1, (ImageView) obj);
            }
        });
    }

    public /* synthetic */ void lambda$setBlurredImageOnViews$0$GlowView(BlurProvider.BlurResult blurResult, ImageView imageView) {
        imageView.setImageDrawable(blurResult.drawable.getConstantState().newDrawable().mutate());
        imageView.setImageMatrix(this.mGlowImageMatrix);
    }

    private void updateGlowImageMatrix() {
        this.mGlowImageMatrix.setRectToRect(this.mGlowImageCropRegion, new RectF(0.0f, 0.0f, (float) getGlowWidth(), (float) getGlowHeight()), Matrix.ScaleToFit.FILL);
    }

    public void setGlowsY(int i, int i2, EdgeLight[] edgeLightArr) {
        this.mTranslationY = i;
        this.mMinimumHeightPx = i2;
        this.mEdgeLights = edgeLightArr;
        int i3 = 0;
        if (edgeLightArr == null || edgeLightArr.length != 4) {
            while (i3 < 4) {
                this.mGlowImageViews.get(i3).setTranslationY((float) (getHeight() - i));
                i3++;
            }
            return;
        }
        int i4 = (i - i2) * 4;
        float length = edgeLightArr[0].getLength() + edgeLightArr[1].getLength() + edgeLightArr[2].getLength() + edgeLightArr[3].getLength();
        while (i3 < 4) {
            this.mGlowImageViews.get(i3).setTranslationY((float) (getHeight() - (((int) ((edgeLightArr[i3].getLength() * ((float) i4)) / length)) + i2)));
            i3++;
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        post(new Runnable() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$GlowView$RsiuHu2LuX9WTQjnKF5U72DDI3M */

            public final void run() {
                GlowView.this.lambda$onSizeChanged$2$GlowView();
            }
        });
    }

    public /* synthetic */ void lambda$onSizeChanged$2$GlowView() {
        updateGlowSizes();
        post(new Runnable() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$GlowView$qgRYecX8Ze03PaEEB_cXpBmvLgg */

            public final void run() {
                GlowView.this.lambda$onSizeChanged$1$GlowView();
            }
        });
    }

    public /* synthetic */ void lambda$onSizeChanged$1$GlowView() {
        setGlowsY(this.mTranslationY, this.mMinimumHeightPx, this.mEdgeLights);
    }

    public void distributeEvenly() {
        float width = (float) getWidth();
        float f = this.mGlowWidthRatio / 2.0f;
        float f2 = 0.96f * f;
        float cornerRadiusBottom = (((float) DisplayUtils.getCornerRadiusBottom(super.mContext)) / width) - f;
        float f3 = cornerRadiusBottom + f2;
        float f4 = f3 + f2;
        this.mBlueGlow.setX(cornerRadiusBottom * width);
        this.mRedGlow.setX(f3 * width);
        this.mYellowGlow.setX(f4 * width);
        this.mGreenGlow.setX(width * (f2 + f4));
    }

    private void updateGlowSizes() {
        int glowWidth = getGlowWidth();
        int glowHeight = getGlowHeight();
        updateGlowImageMatrix();
        Iterator<ImageView> it = this.mGlowImageViews.iterator();
        while (it.hasNext()) {
            ImageView next = it.next();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) next.getLayoutParams();
            layoutParams.width = glowWidth;
            layoutParams.height = glowHeight;
            next.setLayoutParams(layoutParams);
            next.setImageMatrix(this.mGlowImageMatrix);
        }
        distributeEvenly();
    }

    private float getGlowImageAspectRatio() {
        if (this.mGlowImageCropRegion.width() == 0.0f) {
            return 0.0f;
        }
        return this.mGlowImageCropRegion.height() / this.mGlowImageCropRegion.width();
    }

    private int getGlowWidth() {
        return (int) Math.ceil((double) (this.mGlowWidthRatio * ((float) getWidth())));
    }

    private int getGlowHeight() {
        return (int) Math.ceil((double) (((float) getGlowWidth()) * getGlowImageAspectRatio()));
    }

    public void setGlowWidthRatio(float f) {
        if (this.mGlowWidthRatio != f) {
            this.mGlowWidthRatio = f;
            updateGlowSizes();
            distributeEvenly();
        }
    }

    public float getGlowWidthRatio() {
        return this.mGlowWidthRatio;
    }

    public void setGlowsBlendMode(PorterDuff.Mode mode) {
        this.mPaint.setXfermode(new PorterDuffXfermode(mode));
        Iterator<ImageView> it = this.mGlowImageViews.iterator();
        while (it.hasNext()) {
            it.next().setLayerPaint(this.mPaint);
        }
    }

    public void clearCaches() {
        Iterator<ImageView> it = this.mGlowImageViews.iterator();
        while (it.hasNext()) {
            it.next().setImageDrawable(null);
        }
        this.mBlurProvider.clearCaches();
    }
}

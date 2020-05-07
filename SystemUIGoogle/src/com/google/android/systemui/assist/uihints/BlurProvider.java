package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.MathUtils;

public final class BlurProvider {
    private final BlurKernel mBlurKernel;
    private Bitmap mBuffer;
    private final SourceDownsampler mImageSource;
    private final Resources mResources;

    private static int getNumDownsampleStepsForEffectiveRadius(float f) {
        int i = 0;
        for (int i2 = 25; ((float) i2) < f; i2 *= 2) {
            i++;
        }
        return i;
    }

    public static class BlurResult {
        public final RectF cropRegion;
        public final Drawable drawable;

        BlurResult(Drawable drawable2, RectF rectF) {
            drawable = drawable2;
            cropRegion = rectF;
        }

        BlurResult(BlurProvider blurProvider, Drawable drawable2) {
            this(drawable2, new RectF(0.0f, 0.0f, (float) drawable2.getIntrinsicWidth(), (float) drawable2.getIntrinsicHeight()));
        }
    }

    public BlurProvider(Context context, Drawable drawable) {
        mResources = context.getResources();
        mImageSource = new SourceDownsampler(drawable);
        mBlurKernel = new BlurKernel(context);
    }

    public void clearCaches() {
        mBuffer = null;
        mBlurKernel.clearCaches();
    }

    public BlurResult get(int i) {
        if (mBuffer == null) {
            mBuffer = mImageSource.createBitmapBuffer(0);
        }
        return blur((float) i, mBuffer, mBuffer);
    }

    private BlurResult blur(float f, Bitmap bitmap, Bitmap bitmap2) {
        float constrain = MathUtils.constrain(f, 0.0f, 1000.0f);
        if (constrain <= 1.0f) {
            return new BlurResult(this, mImageSource.getDrawable());
        }
        int numDownsampleStepsForEffectiveRadius = getNumDownsampleStepsForEffectiveRadius(constrain);
        mImageSource.rasterize(bitmap, numDownsampleStepsForEffectiveRadius);
        mBlurKernel.blur(bitmap, bitmap2, constrain * SourceDownsampler.getDownsampleScaleFactor(numDownsampleStepsForEffectiveRadius));
        return new BlurResult(new BitmapDrawable(mResources, bitmap2), getBitmapVisibleRegion(bitmap2, mImageSource.getDownsampledWidth(numDownsampleStepsForEffectiveRadius), mImageSource.getDownsampledHeight(numDownsampleStepsForEffectiveRadius)));
    }

    private static RectF getBitmapVisibleRegion(Bitmap bitmap, int i, int i2) {
        int i3 = 0;
        int i4 = 0;
        loop0:
        while (true) {
            if (i4 >= i) {
                i4 = 0;
                break;
            }
            for (int i5 = 0; i5 < i2; i5++) {
                if (bitmap.getPixel(i4, i5) != 0) {
                    break loop0;
                }
            }
            i4++;
        }
        int i6 = 0;
        loop2:
        while (true) {
            if (i6 >= i2) {
                break;
            }
            for (int i7 = i4; i7 < i; i7++) {
                if (bitmap.getPixel(i7, i6) != 0) {
                    i3 = i6;
                    break loop2;
                }
            }
            i6++;
        }
        int i8 = i - 1;
        loop4:
        while (true) {
            if (i8 < i4) {
                break;
            }
            for (int i9 = i2 - 1; i9 >= i3; i9--) {
                if (bitmap.getPixel(i8, i9) != 0) {
                    i = i8;
                    break loop4;
                }
            }
            i8--;
        }
        int i10 = i2 - 1;
        loop6:
        while (true) {
            if (i10 < i3) {
                break;
            }
            for (int i11 = i - 1; i11 >= i4; i11--) {
                if (bitmap.getPixel(i11, i10) != 0) {
                    i2 = i10;
                    break loop6;
                }
            }
            i10--;
        }
        return new RectF((float) i4, (float) i3, (float) i, (float) i2);
    }

    private static class SourceDownsampler {
        private final Drawable mDrawable;

        public static float getDownsampleScaleFactor(int i) {
            return 1.0f / ((float) (1 << i));
        }

        public SourceDownsampler(Drawable drawable) {
            mDrawable = drawable;
        }

        public Drawable getDrawable() {
            return mDrawable;
        }

        public void rasterize(Bitmap bitmap, int i) {
            Canvas canvas = new Canvas(bitmap);
            canvas.clipRect(0, 0, getDownsampledWidth(i) + 25, getDownsampledHeight(i) + 25);
            canvas.drawColor(0, BlendMode.CLEAR);
            float f = (float) 25;
            canvas.translate(f, f);
            float downsampleScaleFactor = getDownsampleScaleFactor(i);
            canvas.scale(downsampleScaleFactor, downsampleScaleFactor);
            getDrawable().setBounds(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            getDrawable().draw(canvas);
        }

        public Bitmap createBitmapBuffer(int i) {
            return Bitmap.createBitmap(getDownsampledWidth(i), getDownsampledHeight(i), Bitmap.Config.ARGB_8888);
        }

        public int getDownsampledWidth(int i) {
            return getSideLength((float) getDrawable().getIntrinsicWidth(), i);
        }

        public int getDownsampledHeight(int i) {
            return getSideLength((float) getDrawable().getIntrinsicHeight(), i);
        }

        private static int getSideLength(float f, int i) {
            return (int) Math.ceil((f * getDownsampleScaleFactor(i)) + 50.0f);
        }
    }

    private static class BlurKernel {
        private final RenderScript mBlurRenderScript;
        private final ScriptIntrinsicBlur mBlurScript;
        private Allocation mLastInputAllocation;
        private Bitmap mLastInputBitmap;
        private Allocation mLastOutputAllocation;
        private Bitmap mLastOutputBitmap;

        public BlurKernel(Context context) {
            mBlurRenderScript = RenderScript.create(context);
            RenderScript renderScript = mBlurRenderScript;
            mBlurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        }

        public void blur(Bitmap bitmap, Bitmap bitmap2, float f) {
            prepareInputAllocation(bitmap);
            prepareOutputAllocation(bitmap2);
            mBlurScript.setRadius(MathUtils.constrain(f, 0.0f, 25.0f));
            mBlurScript.setInput(mLastInputAllocation);
            mBlurScript.forEach(mLastOutputAllocation);
            mLastOutputAllocation.copyTo(bitmap2);
        }

        public void clearCaches() {
            prepareInputAllocation(null);
            prepareOutputAllocation(null);
        }

        private void prepareInputAllocation(Bitmap bitmap) {
            boolean canShareAllocations = canShareAllocations(mLastInputBitmap, bitmap);
            mLastInputBitmap = bitmap;
            if (canShareAllocations) {
                mLastInputAllocation.copyFrom(bitmap);
                return;
            }
            if (mLastInputAllocation != null) {
                mLastInputAllocation.destroy();
                mLastInputAllocation = null;
            }
            if (mLastInputBitmap != null) {
                mLastInputAllocation = createAllocationForBitmap(mLastInputBitmap);
            }
        }

        private void prepareOutputAllocation(Bitmap bitmap) {
            if (mLastOutputAllocation != null && !canShareAllocations(mLastOutputBitmap, bitmap)) {
                mLastOutputAllocation.destroy();
                mLastOutputAllocation = null;
            }
            mLastOutputBitmap = bitmap;
            if (mLastOutputBitmap != null && mLastOutputAllocation == null) {
                mLastOutputAllocation = createAllocationForBitmap(mLastOutputBitmap);
            }
        }

        private Allocation createAllocationForBitmap(Bitmap bitmap) {
            return Allocation.createFromBitmap(mBlurRenderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE, 1);
        }

        private static boolean canShareAllocations(Bitmap bitmap, Bitmap bitmap2) {
            return bitmap != null && bitmap2 != null && bitmap.getWidth() == bitmap2.getWidth() && bitmap.getHeight() == bitmap2.getHeight();
        }
    }
}

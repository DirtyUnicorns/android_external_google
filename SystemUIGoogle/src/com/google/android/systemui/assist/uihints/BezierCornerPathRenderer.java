package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.graphics.Path;
import com.android.systemui.assist.ui.CornerPathRenderer;

public final class BezierCornerPathRenderer extends CornerPathRenderer {
    private final int mControlPointBottom;
    private final int mControlPointTop;
    private final int mCornerRadiusBottom;
    private final int mCornerRadiusTop;
    private final int mHeight;
    private final Path mPath = new Path();
    private final int mWidth;

    // TODO: Find a name for this variable
    private final int[] var = new int[CornerPathRenderer.Corner.values().length];

    public BezierCornerPathRenderer(Context context) {
        this.mCornerRadiusBottom = DisplayUtils.getCornerRadiusBottom(context);
        this.mCornerRadiusTop = DisplayUtils.getCornerRadiusTop(context);
        this.mHeight = DisplayUtils.getHeight(context);
        this.mWidth = DisplayUtils.getWidth(context);
        this.mControlPointBottom = (int) Math.floor((double) ((((float) this.mCornerRadiusBottom) * 3.6f) / 8.0f));
        this.mControlPointTop = (int) Math.floor((double) ((((float) this.mCornerRadiusTop) * 3.6f) / 8.0f));
    }

    public Path getCornerPath(CornerPathRenderer.Corner corner) {
        this.mPath.reset();
        int i = var[corner.ordinal()];
        if (i == 1) {
            this.mPath.moveTo(0.0f, (float) (this.mHeight - this.mCornerRadiusBottom));
            Path path = this.mPath;
            int i2 = this.mHeight;
            int i3 = this.mControlPointBottom;
            path.cubicTo(0.0f, (float) (i2 - i3), (float) i3, (float) i2, (float) this.mCornerRadiusBottom, (float) i2);
        } else if (i == 2) {
            this.mPath.moveTo((float) (this.mWidth - this.mCornerRadiusBottom), (float) this.mHeight);
            Path path2 = this.mPath;
            int i4 = this.mWidth;
            int i5 = this.mControlPointBottom;
            int i6 = this.mHeight;
            path2.cubicTo((float) (i4 - i5), (float) i6, (float) i4, (float) (i6 - i5), (float) i4, (float) (i6 - this.mCornerRadiusBottom));
        } else if (i == 3) {
            this.mPath.moveTo((float) this.mCornerRadiusTop, 0.0f);
            Path path3 = this.mPath;
            int i7 = this.mControlPointTop;
            path3.cubicTo((float) i7, 0.0f, 0.0f, (float) i7, 0.0f, (float) this.mCornerRadiusTop);
        } else if (i == 4) {
            this.mPath.moveTo((float) this.mWidth, (float) this.mCornerRadiusTop);
            Path path4 = this.mPath;
            int i8 = this.mWidth;
            int i9 = this.mControlPointTop;
            path4.cubicTo((float) i8, (float) i9, (float) (i8 - i9), 0.0f, (float) (i8 - this.mCornerRadiusTop), 0.0f);
        }
        return this.mPath;
    }
}

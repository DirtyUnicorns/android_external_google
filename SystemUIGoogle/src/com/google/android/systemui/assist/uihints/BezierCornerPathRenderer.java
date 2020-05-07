package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.graphics.Path;
import com.android.systemui.assist.ui.CornerPathRenderer;
import com.android.systemui.assist.ui.CornerPathRenderer.Corner;

public final class BezierCornerPathRenderer extends CornerPathRenderer {
    private final int mCornerRadiusBottom;
    private final int mCornerRadiusTop;
    private final int mControlPointBottom;
    private final int mControlPointTop;
    private final int mHeight;
    private final int mWidth;
    private final Path mPath;

    public BezierCornerPathRenderer(Context context) {
        mCornerRadiusBottom = DisplayUtils.getCornerRadiusBottom(context);
        mCornerRadiusTop = DisplayUtils.getCornerRadiusTop(context);
        mControlPointBottom = (int) Math.floor(mCornerRadiusBottom * 3.6f / 8.0f);
        mControlPointTop = (int) Math.floor(mCornerRadiusTop * 3.6f / 8.0f);
        mHeight = DisplayUtils.getHeight(context);
        mWidth = DisplayUtils.getWidth(context);
        mPath = new Path();
    }

    public Path getCornerPath(Corner corner) {
        mPath.reset();
        switch (corner) {
            case BOTTOM_LEFT:
                mPath.moveTo(0, mHeight - mCornerRadiusBottom);
                mPath.cubicTo(0, mHeight - mControlPointBottom, mControlPointBottom, mHeight, mCornerRadiusBottom, mHeight);
                break;
            case BOTTOM_RIGHT:
                mPath.moveTo(mWidth - mCornerRadiusBottom, mHeight);
                mPath.cubicTo(mWidth - mControlPointBottom, mHeight, mWidth, mHeight - mControlPointBottom, mWidth, mHeight - mCornerRadiusBottom);
                break;
            case TOP_LEFT:
                mPath.moveTo(mCornerRadiusTop, 0);
                mPath.cubicTo(mControlPointTop, 0, 0, mControlPointTop, 0, mCornerRadiusTop);
                break;
            case TOP_RIGHT:
                mPath.moveTo(mWidth, mCornerRadiusTop);
                mPath.cubicTo(mWidth, mControlPointTop, mWidth - mControlPointTop, 0, mWidth - mCornerRadiusTop, 0);
                break;
        }
        return mPath;
    }
}

package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {
    public static int convertDpToPx(float f, Context context) {
        Display defaultDisplay = getDefaultDisplay(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        return (int) Math.ceil((double) (f * displayMetrics.density));
    }

    public static int convertSpToPx(float f, Context context) {
        return (int) TypedValue.applyDimension(2, f, context.getResources().getDisplayMetrics());
    }

    public static int getWidth(Context context) {
        Display defaultDisplay = getDefaultDisplay(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        int rotation = defaultDisplay.getRotation();
        if (rotation == 0 || rotation == 2) {
            return displayMetrics.widthPixels;
        }
        return displayMetrics.heightPixels;
    }

    public static int getHeight(Context context) {
        Display defaultDisplay = getDefaultDisplay(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        int rotation = defaultDisplay.getRotation();
        if (rotation == 0 || rotation == 2) {
            return displayMetrics.heightPixels;
        }
        return displayMetrics.widthPixels;
    }

    public static int getRotatedHeight(Context context) {
        Display defaultDisplay = getDefaultDisplay(context);
        Point point = new Point();
        defaultDisplay.getRealSize(point);
        return point.y;
    }

    public static int getRotatedWidth(Context context) {
        Display defaultDisplay = getDefaultDisplay(context);
        Point point = new Point();
        defaultDisplay.getRealSize(point);
        return point.x;
    }

    public static int getCornerRadiusBottom(Context context) {
        int identifier = context.getResources().getIdentifier("rounded_corner_radius_bottom", "dimen", "android");
        int dimensionPixelSize = identifier > 0 ? context.getResources().getDimensionPixelSize(identifier) : 0;
        return dimensionPixelSize == 0 ? getCornerRadiusDefault(context) : dimensionPixelSize;
    }

    public static int getCornerRadiusTop(Context context) {
        int identifier = context.getResources().getIdentifier("rounded_corner_radius_top", "dimen", "android");
        int dimensionPixelSize = identifier > 0 ? context.getResources().getDimensionPixelSize(identifier) : 0;
        return dimensionPixelSize == 0 ? getCornerRadiusDefault(context) : dimensionPixelSize;
    }

    private static int getCornerRadiusDefault(Context context) {
        int identifier = context.getResources().getIdentifier("rounded_corner_radius", "dimen", "android");
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
    }
}

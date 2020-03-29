package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.graphics.Path;
import com.android.systemui.assist.p003ui.CornerPathRenderer;

public final class BezierCornerPathRenderer extends CornerPathRenderer {
    private final int mControlPointBottom;
    private final int mControlPointTop;
    private final int mCornerRadiusBottom;
    private final int mCornerRadiusTop;
    private final int mHeight;
    private final Path mPath = new Path();
    private final int mWidth;

    public BezierCornerPathRenderer(Context context) {
        this.mCornerRadiusBottom = DisplayUtils.getCornerRadiusBottom(context);
        this.mCornerRadiusTop = DisplayUtils.getCornerRadiusTop(context);
        this.mHeight = DisplayUtils.getHeight(context);
        this.mWidth = DisplayUtils.getWidth(context);
        this.mControlPointBottom = (int) Math.floor((double) ((((float) this.mCornerRadiusBottom) * 3.6f) / 8.0f));
        this.mControlPointTop = (int) Math.floor((double) ((((float) this.mCornerRadiusTop) * 3.6f) / 8.0f));
    }

    /* renamed from: com.google.android.systemui.assist.uihints.BezierCornerPathRenderer$1 */
    static /* synthetic */ class C15611 {

        /* renamed from: $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner */
        static final /* synthetic */ int[] f79x853e7b3e = new int[CornerPathRenderer.Corner.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(10:0|1|2|3|4|5|6|7|8|10) */
        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        static {
            /*
                com.android.systemui.assist.ui.CornerPathRenderer$Corner[] r0 = com.android.systemui.assist.p003ui.CornerPathRenderer.Corner.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                com.google.android.systemui.assist.uihints.BezierCornerPathRenderer.C15611.f79x853e7b3e = r0
                int[] r0 = com.google.android.systemui.assist.uihints.BezierCornerPathRenderer.C15611.f79x853e7b3e     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.p003ui.CornerPathRenderer.Corner.BOTTOM_LEFT     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = com.google.android.systemui.assist.uihints.BezierCornerPathRenderer.C15611.f79x853e7b3e     // Catch:{ NoSuchFieldError -> 0x001f }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.p003ui.CornerPathRenderer.Corner.BOTTOM_RIGHT     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = com.google.android.systemui.assist.uihints.BezierCornerPathRenderer.C15611.f79x853e7b3e     // Catch:{ NoSuchFieldError -> 0x002a }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.p003ui.CornerPathRenderer.Corner.TOP_LEFT     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = com.google.android.systemui.assist.uihints.BezierCornerPathRenderer.C15611.f79x853e7b3e     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.p003ui.CornerPathRenderer.Corner.TOP_RIGHT     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.systemui.assist.uihints.BezierCornerPathRenderer.C15611.<clinit>():void");
        }
    }

    public Path getCornerPath(CornerPathRenderer.Corner corner) {
        this.mPath.reset();
        int i = C15611.f79x853e7b3e[corner.ordinal()];
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

package com.google.android.systemui.assist.uihints.edgelights;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.assist.ui.EdgeLight;
import com.android.systemui.assist.ui.PerimeterPathGuide;
import com.google.android.systemui.assist.uihints.BezierCornerPathRenderer;
import com.google.android.systemui.assist.uihints.DisplayUtils;
import com.google.android.systemui.assist.uihints.edgelights.mode.FulfillBottom;
import com.google.android.systemui.assist.uihints.edgelights.mode.FulfillPerimeter;
import com.google.android.systemui.assist.uihints.edgelights.mode.FullListening;
import com.google.android.systemui.assist.uihints.edgelights.mode.Gone;
import com.google.android.systemui.assist.uihints.edgelights.mode.HalfListening;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class EdgeLightsView extends View {
    private List<EdgeLight> mAssistInvocationLights;
    private EdgeLight[] mAssistLights;
    private Set<EdgeLightsListener> mListeners;
    private Mode mMode;
    private final Paint mPaint;
    private final Path mPath;
    private final PerimeterPathGuide mPerimeterPathGuide;
    private int[] mScreenLocation;

    public EdgeLightsView(Context context) {
        this(context, null);
    }

    public EdgeLightsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public EdgeLightsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public EdgeLightsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        mPaint = new Paint();
        mAssistLights = new EdgeLight[0];
        mAssistInvocationLights = new ArrayList<EdgeLight>();
        mPath = new Path();
        mListeners = new HashSet<>();
        mScreenLocation = new int[2];
        int convertDpToPx = DisplayUtils.convertDpToPx(3.0f, context);
        mPaint.setStrokeWidth((float) convertDpToPx);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setAntiAlias(true);
        mPerimeterPathGuide = new PerimeterPathGuide(context, new BezierCornerPathRenderer(context),
                convertDpToPx / 2, DisplayUtils.getWidth(context), DisplayUtils.getHeight(context));
        mMode = new Gone();
        commitModeTransition(mMode);
        Resources resources = getResources();
        mAssistInvocationLights.add(new EdgeLight(resources.getColor(R.color.edge_light_blue), 0.0f, 0.0f));
        mAssistInvocationLights.add(new EdgeLight(resources.getColor(R.color.edge_light_red), 0.0f, 0.0f));
        mAssistInvocationLights.add(new EdgeLight(resources.getColor(R.color.edge_light_yellow), 0.0f, 0.0f));
        mAssistInvocationLights.add(new EdgeLight(resources.getColor(R.color.edge_light_green), 0.0f, 0.0f));
    }

    protected void addListener(EdgeLightsListener edgeLightsListener) {
        mListeners.add(edgeLightsListener);
    }

    public EdgeLight[] getAssistLights() {
        if (Looper.getMainLooper().isCurrentThread()) {
            return mAssistLights;
        }
        throw new IllegalStateException("Must be called from main thread");
    }

    public List<EdgeLight> getAssistInvocationLights() {
        return mAssistInvocationLights;
    }

    public void setAssistLights(EdgeLight[] edgeLightArr) {
        post(() -> lambda$setAssistLights$1$EdgeLightsView(edgeLightArr));
    }

    private /* synthetic */ void lambda$setAssistLights$1$EdgeLightsView(EdgeLight[] edgeLightArr) {
        mAssistLights = EdgeLight.copy(edgeLightArr);
        mListeners.forEach(listener -> lambda$setAssistLights$0$EdgeLightsView(listener));
        invalidate();
    }

    private /* synthetic */ void lambda$setAssistLights$0$EdgeLightsView(EdgeLightsListener edgeLightsListener) {
        edgeLightsListener.onAssistLightsUpdated(mMode, mAssistLights);
    }

    public Mode getMode() {
        return mMode;
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        if (getVisibility() == 8) {
            updateRotation();
        }
    }

    public void commitModeTransition(Mode mode) {
        mode.start(this, mPerimeterPathGuide, mMode);
        mMode = mode;
        mListeners.forEach((Consumer) obj -> lambda$commitModeTransition$2$EdgeLightsView((EdgeLightsListener) obj));
        mAssistInvocationLights.forEach(EdgeLightsViewLambda.INSTANCE);
        invalidate();
    }

    private /* synthetic */ void lambda$commitModeTransition$2$EdgeLightsView(EdgeLightsListener edgeLightsListener) {
        edgeLightsListener.onModeStarted(mMode);
    }

    public void onAudioLevelUpdate(float f, float f2) {
        mMode.onAudioLevelUpdate(f, f2);
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateRotation();
        mMode.onConfigurationChanged();
    }

    @Override
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateRotation();
    }

    private void updateRotation() {
        mPerimeterPathGuide.setRotation(getContext().getDisplay().getRotation());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getLocationOnScreen(mScreenLocation);
        int[] iArr = mScreenLocation;
        canvas.translate((float) (-iArr[0]), (float) (-iArr[1]));
        renderLights(canvas, mAssistLights);
        renderLights(canvas, mAssistInvocationLights);
    }

    private void renderLights(Canvas canvas, List<EdgeLight> list) {
        if (!list.isEmpty()) {
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            renderLight(canvas, list.get(0));
            if (list.size() > 1) {
                renderLight(canvas, list.get(list.size() - 1));
            }
            if (list.size() > 2) {
                mPaint.setStrokeCap(Paint.Cap.BUTT);
                for (EdgeLight renderLight : list.subList(1, list.size() - 1)) {
                    renderLight(canvas, renderLight);
                }
            }
        }
    }

    private void renderLights(Canvas canvas, EdgeLight[] edgeLightArr) {
        if (edgeLightArr.length != 0) {
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            renderLight(canvas, edgeLightArr[0]);
            if (edgeLightArr.length > 1) {
                renderLight(canvas, edgeLightArr[edgeLightArr.length - 1]);
            }
            if (edgeLightArr.length > 2) {
                mPaint.setStrokeCap(Paint.Cap.BUTT);
                for (int i = 1; i < edgeLightArr.length - 1; i++) {
                    renderLight(canvas, edgeLightArr[i]);
                }
            }
        }
    }

    private void renderLight(Canvas canvas, EdgeLight edgeLight) {
        mPerimeterPathGuide.strokeSegment(mPath, edgeLight.getOffset(), edgeLight.getOffset() + edgeLight.getLength());
        mPaint.setColor(edgeLight.getColor());
        canvas.drawPath(mPath, mPaint);
    }

    public interface Mode {

        static Mode fromBundle(Bundle bundle, Context context) {
            String string = bundle.getString("state", "");
            if (string == null) {
                Log.e("EdgeLightsView", "Cannot construct mode, returning null");
                return null;
            }
            char c = 65535;
            switch (string.hashCode()) {
                case -1911007510:
                    if (string.equals("FULFILL_BOTTOM")) {
                        c = 2;
                        break;
                    }
                    break;
                case 2193567:
                    if (string.equals("GONE")) {
                        c = 4;
                        break;
                    }
                    break;
                case 429932431:
                    if (string.equals("HALF_LISTENING")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1387022046:
                    if (string.equals("FULFILL_PERIMETER")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1971150571:
                    if (string.equals("FULL_LISTENING")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                return new FullListening(context);
            }
            if (c == 1) {
                return new HalfListening(context, bundle.getLong("timeoutMillis", 20000));
            }
            if (c == 2) {
                return new FulfillBottom(context, bundle.getBoolean("listening", false));
            }
            if (c == 3) {
                return new FulfillPerimeter(context);
            }
            if (c == 4) {
                return new Gone();
            }
            Log.e("EdgeLightsView", "Cannot construct mode, returning null");
            return null;
        }

        int getSubType();

        default void logState() {
            MetricsLogger.action(new LogMaker(1716).setType(6).setSubtype(getSubType()));
        }

        void onAudioLevelUpdate(float f, float f2);

        void onConfigurationChanged();

        void onNewModeRequest(EdgeLightsView edgeLightsView, Mode mode);

        default boolean preventsInvocations() {
            return false;
        }

        void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, Mode mode);
    }
}

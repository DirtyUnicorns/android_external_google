package com.google.android.systemui.assist.uihints.edgelights;

import com.android.systemui.assist.ui.EdgeLight;
import java.util.function.Consumer;

public final /* synthetic */ class EdgeLightsViewLambda implements Consumer {
    public static final /* synthetic */ EdgeLightsViewLambda INSTANCE = new EdgeLightsViewLambda();

    private /* synthetic */ EdgeLightsViewLambda() {
    }

    public final void accept(Object obj) {
        ((EdgeLight) obj).setLength(0.0f);
    }
}

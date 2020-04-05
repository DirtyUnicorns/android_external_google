package com.google.android.systemui.assist.uihints.edgelights;

import com.android.systemui.assist.ui.EdgeLight;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;

public interface EdgeLightsListener {
    void onAssistLightsUpdated(EdgeLightsView.Mode mode, EdgeLight[] edgeLightArr);

    void onModeStarted(EdgeLightsView.Mode mode);
}

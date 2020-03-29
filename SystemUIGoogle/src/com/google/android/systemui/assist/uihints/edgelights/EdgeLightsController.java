package com.google.android.systemui.assist.uihints.edgelights;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;

public final class EdgeLightsController {
    private final Context mContext;
    private final EdgeLightsView mEdgeLightsView;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public EdgeLightsController(Context context, ViewGroup viewGroup) {
        this.mContext = context;
        this.mEdgeLightsView = (EdgeLightsView) LayoutInflater.from(context).inflate(R.layout.edge_lights_view, viewGroup, false);
        viewGroup.addView(this.mEdgeLightsView);
    }

    public void onAudioLevelUpdate(float f, float f2) {
        this.mEdgeLightsView.onAudioLevelUpdate(f, f2);
    }

    public void setState(Bundle bundle) {
        EdgeLightsView.Mode fromBundle = EdgeLightsView.Mode.fromBundle(bundle, this.mContext);
        if (fromBundle == null) {
            Log.e("EdgeLightsController", String.format("Invalid mode from bundle %s", bundle));
            return;
        }
        setState(fromBundle);
        fromBundle.logState();
    }

    public void setState(EdgeLightsView.Mode mode) {
        getMode().onNewModeRequest(this.mEdgeLightsView, mode);
    }

    public void addListener(EdgeLightsListener edgeLightsListener) {
        this.mEdgeLightsView.addListener(edgeLightsListener);
    }

    public EdgeLightsView.Mode getMode() {
        return this.mEdgeLightsView.getMode();
    }
}

package com.google.android.systemui.assist.uihints.edgelights;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.systemui.R;

public final class EdgeLightsController {
    private final Context mContext;
    private final EdgeLightsView mEdgeLightsView;

    public EdgeLightsController(Context context, ViewGroup viewGroup) {
        mContext = context;
        mEdgeLightsView = (EdgeLightsView) LayoutInflater.from(context).inflate(R.layout.edge_lights_view, viewGroup, false);
        viewGroup.addView(mEdgeLightsView);
    }

    public void onAudioLevelUpdate(float f, float f2) {
        mEdgeLightsView.onAudioLevelUpdate(f, f2);
    }

    public void setState(Bundle bundle) {
        EdgeLightsView.Mode fromBundle = EdgeLightsView.Mode.fromBundle(bundle, mContext);
        if (fromBundle == null) {
            Log.e("EdgeLightsController", String.format("Invalid mode from bundle %s", bundle));
            return;
        }
        setState(fromBundle);
        fromBundle.logState();
    }

    public void setState(EdgeLightsView.Mode mode) {
        getMode().onNewModeRequest(mEdgeLightsView, mode);
    }

    public void addListener(EdgeLightsListener edgeLightsListener) {
        mEdgeLightsView.addListener(edgeLightsListener);
    }

    public EdgeLightsView.Mode getMode() {
        return mEdgeLightsView.getMode();
    }
}

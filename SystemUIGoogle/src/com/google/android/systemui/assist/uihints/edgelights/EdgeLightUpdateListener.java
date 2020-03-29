package com.google.android.systemui.assist.uihints.edgelights;

import android.animation.ValueAnimator;
import com.android.systemui.assist.ui.EdgeLight;

public final class EdgeLightUpdateListener implements ValueAnimator.AnimatorUpdateListener {
    private EdgeLight[] mFinalLights;
    private EdgeLight[] mInitialLights;
    private EdgeLight[] mLights;
    private EdgeLightsView mView;

    public EdgeLightUpdateListener(EdgeLight[] edgeLightArr, EdgeLight[] edgeLightArr2, EdgeLight[] edgeLightArr3, EdgeLightsView edgeLightsView) {
        if (edgeLightArr.length == edgeLightArr2.length && edgeLightArr3.length == edgeLightArr2.length) {
            this.mFinalLights = edgeLightArr2;
            this.mInitialLights = edgeLightArr;
            this.mLights = edgeLightArr3;
            this.mView = edgeLightsView;
            return;
        }
        throw new IllegalArgumentException("Lights arrays must be the same length");
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        int i = 0;
        while (true) {
            EdgeLight[] edgeLightArr = this.mLights;
            if (i < edgeLightArr.length) {
                float length = this.mInitialLights[i].getLength();
                this.mLights[i].setLength(((this.mFinalLights[i].getLength() - length) * animatedFraction) + length);
                float offset = this.mInitialLights[i].getOffset();
                this.mLights[i].setOffset(((this.mFinalLights[i].getOffset() - offset) * animatedFraction) + offset);
                i++;
            } else {
                this.mView.setAssistLights(edgeLightArr);
                return;
            }
        }
    }
}

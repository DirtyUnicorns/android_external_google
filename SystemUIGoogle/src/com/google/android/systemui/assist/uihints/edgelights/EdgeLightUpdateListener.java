package com.google.android.systemui.assist.uihints.edgelights;

import android.animation.ValueAnimator;
import com.android.systemui.assist.ui.EdgeLight;

public final class EdgeLightUpdateListener implements ValueAnimator.AnimatorUpdateListener {
    private EdgeLight[] mFinalLights;
    private EdgeLight[] mInitialLights;
    private EdgeLight[] mLights;
    private EdgeLightsView mView;

    public EdgeLightUpdateListener(EdgeLight[] edgeLightArr, EdgeLight[] edgeLightArr2,
            EdgeLight[] edgeLightArr3, EdgeLightsView edgeLightsView) {
        if (edgeLightArr.length == edgeLightArr2.length && edgeLightArr3.length == edgeLightArr2.length) {
            mFinalLights = edgeLightArr2;
            mInitialLights = edgeLightArr;
            mLights = edgeLightArr3;
            mView = edgeLightsView;
            return;
        }
        throw new IllegalArgumentException("Lights arrays must be the same length");
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        int i = 0;
        while (true) {
            EdgeLight[] edgeLightArr = mLights;
            if (i < edgeLightArr.length) {
                float length = mInitialLights[i].getLength();
                mLights[i].setLength(((mFinalLights[i].getLength() - length) * animatedFraction) + length);
                float offset = mInitialLights[i].getOffset();
                mLights[i].setOffset(((mFinalLights[i].getOffset() - offset) * animatedFraction) + offset);
                i++;
            } else {
                mView.setAssistLights(edgeLightArr);
                return;
            }
        }
    }
}

package com.google.android.systemui.assist.uihints.edgelights.mode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.util.MathUtils;
import android.view.animation.PathInterpolator;
import com.android.systemui.R;
import com.android.systemui.assist.ui.EdgeLight;
import com.android.systemui.assist.ui.PerimeterPathGuide;
import com.google.android.systemui.assist.uihints.RollingAverage;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightUpdateListener;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;

public final class FullListening implements EdgeLightsView.Mode {
    private static final PathInterpolator INTERPOLATOR;
    private Animator mAnimator;
    private EdgeLightsView mEdgeLightsView;
    private PerimeterPathGuide mGuide;
    private boolean mLastPerturbationWasEven;
    private long mLastSpeechTimestampMs;
    private final EdgeLight[] mLights;
    private RollingAverage mRollingConfidence;
    private State mState;

    static {
        INTERPOLATOR = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);
    }

    public FullListening(Context context) {
        mLastPerturbationWasEven = false;
        mLastSpeechTimestampMs = 0L;
        mRollingConfidence = new RollingAverage(3);
        mState = State.NOT_STARTED;
        mLights = new EdgeLight[]{new EdgeLight(context.getResources().getColor(R.color.edge_light_blue, null), 0.0f, 0.0f),
                new EdgeLight(context.getResources().getColor(R.color.edge_light_red, null), 0.0f, 0.0f),
                new EdgeLight(context.getResources().getColor(R.color.edge_light_yellow, null), 0.0f, 0.0f),
                new EdgeLight(context.getResources().getColor(R.color.edge_light_green, null), 0.0f, 0.0f)};
    }

    @Override
    public void start(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        mEdgeLightsView = edgeLightsView;
        mGuide = perimeterPathGuide;
        mState = State.EXPANDING_TO_WIDTH;
        edgeLightsView.setVisibility(0);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(getExpandToWidthDuration(edgeLightsView, mode));
        ofFloat.setInterpolator(INTERPOLATOR);
        ofFloat.addUpdateListener(new EdgeLightUpdateListener(getInitialLights(edgeLightsView, perimeterPathGuide, mode), getFinalLights(), mLights, edgeLightsView));
        ofFloat.addListener(createUpdateStateOnEndAnimatorListener());
        setAnimator(ofFloat);
    }

    @Override
    public void onNewModeRequest(EdgeLightsView edgeLightsView, EdgeLightsView.Mode mode) {
        if (!(mode instanceof FullListening)) {
            setAnimator(null);
            edgeLightsView.commitModeTransition(mode);
        }
    }

    private long getExpandToWidthDuration(EdgeLightsView edgeLightsView, EdgeLightsView.Mode mode) {
        if (mode instanceof FullListening) {
            return 0;
        }
        if (mode instanceof FulfillBottom) {
            return 300;
        }
        if (!edgeLightsView.getAssistInvocationLights().isEmpty()) {
            return 0;
        }
        return 500;
    }

    private EdgeLight[] getInitialLights(EdgeLightsView edgeLightsView, PerimeterPathGuide perimeterPathGuide, EdgeLightsView.Mode mode) {
        float f;
        EdgeLight[] copy = EdgeLight.copy(mLights);
        EdgeLight[] copy2 = edgeLightsView.getAssistLights() != null ? EdgeLight.copy(edgeLightsView.getAssistLights()) : null;
        boolean z = (mode instanceof FulfillBottom) && copy2 != null && copy.length == copy2.length;
        for (int i = 0; i < copy.length; i++) {
            EdgeLight edgeLight = copy[i];
            if (z) {
                f = copy2[i].getOffset();
            } else {
                f = perimeterPathGuide.getRegionCenter(PerimeterPathGuide.Region.BOTTOM);
            }
            edgeLight.setOffset(f);
            edgeLight.setLength(z ? copy2[i].getLength() : 0.0f);
        }
        return copy;
    }

    private EdgeLight[] getFinalLights() {
        EdgeLight[] copy = EdgeLight.copy(mLights);
        float regionWidth = mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM) / 4.0f;
        for (int i = 0; i < copy.length; i++) {
            copy[i].setOffset(((float) i) * regionWidth);
            copy[i].setLength(regionWidth);
        }
        return copy;
    }

    private void setAnimator(Animator animator) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = animator;
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    @Override
    public void onConfigurationChanged() {
        setAnimator(null);
        float f = 0.0f;
        for (EdgeLight length : mLights) {
            f += length.getLength();
        }
        float regionWidth = mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
        mLights[0].setOffset(0.0f);
        EdgeLight[] edgeLightArr = mLights;
        edgeLightArr[0].setLength((edgeLightArr[0].getLength() / f) * regionWidth);
        EdgeLight[] edgeLightArr2 = mLights;
        edgeLightArr2[1].setOffset(edgeLightArr2[0].getOffset() + mLights[0].getLength());
        EdgeLight[] edgeLightArr3 = mLights;
        edgeLightArr3[1].setLength((edgeLightArr3[1].getLength() / f) * regionWidth);
        EdgeLight[] edgeLightArr4 = mLights;
        edgeLightArr4[2].setOffset(edgeLightArr4[1].getOffset() + mLights[1].getLength());
        EdgeLight[] edgeLightArr5 = mLights;
        edgeLightArr5[2].setLength((edgeLightArr5[2].getLength() / f) * regionWidth);
        EdgeLight[] edgeLightArr6 = mLights;
        edgeLightArr6[3].setOffset(edgeLightArr6[2].getOffset() + mLights[2].getLength());
        EdgeLight[] edgeLightArr7 = mLights;
        edgeLightArr7[3].setLength((edgeLightArr7[3].getLength() / f) * regionWidth);
        updateStateAndAnimation();
    }

    private void updateStateAndAnimation() {
        EdgeLight[] edgeLightArr;
        int i;
        if (mRollingConfidence.getAverage() > 0.10000000149011612) {
            if (mState == State.LISTENING_TO_SPEECH && mAnimator != null) {
                return;
            }
            mState = State.LISTENING_TO_SPEECH;
            edgeLightArr = createPerturbedLights();
            i = (int)MathUtils.lerp(400.0f, 150.0f, (float)mRollingConfidence.getAverage());
        } else {
            if (mState != State.LISTENING_TO_SPEECH && mState != State.WAITING_FOR_ENDPOINTER) {
                if (mState == State.WAITING_FOR_SPEECH && mAnimator != null) {
                    return;
                }
                mState = State.WAITING_FOR_SPEECH;
                edgeLightArr = createPerturbedLights();
                i = 1200;
            } else {
                if (mAnimator != null) {
                    return;
                }
                mState = State.WAITING_FOR_ENDPOINTER;
                edgeLightArr = getFinalLights();
                i = 2000;
            }
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addListener(createUpdateStateOnEndAnimatorListener());
        ofFloat.setDuration(i);
        ofFloat.setInterpolator(INTERPOLATOR);
        ofFloat.addUpdateListener(new EdgeLightUpdateListener(EdgeLight.copy(mLights), edgeLightArr, mLights, mEdgeLightsView));
        setAnimator(ofFloat);
    }

    private EdgeLight[] createPerturbedLights() {
        float f;
        float regionWidth = mGuide.getRegionWidth(PerimeterPathGuide.Region.BOTTOM);
        if (mState == State.LISTENING_TO_SPEECH) {
            f = mLastPerturbationWasEven ? 0.39999998f : 0.6f;
        } else {
            f = mLastPerturbationWasEven ? 0.49f : 0.51f;
        }
        float f2 = f * regionWidth;
        float f3 = regionWidth / 2.0f;
        float lerp = MathUtils.lerp(Math.min(f3, f2), Math.max(f3, f2), (float) mRollingConfidence.getAverage());
        float f4 = regionWidth - lerp;
        mLastPerturbationWasEven = !mLastPerturbationWasEven;
        double d = mState == State.LISTENING_TO_SPEECH ? 0.6 : 0.52;
        double d2 = mState == State.LISTENING_TO_SPEECH ? 0.4 : 0.48;
        double d3 = d - d2;
        float random = ((float) ((Math.random() * d3) + d2)) * lerp;
        float random2 = ((float) ((Math.random() * d3) + d2)) * f4;
        float f5 = f4 - random2;
        EdgeLight[] copy = EdgeLight.copy(mLights);
        copy[0].setLength(random);
        copy[1].setLength(random2);
        copy[2].setLength(f5);
        copy[3].setLength(lerp - random);
        copy[0].setOffset(0.0f);
        copy[1].setOffset(random);
        float f6 = random + random2;
        copy[2].setOffset(f6);
        copy[3].setOffset(f6 + f5);
        return copy;
    }

    private AnimatorListenerAdapter createUpdateStateOnEndAnimatorListener() {
        return new AnimatorListenerAdapter() {
            private boolean mCancelled = false;
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                mCancelled = true;
            }
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (mAnimator == animator) {
                    mAnimator = null;
                }
                if (!mCancelled) {
                    updateStateAndAnimation();
                }
            }
        };
    }

    @Override
    public void onAudioLevelUpdate(float f, float f2) {
        mRollingConfidence.add(f);
        mLastSpeechTimestampMs = f > 0.1f ? SystemClock.uptimeMillis() : mLastSpeechTimestampMs;
        if (mState != State.EXPANDING_TO_WIDTH) {
            updateStateAndAnimation();
        }
    }

    private enum State {
        NOT_STARTED,
        EXPANDING_TO_WIDTH,
        WAITING_FOR_SPEECH,
        LISTENING_TO_SPEECH,
        WAITING_FOR_ENDPOINTER
    }

    @Override
    public int getSubType() {
        return 1;
    }

    @Override
    public boolean preventsInvocations() {
        return true;
    }
}

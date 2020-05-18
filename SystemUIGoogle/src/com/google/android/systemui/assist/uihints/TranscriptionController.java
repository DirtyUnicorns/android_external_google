package com.google.android.systemui.assist.uihints;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.google.android.systemui.assist.uihints.OverlayUiHost;
import com.google.android.systemui.assist.uihints.TranscriptionControllerLambda;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class TranscriptionController implements ConfigurationController.ConfigurationListener, OverlayUiHost.BottomMarginListener {
    private final Context mContext;
    private State mCurrentState;
    private final Runnable mDefaultOnTap;
    private boolean mHasAccurateBackground;
    private TranscriptionSpaceListener mListener;
    private final int mMargin;
    private PendingIntent mOnGreetingTap;
    private PendingIntent mOnTranscriptionTap;
    private ViewGroup mParent;
    private Runnable mQueuedCompletion;
    private State mQueuedState;
    private boolean mQueuedStateAnimates;
    private Map<State, TranscriptionSpaceView> mViewMap;

    public TranscriptionController(Context context, ViewGroup viewGroup, Runnable runnable) {
        mViewMap = new HashMap<>();
        mCurrentState = State.NONE;
        mHasAccurateBackground = false;
        mQueuedState = null;
        mQueuedStateAnimates = false;
        mContext = context;
        mParent = viewGroup;
        mDefaultOnTap = runnable;
        setUpViews();
        mMargin = mContext.getResources().getDimensionPixelSize(R.dimen.transcription_space_bottom_margin);
        Dependency.get(ConfigurationController.class).addCallback(this);
    }

    public void setListener(TranscriptionSpaceListener transcriptionSpaceListener) {
        mListener = transcriptionSpaceListener;
        if (transcriptionSpaceListener != null) {
            transcriptionSpaceListener.onStateChanged(null, mCurrentState);
        }
    }

    @Override
    public void onDensityOrFontScaleChanged() {
        setUpViews();
        for (TranscriptionSpaceView onFontSizeChanged : mViewMap.values()) {
            onFontSizeChanged.onFontSizeChanged();
        }
    }

    public void onBottomMarginChanged() {
        for (TranscriptionSpaceView transcriptionSpaceView : mViewMap.values()) {
            View view = (View) transcriptionSpaceView;
            ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin = mMargin;
            view.requestLayout();
        }
    }

    public void setHasDarkBackground(boolean z) {
        for (TranscriptionSpaceView hasDarkBackground : mViewMap.values()) {
            hasDarkBackground.setHasDarkBackground(z);
        }
    }

    public void setCardVisible(boolean z) {
        for (TranscriptionSpaceView cardVisible : mViewMap.values()) {
            cardVisible.setCardVisible(z);
        }
    }

    public void setTranscription(String str, PendingIntent pendingIntent) {
        mOnTranscriptionTap = pendingIntent;
        setState(State.TRANSCRIPTION, false, () -> ((TranscriptionView) Objects.requireNonNull(mViewMap.get(State.TRANSCRIPTION))).setTranscription(str));
    }

    public void setTranscriptionColor(int i) {
        ((TranscriptionView) Objects.requireNonNull(mViewMap.get(State.TRANSCRIPTION))).setTranscriptionColor(i);
    }

    public void setGreeting(String str, float f, PendingIntent pendingIntent) {
        if (!TextUtils.isEmpty(str)) {
            mOnGreetingTap = pendingIntent;
            setState(State.GREETING, false, () -> ((GreetingView) Objects.requireNonNull(mViewMap.get(State.GREETING))).setGreeting(str, f));
        }
    }

    public void setChips(ArrayList<Bundle> arrayList) {
        setState(State.CHIPS, false, () -> ((ChipsContainer) Objects.requireNonNull(mViewMap.get(State.CHIPS))).setChips(arrayList));
    }

    public void clear(boolean z) {
        setState(State.NONE, z, null);
    }

    public boolean isTranscribing() {
        return mCurrentState == State.TRANSCRIPTION;
    }

    public void setHasAccurateBackground(boolean z) {
        mHasAccurateBackground = z;
        if (mHasAccurateBackground) {
            if (mQueuedState != null) {
                setState(mQueuedState, mQueuedStateAnimates, mQueuedCompletion);
                mQueuedState = null;
            }
        }
    }

    public Rect getTouchableRegion() {
        TranscriptionSpaceView transcriptionSpaceView = mViewMap.get(mCurrentState);
        if (transcriptionSpaceView == null) {
            return null;
        }
        Rect rect = new Rect();
        transcriptionSpaceView.getBoundsOnScreen(rect);
        return rect;
    }

    private void setState(State state, boolean z, Runnable runnable) {
        if (mCurrentState == state) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }
        if (!mHasAccurateBackground && state != State.NONE) {
            mQueuedState = state;
            mQueuedStateAnimates = z;
            mQueuedCompletion = runnable;
            return;
        }
        mQueuedState = null;
        mQueuedCompletion = null;
        if (runnable != null) {
            runnable.run();
        }
        updateListener(mCurrentState, state);
        if (State.NONE.equals(mCurrentState)) {
            final TranscriptionSpaceView transcriptionSpaceView = mViewMap.get(state);
            if (transcriptionSpaceView != null) {
                transcriptionSpaceView.show();
            }
        } else {
            Futures.transform(mViewMap.get(mCurrentState).hide(z), new TranscriptionControllerLambda(this, state), MoreExecutors.directExecutor());
        }
        mCurrentState = state;
    }

    private void updateListener(State state, State state2) {
        if (mListener != null) {
            mListener.onStateChanged(state, state2);
        }
    }

    private void setUpViews() {
        mViewMap = new HashMap<>();
        TranscriptionView transcriptionView = (TranscriptionView) createView(R.layout.assist_transcription);
        transcriptionView.setOnClickListener(view -> {
            if (mOnTranscriptionTap == null) {
                mDefaultOnTap.run();
            } else {
                try {
                    mOnTranscriptionTap.send();
                } catch (PendingIntent.CanceledException unused) {
                    Log.e("TranscriptionController", "Transcription tap PendingIntent cancelled");
                    mDefaultOnTap.run();
                }
            }
        });
        mViewMap.put(State.TRANSCRIPTION, transcriptionView);
        GreetingView greetingView = (GreetingView) createView(R.layout.assist_greeting);
        greetingView.setOnClickListener(view -> {
            if (mOnGreetingTap == null) {
                mDefaultOnTap.run();
            } else {
                try {
                    mOnGreetingTap.send();
                } catch (PendingIntent.CanceledException unused) {
                    Log.e("TranscriptionController", "Greeting tap PendingIntent cancelled");
                    mDefaultOnTap.run();
                }
            }
        });
        mViewMap.put(State.GREETING, greetingView);
        mViewMap.put(State.CHIPS, (ChipsContainer) createView(R.layout.assist_chips_container));
    }

    private View createView(int i) {
        View view = LayoutInflater.from(mContext).inflate(i, mParent, false);
        view.setVisibility(8);
        mParent.addView(view);
        return view;
    }

    public /* synthetic */ Object maybeSetStateTranscriptionControllerLambda(State state, Void object) {
        if (state != State.NONE) {
            Objects.requireNonNull(mViewMap.get(state)).show();
        }
        return null;
    }

    public enum State {
        TRANSCRIPTION,
        GREETING,
        CHIPS,
        NONE
    }

    public interface TranscriptionSpaceListener {
        void onStateChanged(State state, State state2);
    }

    interface TranscriptionSpaceView {
        void getBoundsOnScreen(Rect rect);

        ListenableFuture<Void> hide(boolean z);

        void onFontSizeChanged();

        default void setCardVisible(boolean z) {
        }

        void setHasDarkBackground(boolean z);

        void show();
    }
}

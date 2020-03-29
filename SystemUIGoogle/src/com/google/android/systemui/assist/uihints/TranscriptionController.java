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
import com.google.android.systemui.assist.uihints.TranscriptionController;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TranscriptionController implements ConfigurationController.ConfigurationListener, OverlayUiHost.BottomMarginListener {
    private final Context mContext;
    private State mCurrentState = State.NONE;
    private final Runnable mDefaultOnTap;
    private boolean mHasAccurateBackground = false;
    private TranscriptionSpaceListener mListener;
    private final int mMargin;
    private PendingIntent mOnGreetingTap;
    private PendingIntent mOnTranscriptionTap;
    private ViewGroup mParent;
    private Runnable mQueuedCompletion;
    private State mQueuedState = null;
    private boolean mQueuedStateAnimates = false;
    private Map<State, TranscriptionSpaceView> mViewMap = new HashMap();

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

    public TranscriptionController(Context context, ViewGroup viewGroup, Runnable runnable) {
        this.mContext = context;
        this.mParent = viewGroup;
        this.mDefaultOnTap = runnable;
        setUpViews();
        this.mMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.transcription_space_bottom_margin);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    public void setListener(TranscriptionSpaceListener transcriptionSpaceListener) {
        this.mListener = transcriptionSpaceListener;
        TranscriptionSpaceListener transcriptionSpaceListener2 = this.mListener;
        if (transcriptionSpaceListener2 != null) {
            transcriptionSpaceListener2.onStateChanged(null, this.mCurrentState);
        }
    }

    public void onDensityOrFontScaleChanged() {
        for (TranscriptionSpaceView onFontSizeChanged : this.mViewMap.values()) {
            onFontSizeChanged.onFontSizeChanged();
        }
    }

    public void onBottomMarginChanged(int i) {
        Iterator<TranscriptionSpaceView> it = this.mViewMap.values().iterator();
        while (it.hasNext()) {
            View view = (View) it.next();
            ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin = this.mMargin + i;
            view.requestLayout();
        }
    }

    public void setHasDarkBackground(boolean z) {
        for (TranscriptionSpaceView hasDarkBackground : this.mViewMap.values()) {
            hasDarkBackground.setHasDarkBackground(z);
        }
    }

    public void setCardVisible(boolean z) {
        for (TranscriptionSpaceView cardVisible : this.mViewMap.values()) {
            cardVisible.setCardVisible(z);
        }
    }

    public void setTranscription(String str, PendingIntent pendingIntent) {
        this.mOnTranscriptionTap = pendingIntent;
        setState(State.TRANSCRIPTION, false, new Runnable(str) {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$TranscriptionController$DXAs0CvUnEDrhyv80sddGx80g */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TranscriptionController.this.lambda$setTranscription$0$TranscriptionController(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setTranscription$0$TranscriptionController(String str) {
        ((TranscriptionView) this.mViewMap.get(State.TRANSCRIPTION)).setTranscription(str);
    }

    public void setTranscriptionColor(int i) {
        ((TranscriptionView) this.mViewMap.get(State.TRANSCRIPTION)).setTranscriptionColor(i);
    }

    public void setGreeting(String str, float f, PendingIntent pendingIntent) {
        if (!TextUtils.isEmpty(str)) {
            this.mOnGreetingTap = pendingIntent;
            setState(State.GREETING, false, new Runnable(str, f) {
                /* class com.google.android.systemui.assist.uihints.$$Lambda$TranscriptionController$HvDmbk_uZWsUlrS9JbsN9siO3I */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ float f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    TranscriptionController.this.lambda$setGreeting$1$TranscriptionController(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$setGreeting$1$TranscriptionController(String str, float f) {
        ((GreetingView) this.mViewMap.get(State.GREETING)).setGreeting(str, f);
    }

    public void setChips(ArrayList<Bundle> arrayList) {
        setState(State.CHIPS, false, new Runnable(arrayList) {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$TranscriptionController$oja2NVT9eV8XDWGiLumFbN6mMqk */
            private final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TranscriptionController.this.lambda$setChips$2$TranscriptionController(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setChips$2$TranscriptionController(ArrayList arrayList) {
        ((ChipsContainer) this.mViewMap.get(State.CHIPS)).setChips(arrayList);
    }

    public void clear(boolean z) {
        setState(State.NONE, z, null);
    }

    public boolean isTranscribing() {
        return this.mCurrentState == State.TRANSCRIPTION;
    }

    public void setHasAccurateBackground(boolean z) {
        State state;
        this.mHasAccurateBackground = z;
        if (z && (state = this.mQueuedState) != null) {
            setState(state, this.mQueuedStateAnimates, this.mQueuedCompletion);
            this.mQueuedState = null;
        }
    }

    public Rect getTouchableRegion() {
        TranscriptionSpaceView transcriptionSpaceView = this.mViewMap.get(this.mCurrentState);
        if (transcriptionSpaceView == null) {
            return null;
        }
        Rect rect = new Rect();
        transcriptionSpaceView.getBoundsOnScreen(rect);
        return rect;
    }

    private void setState(State state, boolean z, Runnable runnable) {
        if (this.mCurrentState == state) {
            if (runnable != null) {
                runnable.run();
            }
        } else if (this.mHasAccurateBackground || state == State.NONE) {
            this.mQueuedState = null;
            this.mQueuedCompletion = null;
            if (runnable != null) {
                runnable.run();
            }
            updateListener(this.mCurrentState, state);
            if (State.NONE.equals(this.mCurrentState)) {
                TranscriptionSpaceView transcriptionSpaceView = this.mViewMap.get(state);
                if (transcriptionSpaceView != null) {
                    transcriptionSpaceView.show();
                }
            } else {
                Futures.transform(this.mViewMap.get(this.mCurrentState).hide(z), new Function(state) {
                    /* class com.google.android.systemui.assist.uihints.$$Lambda$TranscriptionController$E_ey_UuShBBb99Dal6Fat02GRw */
                    private final /* synthetic */ TranscriptionController.State f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final Object apply(Object obj) {
                        return TranscriptionController.this.lambda$setState$3$TranscriptionController(this.f$1, (Void) obj);
                    }
                }, MoreExecutors.directExecutor());
            }
            this.mCurrentState = state;
        } else {
            this.mQueuedState = state;
            this.mQueuedStateAnimates = z;
            this.mQueuedCompletion = runnable;
        }
    }

    public /* synthetic */ Object lambda$setState$3$TranscriptionController(State state, Void voidR) {
        if (state == State.NONE) {
            return null;
        }
        this.mViewMap.get(state).show();
        return null;
    }

    private void updateListener(State state, State state2) {
        TranscriptionSpaceListener transcriptionSpaceListener = this.mListener;
        if (transcriptionSpaceListener != null) {
            transcriptionSpaceListener.onStateChanged(state, state2);
        }
    }

    private void setUpViews() {
        this.mViewMap = new HashMap();
        TranscriptionView transcriptionView = (TranscriptionView) createView(R.layout.assist_transcription);
        transcriptionView.setOnClickListener(new View.OnClickListener() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$TranscriptionController$KKdRQ8kRnXrWPunS0MBIwBRXepo */

            public final void onClick(View view) {
                TranscriptionController.this.lambda$setUpViews$4$TranscriptionController(view);
            }
        });
        this.mViewMap.put(State.TRANSCRIPTION, transcriptionView);
        GreetingView greetingView = (GreetingView) createView(R.layout.assist_greeting);
        greetingView.setOnClickListener(new View.OnClickListener() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$TranscriptionController$DHMbRsTm4tBz9fnxjeGTYGOM9tg */

            public final void onClick(View view) {
                TranscriptionController.this.lambda$setUpViews$5$TranscriptionController(view);
            }
        });
        this.mViewMap.put(State.GREETING, greetingView);
        this.mViewMap.put(State.CHIPS, (ChipsContainer) createView(R.layout.assist_chips_container));
    }

    public /* synthetic */ void lambda$setUpViews$4$TranscriptionController(View view) {
        PendingIntent pendingIntent = this.mOnTranscriptionTap;
        if (pendingIntent == null) {
            this.mDefaultOnTap.run();
            return;
        }
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException unused) {
            Log.e("TranscriptionController", "Transcription tap PendingIntent cancelled");
            this.mDefaultOnTap.run();
        }
    }

    public /* synthetic */ void lambda$setUpViews$5$TranscriptionController(View view) {
        PendingIntent pendingIntent = this.mOnGreetingTap;
        if (pendingIntent == null) {
            this.mDefaultOnTap.run();
            return;
        }
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException unused) {
            Log.e("TranscriptionController", "Greeting tap PendingIntent cancelled");
            this.mDefaultOnTap.run();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    private View createView(int i) {
        View inflate = LayoutInflater.from(this.mContext).inflate(i, this.mParent, false);
        inflate.setVisibility(8);
        this.mParent.addView(inflate);
        return inflate;
    }
}

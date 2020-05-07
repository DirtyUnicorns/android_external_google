package com.google.android.systemui.assist.uihints;

import com.google.android.systemui.assist.uihints.TranscriptionController;
import com.google.common.base.Function;

public final class TranscriptionControllerLambda implements Function {
    private final /* synthetic */ TranscriptionController mTranscriptionController;
    private final /* synthetic */ TranscriptionController.State mState;

    public /* synthetic */ TranscriptionControllerLambda(TranscriptionController transcriptionController, TranscriptionController.State state) {
        mTranscriptionController = transcriptionController;
        mState = state;
    }

    public final Object apply(Object object) {
        return mTranscriptionController.maybeSetStateTranscriptionControllerLambda(mState, (Void)object);
    }
}

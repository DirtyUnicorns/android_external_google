package com.google.android.systemui.assist.uihints;

import java.util.concurrent.Executor;

public final /* synthetic */ class LightnessProviderLambda implements Executor {
    public static final /* synthetic */ LightnessProviderLambda INSTANCE = new LightnessProviderLambda();

    private /* synthetic */ LightnessProviderLambda() {
    }

    public final void execute(Runnable runnable) {
        runnable.run();
    }
}

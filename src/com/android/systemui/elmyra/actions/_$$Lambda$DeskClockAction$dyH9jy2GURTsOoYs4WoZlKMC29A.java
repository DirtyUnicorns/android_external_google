package com.google.android.systemui.elmyra.actions;

import java.util.function.Consumer;

public final /* synthetic */ class _$$Lambda$DeskClockAction$dyH9jy2GURTsOoYs4WoZlKMC29A implements Consumer {
    private final /* synthetic */ DeskClockAction f$0;

    public /* synthetic */ _$$Lambda$DeskClockAction$dyH9jy2GURTsOoYs4WoZlKMC29A(DeskClockAction deskClockAction) {
        f$0 = deskClockAction;
    }

    @Override
	public final void accept(Object obj) {
        f$0.updateBroadcastReceiver();
    }
}

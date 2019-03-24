package com.google.android.settings.connecteddevice.dock;

import java.util.function.Predicate;

/* compiled from: lambda */
public final /* synthetic */ class Lambda$SavedDockUpdater$J8Vme1TBFB2Oj8doX2QnYETFs1E implements Predicate {
    final /* synthetic */ SavedDockUpdater f$0;

    public /* synthetic */ Lambda$SavedDockUpdater$J8Vme1TBFB2Oj8doX2QnYETFs1E(SavedDockUpdater savedDockUpdater) {
        this.f$0 = savedDockUpdater;
    }

    public final boolean test(Object obj) {
        return this.f$0.hasDeviceBeenRemoved((String) obj);
    }
}
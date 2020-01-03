package com.google.android.systemui.elmyra.gates;

import android.content.Context;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.CommandQueue.Callbacks;
import com.google.android.systemui.elmyra.actions.Action;
import java.util.ArrayList;
import java.util.List;

public class NavigationBarVisibility extends Gate {
    private final CommandQueue mCommandQueue;
    private final Callbacks mCommandQueueCallbacks = new CommandQueueCallbacks();
    private final List<Action> mExceptions;
    private boolean mIsNavigationHidden;

    private class CommandQueueCallbacks implements Callbacks {
        CommandQueueCallbacks() {
        }

        public void setWindowState(int i, int i2) {
            if (i == 2) {
                boolean z = i2 != 0;
                if (z != mIsNavigationHidden) {
                    mIsNavigationHidden = z;
                    notifyListener();
                }
            }
        }
    }

    public NavigationBarVisibility(Context context, List<Action> list) {
        super(context);
        mExceptions = new ArrayList(list);
        mIsNavigationHidden = false;
        mCommandQueue = (CommandQueue) SysUiServiceProvider.getComponent(context, CommandQueue.class);
        mCommandQueue.addCallback(mCommandQueueCallbacks);
    }

    protected boolean isBlocked() {
        for (int i = 0; i < mExceptions.size(); i++) {
            if (((Action) mExceptions.get(i)).isAvailable()) {
                return false;
            }
        }
        return mIsNavigationHidden;
    }

    protected void onActivate() {
    }

    protected void onDeactivate() {
    }
}

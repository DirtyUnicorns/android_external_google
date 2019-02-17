package com.google.android.systemui.elmyra.feedback;

import android.content.Context;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import java.util.ArrayList;
import java.util.List;

public abstract class NavigationBarEffect implements FeedbackEffect {
    private final Context mContext;
    private final List<FeedbackEffect> mFeedbackEffects = new ArrayList();

    public NavigationBarEffect(Context context) {
        mContext = context;
    }

    private void refreshFeedbackEffects() {
        StatusBar statusBar = (StatusBar) SysUiServiceProvider.getComponent(mContext, StatusBar.class);
        NavigationBarView navigationBarView = statusBar.getNavigationBarView();
        if (statusBar == null || navigationBarView == null
                || navigationBarView.isFullGestureMode()) {
            mFeedbackEffects.clear();
            return;
        }
        if (!validateFeedbackEffects(mFeedbackEffects)) {
            mFeedbackEffects.clear();
        }
        if (mFeedbackEffects.isEmpty()) {
            mFeedbackEffects.addAll(findFeedbackEffects(navigationBarView));
        }
    }

    protected abstract List<FeedbackEffect> findFeedbackEffects(NavigationBarView navigationBarView);

    protected boolean isActiveFeedbackEffect(FeedbackEffect feedbackEffect) {
        return true;
    }

    @Override
	public void onProgress(float f, int i) {
        refreshFeedbackEffects();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < mFeedbackEffects.size()) {
                FeedbackEffect feedbackEffect = mFeedbackEffects.get(i3);
                if (isActiveFeedbackEffect(feedbackEffect)) {
                    feedbackEffect.onProgress(f, i);
                }
                i2 = i3 + 1;
            } else {
                return;
            }
        }
    }

    @Override
	public void onRelease() {
        refreshFeedbackEffects();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < mFeedbackEffects.size()) {
                mFeedbackEffects.get(i2).onRelease();
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public void onResolve(DetectionProperties detectionProperties) {
        refreshFeedbackEffects();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < mFeedbackEffects.size()) {
                mFeedbackEffects.get(i2).onResolve(detectionProperties);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    protected abstract boolean validateFeedbackEffects(List<FeedbackEffect> list);
}

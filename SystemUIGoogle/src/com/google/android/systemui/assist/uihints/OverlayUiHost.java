package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.util.ArrayList;

public class OverlayUiHost {
    private final int MINIMUM_MARGIN_PX;
    private boolean mAttached = false;
    private final Context mContext;
    private boolean mFocusable = false;
    private boolean mGesturalMode = true;
    private WindowManager.LayoutParams mLayoutParams;
    private ArrayList<BottomMarginListener> mMarginListeners = new ArrayList<>();
    private boolean mPortraitMode = true;
    private final ViewGroup mRoot;
    private final WindowManager mWindowManager;

    interface BottomMarginListener {
        void onBottomMarginChanged(int i);
    }

    private class RootView extends FrameLayout {
        private final Display mDisplay;
        private final Runnable mHideUi;
        private final Runnable mTouchOutside;

        public RootView(Context context, Runnable runnable, Runnable runnable2) {
            super(context);
            this.mHideUi = runnable;
            this.mTouchOutside = runnable2;
            this.mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
            setClipChildren(false);
        }

        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            if (keyEvent.getAction() != 1 || keyEvent.getKeyCode() != 4) {
                return super.dispatchKeyEvent(keyEvent);
            }
            this.mHideUi.run();
            return true;
        }

        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getAction() != 4) {
                return super.dispatchTouchEvent(motionEvent);
            }
            this.mTouchOutside.run();
            return false;
        }
    }

    public OverlayUiHost(Context context, Runnable runnable, Runnable runnable2) {
        this.mContext = context;
        this.mRoot = new RootView(context, runnable, runnable2);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.MINIMUM_MARGIN_PX = DisplayUtils.convertDpToPx(16.0f, this.mContext);
    }

    public ViewGroup getParent() {
        return this.mRoot;
    }

    public void setAssistState(boolean z, boolean z2) {
        if (z && !this.mAttached) {
            this.mLayoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2024, 262952, -3);
            this.mFocusable = z2;
            WindowManager.LayoutParams layoutParams = this.mLayoutParams;
            layoutParams.gravity = 80;
            layoutParams.privateFlags = 64;
            layoutParams.setTitle("Assist");
            this.mWindowManager.addView(this.mRoot, this.mLayoutParams);
            this.mAttached = true;
        } else if (!z && this.mAttached) {
            this.mWindowManager.removeViewImmediate(this.mRoot);
            this.mAttached = false;
        } else if (z && this.mFocusable != z2) {
            this.mWindowManager.updateViewLayout(this.mRoot, this.mLayoutParams);
            this.mFocusable = z2;
        }
    }

    /* access modifiers changed from: package-private */
    public void addMarginListener(BottomMarginListener bottomMarginListener) {
        this.mMarginListeners.add(bottomMarginListener);
        bottomMarginListener.onBottomMarginChanged(getMargin());
    }

    private int getMargin() {
        return this.MINIMUM_MARGIN_PX;
    }
}

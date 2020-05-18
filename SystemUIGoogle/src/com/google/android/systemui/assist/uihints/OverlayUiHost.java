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
    private boolean mAttached;
    private final Context mContext;
    private boolean mFocusable;
    private boolean mGesturalMode;
    private WindowManager.LayoutParams mLayoutParams;
    private ArrayList<BottomMarginListener> mMarginListeners;
    private boolean mPortraitMode;
    private final ViewGroup mRoot;
    private final WindowManager mWindowManager;

    interface BottomMarginListener {
        void onBottomMarginChanged();
    }

    private static class RootView extends FrameLayout {
        private final Display mDisplay;
        private final Runnable mHideUi;
        private final Runnable mTouchOutside;

        public RootView(Context context, Runnable runnable, Runnable runnable2) {
            super(context);
            mHideUi = runnable;
            mTouchOutside = runnable2;
            mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
            setClipChildren(false);
        }

        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            if (keyEvent.getAction() != 1 || keyEvent.getKeyCode() != 4) {
                return super.dispatchKeyEvent(keyEvent);
            }
            mHideUi.run();
            return true;
        }

        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getAction() != 4) {
                return super.dispatchTouchEvent(motionEvent);
            }
            mTouchOutside.run();
            return false;
        }
    }

    public OverlayUiHost(Context context, Runnable runnable, Runnable runnable2) {
        mContext = context;
        mAttached = false;
        mFocusable = false;
        mGesturalMode = true;
        mMarginListeners = new ArrayList<>();
        mPortraitMode = true;
        mRoot = new RootView(context, runnable, runnable2);
        mWindowManager = (WindowManager) context.getSystemService("window");
    }

    public ViewGroup getParent() {
        return mRoot;
    }

    public void setAssistState(boolean z, boolean z2) {
        if (z && !mAttached) {
            mLayoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2024, 262952, -3);
            mFocusable = z2;
            WindowManager.LayoutParams layoutParams = mLayoutParams;
            layoutParams.gravity = 80;
            layoutParams.privateFlags = 64;
            layoutParams.setTitle("Assist");
            mWindowManager.addView(mRoot, mLayoutParams);
            mAttached = true;
        } else if (!z && mAttached) {
            mWindowManager.removeViewImmediate(mRoot);
            mAttached = false;
        } else if (z && mFocusable != z2) {
            mWindowManager.updateViewLayout(mRoot, mLayoutParams);
            mFocusable = z2;
        }
    }

    void addMarginListener(BottomMarginListener bottomMarginListener) {
        mMarginListeners.add(bottomMarginListener);
        bottomMarginListener.onBottomMarginChanged();
    }
}

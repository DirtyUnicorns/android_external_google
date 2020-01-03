package com.google.android.systemui.dreamliner;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowView;

public class DockIndicationController implements StateListener, OnClickListener, OnAttachStateChangeListener {
    @VisibleForTesting
    static final String ACTION_ASSISTANT_POODLE = "com.google.android.systemui.dreamliner.ASSISTANT_POODLE";
    @VisibleForTesting
    static final String ACTION_HOME_CONTROL = "com.google.android.systemui.dreamliner.HOME_CONTROL";
    @VisibleForTesting
    ImageView mAssistantPoodle;
    private boolean mAssistantPoodleShowing;
    private final Context mContext;
    @VisibleForTesting
    ImageView mDockedTopIcon;
    private boolean mDocking;
    private boolean mDozing;
    @VisibleForTesting
    ImageView mHomeControl;
    private boolean mHomeControlShowing;
    @VisibleForTesting
    boolean mIconViewsValidated;

    public void onViewAttachedToWindow(View view) {
    }

    public DockIndicationController(Context context) {
        mContext = context;
        ((SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this);
    }

    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
        if (id == R.id.home_control) {
            intent = new Intent(ACTION_HOME_CONTROL);
        } else {
            if (id == R.id.assistant_poodle) {
                intent = new Intent(ACTION_ASSISTANT_POODLE);
            } else if (id != R.id.docked_top_icon) {
                return;
            } else {
                if (mAssistantPoodleShowing) {
                    intent = new Intent(ACTION_ASSISTANT_POODLE);
                } else {
                    intent = new Intent(ACTION_HOME_CONTROL);
                }
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        } catch (SecurityException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot send event for intent= ");
            sb.append(intent);
            Log.w("DockIndicationController", sb.toString(), e);
        }
    }

    public void onDozingChanged(boolean z) {
        mDozing = z;
        updateVisibility();
    }

    public void onViewDetachedFromWindow(View view) {
        view.removeOnAttachStateChangeListener(this);
        mIconViewsValidated = false;
        mHomeControl = null;
        mAssistantPoodle = null;
        mDockedTopIcon = null;
    }

    public void setShowing(int i, boolean z) {
        if (i == 0) {
            mHomeControlShowing = z;
        } else if (i == 1) {
            mAssistantPoodleShowing = z;
        }
        updateVisibility();
    }

    public void setDocking(boolean z) {
        mDocking = z;
        if (!mDocking) {
            mHomeControlShowing = false;
            mAssistantPoodleShowing = false;
        }
        updateVisibility();
    }

    /* access modifiers changed from: 0000 */
    @VisibleForTesting
    public void initializeIconViews() {
        StatusBarWindowView statusBarWindow = ((StatusBar) SysUiServiceProvider.getComponent(mContext, StatusBar.class)).getStatusBarWindow();
        mHomeControl = (ImageView) statusBarWindow.findViewById(R.id.home_control);
        mHomeControl.setOnClickListener(this);
        mAssistantPoodle = (ImageView) statusBarWindow.findViewById(R.id.assistant_poodle);
        mAssistantPoodle.setOnClickListener(this);
        mDockedTopIcon = (ImageView) statusBarWindow.findViewById(R.id.docked_top_icon);
        mDockedTopIcon.setOnClickListener(this);
        statusBarWindow.findViewById(R.id.keyguard_indication_area).addOnAttachStateChangeListener(this);
        mIconViewsValidated = true;
    }

    private void updateVisibility() {
        if (!mIconViewsValidated) {
            initializeIconViews();
        }
        if (!mDozing || !mDocking || (!mAssistantPoodleShowing && !mHomeControlShowing)) {
            mDockedTopIcon.setVisibility(View.GONE);
            mHomeControl.setVisibility(View.GONE);
            mAssistantPoodle.setVisibility(View.GONE);
        } else if (!mAssistantPoodleShowing || !mHomeControlShowing) {
            mDockedTopIcon.setImageResource(mAssistantPoodleShowing ? R.drawable.ic_assistant_logo : R.drawable.ic_home_logo);
            mDockedTopIcon.setVisibility(View.VISIBLE);
            mAssistantPoodle.setVisibility(View.GONE);
            mHomeControl.setVisibility(View.GONE);
        } else {
            mDockedTopIcon.setVisibility(View.GONE);
            mAssistantPoodle.setVisibility(View.VISIBLE);
            mHomeControl.setVisibility(View.VISIBLE);
        }
    }
}
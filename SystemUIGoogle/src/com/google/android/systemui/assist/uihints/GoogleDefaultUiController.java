package com.google.android.systemui.assist.uihints;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.systemui.C1737R$layout;
import com.android.systemui.assist.p003ui.DefaultUiController;

public class GoogleDefaultUiController extends DefaultUiController {
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.widget.FrameLayout, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public GoogleDefaultUiController(Context context) {
        super(context);
        context.getResources();
        setGoogleAssistant(false);
        super.mInvocationLightsView = (AssistantInvocationLightsView) LayoutInflater.from(context).inflate(C1737R$layout.invocation_lights, (ViewGroup) super.mRoot, false);
        super.mRoot.addView(super.mInvocationLightsView);
    }

    public void setGoogleAssistant(boolean z) {
        ((AssistantInvocationLightsView) super.mInvocationLightsView).setGoogleAssistant(z);
    }
}

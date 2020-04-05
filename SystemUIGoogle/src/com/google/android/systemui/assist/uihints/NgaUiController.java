package com.google.android.systemui.assist.uihints;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Region;
import android.metrics.LogMaker;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.google.android.systemui.assist.uihints.NgaUiController;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsController;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;
import com.google.android.systemui.assist.uihints.edgelights.mode.FullListening;
import com.google.android.systemui.assist.uihints.edgelights.mode.Gone;
import com.google.android.systemui.assist.uihints.edgelights.mode.HalfListening;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class NgaUiController implements AssistManager.UiController, ViewTreeObserver.OnComputeInternalInsetsListener, StatusBarStateController.StateListener, ConfigurationController.ConfigurationListener {
    private static final long INVOCATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(3);
    private static final long SESSION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
    /* access modifiers changed from: private */
    public static final boolean VERBOSE = (Build.TYPE.toLowerCase(Locale.ROOT).contains("debug") || Build.TYPE.toLowerCase(Locale.ROOT).equals("eng"));
    private static final PathInterpolator mProgressInterpolator = new PathInterpolator(0.83f, 0.0f, 0.84f, 1.0f);
    /* access modifiers changed from: private */
    public long mColorMonitoringStart = 0;
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mDidSetParent = false;
    /* access modifiers changed from: private */
    public EdgeLightsController mEdgeLightsController;
    private float mFlingVelocity = 0.0f;
    /* access modifiers changed from: private */
    public GlowController mGlowController;
    private int mGlowVisibility = 8;
    private boolean mHasDarkBackground = false;
    /* access modifiers changed from: private */
    public boolean mHaveAccurateLuma = false;
    private ValueAnimator mInvocationAnimator;
    private boolean mInvocationInProgress = false;
    private AssistantInvocationLightsView mInvocationLightsView;
    private boolean mIsMonitoringColor = false;
    private KeyboardIconView mKeyboardIcon;
    private PendingIntent mKeyboardIntent = null;
    private boolean mKeyboardRequested = false;
    private float mLastInvocationProgress = 0.0f;
    private long mLastInvocationStartTime = 0;
    private final LightnessProvider mLightnessProvider = new LightnessProvider(new LightnessListener() {
        /* class com.google.android.systemui.assist.uihints.NgaUiController.C15651 */

        public void onLightnessUpdate(float f) {
            boolean unused = NgaUiController.VERBOSE;
            if (NgaUiController.this.mColorMonitoringStart > 0) {
                long elapsedRealtime = SystemClock.elapsedRealtime() - NgaUiController.this.mColorMonitoringStart;
                if (NgaUiController.VERBOSE) {
                    Log.d("NgaUiController", "Got lightness update (" + f + ") after " + elapsedRealtime + " ms");
                }
                long unused2 = NgaUiController.this.mColorMonitoringStart = 0;
            }
            boolean z = true;
            boolean unused3 = NgaUiController.this.mHaveAccurateLuma = true;
            NgaUiController.this.mGlowController.setHasMedianLightness(f);
            NgaUiController.this.mTranscriptionController.setHasAccurateBackground(true);
            NgaUiController ngaUiController = NgaUiController.this;
            if (f > NgaUiController.getDarkUiThreshold()) {
                z = false;
            }
            ngaUiController.setHasDarkBackground(z);
            NgaUiController.this.refresh();
        }
    });
    private ValueAnimator mNavBarAlphaAnimator;
    private float mNavBarDestinationAlpha = -1.0f;
    private boolean mNgaPresent = false;
    /* access modifiers changed from: private */
    public Bundle mPendingEdgeLightsBundle = null;
    private PromptView mPromptView;
    private boolean mScrimVisible = false;
    private Runnable mSessionTimeout = new Runnable() {
        /* class com.google.android.systemui.assist.uihints.$$Lambda$NgaUiController$SvtRUSOonqfg0GbAnB7121JGCls */

        public final void run() {
            NgaUiController.this.lambda$new$0$NgaUiController();
        }
    };
    private boolean mShowingAssistUi = false;
    private float mSpeechConfidence = 0.0f;
    private final TaskStackNotifier mTaskStackNotifier = new TaskStackNotifier();
    private PendingIntent mTouchInsidePendingIntent;
    private PendingIntent mTouchOutsidePendingIntent;
    /* access modifiers changed from: private */
    public TranscriptionController mTranscriptionController;
    /* access modifiers changed from: private */
    public final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final OverlayUiHost mUiHost;
    private PowerManager.WakeLock mWakeLock;
    private ZeroStateIconView mZeroStateIcon;
    private PendingIntent mZeroStateIntent = null;
    private boolean mZeroStateRequested = false;

    public static final float getDarkUiThreshold() {
        return 0.4f;
    }

    public /* synthetic */ void lambda$new$0$NgaUiController() {
        if (this.mShowingAssistUi) {
            Log.e("NgaUiController", "Timed out");
            closeNgaUi();
            MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(4));
        }
    }

    public NgaUiController(Context context) {
        this.mContext = context;
        this.mUiHost = new OverlayUiHost(context, new Runnable() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$NgaUiController$6Tb1SxVcajpw3wCrZyzkldewS60 */

            public final void run() {
                NgaUiController.this.lambda$new$1$NgaUiController();
            }
        }, new Runnable() {
            /* class com.google.android.systemui.assist.uihints.$$Lambda$NgaUiController$PbaQdfvr1mXbXqUrwmKEuwHUT4c */

            public final void run() {
                NgaUiController.this.lambda$new$2$NgaUiController();
            }
        });
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(805306378, "Assist (NGA)");
        setViewParent();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        refresh();
    }

    public /* synthetic */ void lambda$new$1$NgaUiController() {
        closeNgaUi();
        MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(3));
    }

    public /* synthetic */ void lambda$new$2$NgaUiController() {
        PendingIntent pendingIntent = this.mTouchOutsidePendingIntent;
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException unused) {
                Log.w("NgaUiController", "Touch outside PendingIntent canceled");
            }
        }
    }

    public void processBundle(Bundle bundle) {
        this.mUiHandler.removeCallbacks(this.mSessionTimeout);
        // TODO: Inspect a little bit more this
        if (Looper.myLooper() != this.mUiHandler.getLooper()) {
            mUiHandler.post(() -> processBundle(bundle));
            return;
        }

        this.mUiHandler.postDelayed(this.mSessionTimeout, SESSION_TIMEOUT_MS);
        setViewParent();
        logBundle(bundle);
        String string = bundle.getString("action", "");
        char c = 65535;
        switch (string.hashCode()) {
            case -2051025175:
                if (string.equals("show_keyboard")) {
                    c = 10;
                    break;
                }
                break;
            case -2040419289:
                if (string.equals("show_zerostate")) {
                    c = 12;
                    break;
                }
                break;
            case -1354792126:
                if (string.equals("config")) {
                    c = 3;
                    break;
                }
                break;
            case -1160605116:
                if (string.equals("hide_keyboard")) {
                    c = 11;
                    break;
                }
                break;
            case -241763182:
                if (string.equals("transcription")) {
                    c = 5;
                    break;
                }
                break;
            case -207201236:
                if (string.equals("hide_zerostate")) {
                    c = 13;
                    break;
                }
                break;
            case 3046160:
                if (string.equals("card")) {
                    c = 2;
                    break;
                }
                break;
            case 94631335:
                if (string.equals("chips")) {
                    c = 7;
                    break;
                }
                break;
            case 94746189:
                if (string.equals("clear")) {
                    c = 8;
                    break;
                }
                break;
            case 205422649:
                if (string.equals("greeting")) {
                    c = 6;
                    break;
                }
                break;
            case 371207756:
                if (string.equals("start_activity")) {
                    c = 9;
                    break;
                }
                break;
            case 771587807:
                if (string.equals("edge_lights")) {
                    c = 4;
                    break;
                }
                break;
            case 1549039479:
                if (string.equals("audio_info")) {
                    c = 1;
                    break;
                }
                break;
            case 1642639251:
                if (string.equals("keep_alive")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                break;
            case 1:
                float f = bundle.getFloat("volume");
                float f2 = bundle.getFloat("speech_confidence");
                if (f2 > 0.0f) {
                    this.mSpeechConfidence = f2;
                }
                this.mEdgeLightsController.onAudioLevelUpdate(this.mSpeechConfidence, f);
                this.mGlowController.onAudioLevelUpdate(this.mSpeechConfidence, f);
                break;
            case 2:
                boolean z = bundle.getBoolean("is_visible", false);
                int i = bundle.getInt("sysui_color", 0);
                boolean z2 = bundle.getBoolean("animate_transition", true);
                boolean z3 = bundle.getBoolean("card_forces_scrim", false);
                this.mGlowController.setCardVisible(z);
                this.mGlowController.getScrimController().setCardVisible(z, z2, z3);
                this.mLightnessProvider.setCardVisible(z, i);
                this.mTranscriptionController.setCardVisible(z);
                break;
            case 3:
                boolean z4 = this.mNgaPresent;
                this.mNgaPresent = bundle.getBoolean("is_available", false);
                this.mTouchOutsidePendingIntent = (PendingIntent) bundle.getParcelable("touch_outside");
                this.mTouchInsidePendingIntent = (PendingIntent) bundle.getParcelable("touch_inside");
                this.mTaskStackNotifier.setIntent((PendingIntent) bundle.getParcelable("task_stack_changed"));
                if (z4 && !this.mNgaPresent) {
                    hide();
                    break;
                }
            case 4:
                ValueAnimator valueAnimator = this.mInvocationAnimator;
                if (valueAnimator == null || !valueAnimator.isStarted()) {
                    if (bundle.get("state").equals("FULL_LISTENING") && !this.mShowingAssistUi) {
                        this.mInvocationInProgress = true;
                        onInvocationProgress(0, 1.0f);
                        this.mPendingEdgeLightsBundle = bundle;
                        break;
                    } else {
                        this.mPendingEdgeLightsBundle = null;
                        this.mEdgeLightsController.setState(bundle);
                        break;
                    }
                } else {
                    this.mPendingEdgeLightsBundle = bundle;
                    break;
                }
            case 5:
                this.mTranscriptionController.setTranscription(bundle.getString("text"), (PendingIntent) bundle.getParcelable("tap_action"));
                this.mTranscriptionController.setTranscriptionColor(bundle.getInt("text_color", 0));
                break;
            case 6:
                this.mTranscriptionController.setGreeting(bundle.getString("text"), this.mFlingVelocity, (PendingIntent) bundle.getParcelable("tap_action"));
                break;
            case 7:
                this.mTranscriptionController.setChips(bundle.getParcelableArrayList("chips"));
                break;
            case 8:
                this.mTranscriptionController.clear(bundle.getBoolean("show_animation", true));
                break;
            case 9:
                Intent intent = (Intent) bundle.getParcelable("intent");
                if (intent != null) {
                    ((StatusBar) SysUiServiceProvider.getComponent(this.mContext, StatusBar.class)).startActivity(intent, bundle.getBoolean("dismiss_shade"));
                    break;
                } else {
                    Log.e("NgaUiController", "Null intent; cannot start activity");
                    return;
                }
            case 10:
                this.mKeyboardRequested = true;
                this.mKeyboardIntent = (PendingIntent) bundle.getParcelable("tap_action");
                refresh();
                break;
            case 11:
                this.mKeyboardRequested = false;
                this.mKeyboardIntent = null;
                refresh();
                break;
            case 12:
                this.mZeroStateRequested = true;
                this.mZeroStateIntent = (PendingIntent) bundle.getParcelable("tap_action");
                refresh();
                break;
            case 13:
                this.mZeroStateRequested = false;
                this.mZeroStateIntent = null;
                refresh();
                break;
            default:
                Log.w("NgaUiController", String.format("Unknown action \"%s\"; cannot process RPC", string));
                break;
        }
        if (!"config".equals(string)) {
            this.mNgaPresent = true;
        }
        refresh();
    }

    public boolean extractNga(Bundle bundle) {
        logBundle(bundle);
        if ("config".equals(bundle.getString("action"))) {
            return bundle.getBoolean("is_available", false);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void refresh() {
        if (this.mDidSetParent) {
            updateShowingKeyboard();
            updateShowingZeroState();
            updateShowingAssistUi();
            updateShowingNavBar();
        }
    }

    private void updateShowingKeyboard() {
        if (!this.mKeyboardRequested) {
            this.mKeyboardIcon.hide();
        } else if (this.mHaveAccurateLuma) {
            this.mKeyboardIcon.show(this.mKeyboardIntent);
        }
    }

    private void updateShowingZeroState() {
        if (!this.mZeroStateRequested) {
            this.mZeroStateIcon.hide();
        } else if (this.mHaveAccurateLuma) {
            this.mZeroStateIcon.show(this.mZeroStateIntent);
        }
    }

    private void logBundle(Bundle bundle) {
        Object obj;
        if (bundle == null) {
            Log.w("NgaUiController", "Received null bundle!");
        } else if (VERBOSE && !"audio_info".equals(bundle.get("action"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("<UiHintsBundle>\n");
            for (String next : bundle.keySet()) {
                if ("text".equals(next)) {
                    String string = bundle.getString(next);
                    Object[] objArr = new Object[1];
                    if (string == null) {
                        obj = "NULL";
                    } else {
                        obj = Integer.valueOf(string.length());
                    }
                    objArr[0] = obj;
                    sb.append(String.format("transcription length = %s\n", objArr));
                } else if ("chips".equals(next)) {
                    sb.append(String.format("%s =\n", "chips"));
                    ArrayList parcelableArrayList = bundle.getParcelableArrayList("chips");
                    if (parcelableArrayList != null) {
                        Iterator it = parcelableArrayList.iterator();
                        while (it.hasNext()) {
                            Bundle bundle2 = (Bundle) it.next();
                            for (String next2 : bundle2.keySet()) {
                                sb.append(String.format("\t%s = %s\n", next2, bundle2.get(next2)));
                            }
                        }
                        sb.append("\n");
                    }
                } else {
                    sb.append(String.format("%s = %s\n", next, bundle.get(next)));
                }
            }
            sb.append("</UiHintsBundle>");
            if (!"transcription".equals(bundle.get("action")) || !this.mTranscriptionController.isTranscribing()) {
                Log.i("NgaUiController", sb.toString());
            } else {
                Log.v("NgaUiController", sb.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void setHasDarkBackground(boolean z) {
        String str = "dark";
        if (this.mHasDarkBackground != z) {
            this.mHasDarkBackground = z;
            if (VERBOSE) {
                StringBuilder sb = new StringBuilder();
                sb.append("switching to ");
                if (!this.mHasDarkBackground) {
                    str = "light";
                }
                sb.append(str);
                Log.v("NgaUiController", sb.toString());
            }
            dispatchHasDarkBackground();
        } else if (VERBOSE) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("not switching; already ");
            if (!z) {
                str = "light";
            }
            sb2.append(str);
            Log.v("NgaUiController", sb2.toString());
        }
    }

    private void dispatchHasDarkBackground() {
        if (this.mDidSetParent) {
            this.mTranscriptionController.setHasDarkBackground(this.mHasDarkBackground);
            this.mKeyboardIcon.setHasDarkBackground(this.mHasDarkBackground);
            this.mPromptView.setHasDarkBackground(this.mHasDarkBackground);
            this.mKeyboardIcon.setHasDarkBackground(this.mHasDarkBackground);
            this.mZeroStateIcon.setHasDarkBackground(this.mHasDarkBackground);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    private void setViewParent() {
        if (!this.mDidSetParent) {
            ViewGroup parent = this.mUiHost.getParent();
            if (parent == null) {
                Log.e("NgaUiController", "Status bar view unavailable!");
                return;
            }
            // TODO-FIXME: Investigate whether we need to create a specific class for this or not. And eventually change name.
            Runnable r8 = new Runnable(){
            
                @Override
                public void run() {
                    lambda$setViewParent$4$NgaUiController();
                    
                }
            };

            LayoutInflater from = LayoutInflater.from(this.mContext);
            this.mGlowController = new GlowController(this.mContext, parent, this.mLightnessProvider, new VisibilityListener() {
                /* class com.google.android.systemui.assist.uihints.$$Lambda$NgaUiController$xj20nN5laJ4ty8_3z7U9IZwhTC8 */

                public final void onVisibilityChanged(int i) {
                    NgaUiController.this.lambda$setViewParent$5$NgaUiController(i);
                }
            }, r8);
            this.mInvocationLightsView = (AssistantInvocationLightsView) from.inflate(R.layout.invocation_lights, parent, false);
            this.mInvocationLightsView.setGoogleAssistant(true);
            parent.addView(this.mInvocationLightsView);
            this.mEdgeLightsController = new EdgeLightsController(this.mContext, parent);
            this.mEdgeLightsController.addListener(this.mGlowController);
            this.mTranscriptionController = new TranscriptionController(this.mContext, parent, r8);
            this.mTranscriptionController.setListener(this.mGlowController.getScrimController());
            this.mKeyboardIcon = (KeyboardIconView) from.inflate(R.layout.keyboard_icon, parent, false);
            parent.addView(this.mKeyboardIcon);
            this.mZeroStateIcon = (ZeroStateIconView) LayoutInflater.from(this.mContext).inflate(R.layout.zerostate_icon, parent, false);
            parent.addView(this.mZeroStateIcon);
            this.mPromptView = (PromptView) from.inflate(R.layout.assist_prompt, parent, false);
            parent.addView(this.mPromptView);
            this.mUiHost.addMarginListener(this.mTranscriptionController);
            this.mUiHost.addMarginListener(this.mKeyboardIcon);
            this.mUiHost.addMarginListener(this.mZeroStateIcon);
            this.mUiHost.addMarginListener(this.mPromptView);
            this.mDidSetParent = true;
            dispatchHasDarkBackground();
            if (VERBOSE) {
                Log.v("NgaUiController", "Added UI");
            }
        }
    }

    public /* synthetic */ void lambda$setViewParent$4$NgaUiController() {
        PendingIntent pendingIntent = this.mTouchInsidePendingIntent;
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException unused) {
                Log.w("NgaUiController", "Touch outside PendingIntent canceled");
                closeNgaUi();
            }
        } else {
            closeNgaUi();
        }
        MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(2));
    }

    public /* synthetic */ void lambda$setViewParent$5$NgaUiController(int i) {
        boolean isScrimVisible = this.mGlowController.isScrimVisible();
        if (this.mScrimVisible != isScrimVisible) {
            this.mScrimVisible = isScrimVisible;
            refresh();
        }
        if (this.mGlowVisibility != i) {
            this.mGlowVisibility = i;
            refresh();
        }
    }

    private void closeNgaUi() {
        ((AssistManager) Dependency.get(AssistManager.class)).hideAssist();
        hide();
    }

    public void hide() {
        ValueAnimator valueAnimator = this.mInvocationAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            this.mInvocationAnimator.cancel();
        }
        this.mInvocationInProgress = false;
        this.mTranscriptionController.clear(false);
        this.mEdgeLightsController.setState(new Gone());
        this.mPendingEdgeLightsBundle = null;
        this.mPromptView.disable();
        this.mZeroStateRequested = false;
        this.mKeyboardRequested = false;
        refresh();
    }

    private void setColorMonitoringState(boolean z) {
        if (this.mIsMonitoringColor != z) {
            if (!z || this.mGlowController.getScrimSurfaceControllerHandle() != null) {
                this.mIsMonitoringColor = z;
                if (this.mIsMonitoringColor) {
                    int rotatedHeight = (DisplayUtils.getRotatedHeight(this.mContext) - ((int) this.mContext.getResources().getDimension(R.dimen.transcription_space_bottom_margin))) - DisplayUtils.convertSpToPx(20.0f, this.mContext);
                    Rect rect = new Rect(0, rotatedHeight - DisplayUtils.convertDpToPx(160.0f, this.mContext), DisplayUtils.getRotatedWidth(this.mContext), rotatedHeight);
                    this.mColorMonitoringStart = SystemClock.elapsedRealtime();
                    this.mLightnessProvider.enableColorMonitoring(true, rect, this.mGlowController.getScrimSurfaceControllerHandle());
                    return;
                }
                this.mLightnessProvider.enableColorMonitoring(false, null, null);
                this.mHaveAccurateLuma = false;
                this.mGlowController.getScrimController().onLightnessInvalidated();
                this.mTranscriptionController.setHasAccurateBackground(false);
            }
        }
    }

    private void updateShowingAssistUi() {
        boolean z = !(this.mEdgeLightsController.getMode() instanceof Gone) || this.mGlowController.isVisible() || this.mInvocationInProgress || this.mKeyboardIcon.getVisibility() == 0 || this.mZeroStateIcon.getVisibility() == 0;
        setColorMonitoringState(z);
        if (this.mShowingAssistUi != z) {
            this.mShowingAssistUi = z;
            ((ScreenDecorations) SysUiServiceProvider.getComponent(this.mContext, ScreenDecorations.class)).setAssistHintBlocked(z);
            if (this.mShowingAssistUi) {
                this.mWakeLock.acquire();
                this.mUiHost.getParent().getViewTreeObserver().addOnComputeInternalInsetsListener(this);
            } else {
                this.mWakeLock.release();
                this.mUiHost.getParent().getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
                ValueAnimator valueAnimator = this.mInvocationAnimator;
                if (valueAnimator != null && valueAnimator.isStarted()) {
                    this.mInvocationAnimator.cancel();
                }
            }
        }
        this.mUiHost.setAssistState(this.mShowingAssistUi, this.mEdgeLightsController.getMode() instanceof FullListening);
    }

    private void updateShowingNavBar() {
        EdgeLightsView.Mode mode = this.mEdgeLightsController.getMode();
        boolean z = !this.mInvocationInProgress && ((mode instanceof Gone) || (mode instanceof HalfListening));
        float f = z ? 1.0f : 0.0f;
        NavigationBarView defaultNavigationBarView = ((NavigationBarController) Dependency.get(NavigationBarController.class)).getDefaultNavigationBarView();
        if (defaultNavigationBarView != null) {
            float alpha = defaultNavigationBarView.getAlpha();
            if (f != alpha && f != this.mNavBarDestinationAlpha) {
                this.mNavBarDestinationAlpha = f;
                ValueAnimator valueAnimator = this.mNavBarAlphaAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                }
                this.mNavBarAlphaAnimator = ObjectAnimator.ofFloat(defaultNavigationBarView, View.ALPHA, alpha, f).setDuration((long) Math.abs((f - alpha) * 80.0f));
                if (z) {
                    this.mNavBarAlphaAnimator.setStartDelay(80);
                }
                this.mNavBarAlphaAnimator.start();
            }
        }
    }

    private float getAnimationProgress(int i, float f) {
        return i == 2 ? f * 0.95f : mProgressInterpolator.getInterpolation(f * 0.8f);
    }

    public void onInvocationProgress(int i, float f) {
        ValueAnimator valueAnimator = this.mInvocationAnimator;
        if (valueAnimator == null || !valueAnimator.isStarted()) {
            setViewParent();
            if (this.mDidSetParent) {
                if (!this.mEdgeLightsController.getMode().preventsInvocations()) {
                    boolean z = this.mInvocationInProgress;
                    int i2 = (f > 1.0f ? 1 : (f == 1.0f ? 0 : -1));
                    if (i2 < 0) {
                        this.mLastInvocationProgress = f;
                        if (!z && f > 0.0f) {
                            this.mLastInvocationStartTime = SystemClock.uptimeMillis();
                        }
                        this.mInvocationInProgress = f > 0.0f && i2 < 0;
                        if (!this.mInvocationInProgress) {
                            this.mPromptView.disable();
                        } else if (f < 0.9f && SystemClock.uptimeMillis() - this.mLastInvocationStartTime > 200) {
                            this.mPromptView.enable();
                        }
                        setProgress(i, getAnimationProgress(i, f));
                    } else {
                        ValueAnimator valueAnimator2 = this.mInvocationAnimator;
                        if (valueAnimator2 == null || !valueAnimator2.isStarted()) {
                            this.mFlingVelocity = 0.0f;
                            completeInvocation(i);
                        }
                    }
                    logInvocationProgressMetrics(i, f, z);
                } else if (VERBOSE) {
                    Log.v("NgaUiController", "ignoring invocation; mode is " + this.mEdgeLightsController.getMode().getClass().getSimpleName());
                }
            }
        } else {
            Log.w("NgaUiController", "Already animating; ignoring invocation progress");
        }
    }

    private static void logInvocationProgressMetrics(int i, float f, boolean z) {
        if (!z && f > 0.0f) {
            MetricsLogger.action(new LogMaker(1716).setType(4).setSubtype(((AssistManager) Dependency.get(AssistManager.class)).toLoggingSubType(i)));
        }
        if (z && f == 0.0f) {
            MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(1));
        }
    }

    public void onGestureCompletion(float f) {
        setViewParent();
        if (this.mDidSetParent) {
            if (!this.mEdgeLightsController.getMode().preventsInvocations()) {
                this.mFlingVelocity = f;
                completeInvocation(1);
            } else if (VERBOSE) {
                Log.v("NgaUiController", "ignoring invocation; mode is " + this.mEdgeLightsController.getMode().getClass().getSimpleName());
            }
        }
    }

    private void setProgress(int i, float f) {
        this.mInvocationLightsView.onInvocationProgress(f);
        this.mGlowController.setInvocationProgress(f);
        this.mGlowController.getScrimController().setInvocationProgress(f);
        this.mPromptView.onInvocationProgress(i, f);
        updateShowingNavBar();
        refresh();
    }

    private void completeInvocation(int i) {
        if (!this.mNgaPresent) {
            setProgress(i, 0.0f);
            resetInvocationProgress();
            return;
        }
        this.mUiHandler.removeCallbacks(this.mSessionTimeout);
        this.mUiHandler.postDelayed(this.mSessionTimeout, INVOCATION_TIMEOUT_MS);
        this.mPromptView.disable();
        ValueAnimator valueAnimator = this.mInvocationAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            this.mInvocationAnimator.cancel();
        }
        float f = this.mFlingVelocity;
        float f2 = 3.0f;
        if (f != 0.0f) {
            f2 = MathUtils.constrain((-f) / 1.45f, 3.0f, 12.0f);
        }
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(f2);
        
        ValueAnimator ofFloat = ValueAnimator.ofFloat(approximateInverse(Float.valueOf(getAnimationProgress(i, mLastInvocationProgress)), (v) -> Float.valueOf(Math.min(1.0f, overshootInterpolator.getInterpolation(v)))));
        ofFloat.setDuration(600L);
        ofFloat.setStartDelay(1);
        ofFloat.addUpdateListener(listener -> setProgress(i, overshootInterpolator.getInterpolation((Float)valueAnimator.getAnimatedValue())));
        ofFloat.addListener(new AnimatorListenerAdapter() {
            /* class com.google.android.systemui.assist.uihints.NgaUiController.C15662 */
            private boolean mCancelled = false;

            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (!this.mCancelled) {
                    if (NgaUiController.this.mPendingEdgeLightsBundle != null) {
                        NgaUiController.this.mEdgeLightsController.setState(NgaUiController.this.mPendingEdgeLightsBundle);
                    } else {
                        NgaUiController.this.mEdgeLightsController.setState(new FullListening(NgaUiController.this.mContext));
                    }
                    Bundle unused = NgaUiController.this.mPendingEdgeLightsBundle = null;
                }
                NgaUiController.this.mUiHandler.post(new Runnable() {
                    /* class com.google.android.systemui.assist.uihints.$$Lambda$NgaUiController$2$wDPTUQdfJQKhQ5xgkiBru4FbWhM */

                    public final void run() {
                        lambda$onAnimationEnd$0$NgaUiController$2();
                    }
                });
            }

            public void lambda$onAnimationEnd$0$NgaUiController$2() {
                NgaUiController.this.resetInvocationProgress();
            }
        });

        this.mInvocationAnimator = ofFloat;
        this.mInvocationAnimator.start();
    }

    public /* synthetic */ void lambda$completeInvocation$7$NgaUiController(int i, OvershootInterpolator overshootInterpolator, ValueAnimator valueAnimator) {
        setProgress(i, overshootInterpolator.getInterpolation(((Float) valueAnimator.getAnimatedValue()).floatValue()));
    }

    /* access modifiers changed from: private */
    public void resetInvocationProgress() {
        this.mInvocationInProgress = false;
        this.mInvocationLightsView.hide();
        this.mLastInvocationProgress = 0.0f;
        this.mGlowController.getScrimController().setInvocationProgress(0.0f);
        refresh();
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        Region region = new Region();
        if (this.mKeyboardIcon.getVisibility() == 0) {
            Rect rect = new Rect();
            this.mKeyboardIcon.getBoundsOnScreen(rect);
            region.union(rect);
        }
        if (this.mZeroStateIcon.getVisibility() == 0) {
            Rect rect2 = new Rect();
            this.mZeroStateIcon.getBoundsOnScreen(rect2);
            region.union(rect2);
        }
        Rect touchableRegion = this.mGlowController.getTouchableRegion();
        Rect touchableRegion2 = this.mTranscriptionController.getTouchableRegion();
        if (touchableRegion != null && touchableRegion2 != null) {
            touchableRegion.top = Integer.min(touchableRegion.top, touchableRegion2.top);
            region.union(touchableRegion);
        } else if (touchableRegion != null) {
            region.union(touchableRegion);
        } else if (touchableRegion2 != null) {
            region.union(touchableRegion2);
        }
        internalInsetsInfo.touchableRegion.set(region);
    }

    private float approximateInverse(Float f, Function<Float, Float> function) {
        ArrayList arrayList = new ArrayList((int) 200.0f);
        for (float f2 = 0.0f; f2 < 1.0f; f2 += 0.005f) {
            arrayList.add(function.apply(Float.valueOf(f2)));
        }
        int binarySearch = Collections.binarySearch(arrayList, f);
        if (binarySearch < 0) {
            binarySearch = (binarySearch + 1) * -1;
        }
        return ((float) binarySearch) * 0.005f;
    }

    public void onDozingChanged(boolean isDozing) {
        if (Looper.myLooper() != this.mUiHandler.getLooper()) {
           this.mUiHandler.post(() -> onDozingChanged(isDozing));
        } else {
           this.mGlowController.getScrimController().setIsDozing(isDozing);
           if (isDozing && this.mShowingAssistUi) {
              this.closeNgaUi();
           }
        }
    }

    public void onDensityOrFontScaleChanged() {
        this.mKeyboardIcon.onDensityChanged();
        this.mZeroStateIcon.onDensityChanged();
    }
}

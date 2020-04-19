package com.google.android.systemui.assist.uihints;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class NgaUiController implements AssistManager.UiController, ViewTreeObserver.OnComputeInternalInsetsListener, StatusBarStateController.StateListener, ConfigurationController.ConfigurationListener {

    private static long INVOCATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(3);

    private static long SESSION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);

    public static boolean VERBOSE = (Build.TYPE.toLowerCase(Locale.ROOT).contains("debug") || Build.TYPE.toLowerCase(Locale.ROOT).equals("eng"));
    private static PathInterpolator mProgressInterpolator = new PathInterpolator(0.83f, 0.0f, 0.84f, 1.0f);
    public long mColorMonitoringStart = 0;
    public Context mContext;
    private boolean mDidSetParent = false;
    public EdgeLightsController mEdgeLightsController;
    private float mFlingVelocity = 0.0f;
    public GlowController mGlowController;
    private int mGlowVisibility = 8;
    private boolean mHasDarkBackground = false;
    public boolean mHaveAccurateLuma = false;
    private ValueAnimator mInvocationAnimator = new ValueAnimator();
    private boolean mInvocationInProgress = false;
    private AssistantInvocationLightsView mInvocationLightsView;
    private boolean mIsMonitoringColor = false;
    private KeyboardIconView mKeyboardIcon;
    private PendingIntent mKeyboardIntent = null;
    private boolean mKeyboardRequested = false;
    private float mLastInvocationProgress = 0.0f;
    private long mLastInvocationStartTime = 0;

    private LightnessProvider mLightnessProvider = new LightnessProvider(new LightnessListener() {

        public void onLightnessUpdate(float f) {
            boolean unused = NgaUiController.VERBOSE;
            if (mColorMonitoringStart > 0) {
                long elapsedRealtime = SystemClock.elapsedRealtime() - mColorMonitoringStart;
                if (NgaUiController.VERBOSE) {
                    Log.d("NgaUiController", "Got lightness update (" + f + ") after " + elapsedRealtime + " ms");
                }
                long unused2 = mColorMonitoringStart = 0;
            }
            boolean z = true;
            boolean unused3 = mHaveAccurateLuma = true;
            mGlowController.setHasMedianLightness(f);
            mTranscriptionController.setHasAccurateBackground(true);
            NgaUiController ngaUiController = NgaUiController.this;
            if (f > NgaUiController.getDarkUiThreshold()) {
                z = false;
            }
            ngaUiController.setHasDarkBackground(z);
            refresh();
        }
    });

    private ValueAnimator mNavBarAlphaAnimator = new ValueAnimator();
    private float mNavBarDestinationAlpha = -1.0f;
    private boolean mNgaPresent = false;

    public Bundle mPendingEdgeLightsBundle = null;
    private PromptView mPromptView;
    private boolean mScrimVisible = false;
    private Runnable mSessionTimeout = new Runnable() {
        /* class com.google.android.systemui.assist.uihints.$$Lambda$NgaUiController$SvtRUSOonqfg0GbAnB7121JGCls */

        public final void run() {
            lambda$new$0$NgaUiController();
        }
    };
    private boolean mShowingAssistUi = false;
    private float mSpeechConfidence = 0.0f;
    private TaskStackNotifier mTaskStackNotifier = new TaskStackNotifier();
    private PendingIntent mTouchInsidePendingIntent;
    private PendingIntent mTouchOutsidePendingIntent;

    public TranscriptionController mTranscriptionController;

    public Handler mUiHandler = new Handler(Looper.getMainLooper());
    private OverlayUiHost mUiHost;
    private PowerManager.WakeLock mWakeLock;
    private ZeroStateIconView mZeroStateIcon;
    private PendingIntent mZeroStateIntent = null;
    private boolean mZeroStateRequested = false;

    public static float getDarkUiThreshold() {
        return 0.4f;
    }

    public void lambda$new$0$NgaUiController() {
        if (mShowingAssistUi) {
            Log.e("NgaUiController", "Timed out");
            closeNgaUi();
            MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(4));
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    public NgaUiController(Context context) {
        mContext = context;
        mUiHost = new OverlayUiHost(context, new Runnable() {
            public final void run() {
                lambda$new$1$NgaUiController();
            }
        }, new Runnable() {
            public final void run() {
                lambda$new$2$NgaUiController();
            }
        });
        mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(805306378, "Assist (NGA)");
        setViewParent();
        Dependency.get(StatusBarStateController.class).addCallback(this);
        Dependency.get(ConfigurationController.class).addCallback(this);
        refresh();
    }

    public void lambda$new$1$NgaUiController() {
        closeNgaUi();
        MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(3));
    }

    public void lambda$new$2$NgaUiController() {
        PendingIntent pendingIntent = mTouchOutsidePendingIntent;
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException unused) {
                Log.w("NgaUiController", "Touch outside PendingIntent canceled");
            }
        }
    }

    public void processBundle(Bundle bundle) {
        mUiHandler.removeCallbacks(mSessionTimeout);
        // TODO: Inspect a little bit more this
        if (Looper.myLooper() != mUiHandler.getLooper()) {
            mUiHandler.post(() -> processBundle(bundle));
            return;
        }

        mUiHandler.postDelayed(mSessionTimeout, SESSION_TIMEOUT_MS);
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
                    mSpeechConfidence = f2;
                }
                mEdgeLightsController.onAudioLevelUpdate(mSpeechConfidence, f);
                mGlowController.onAudioLevelUpdate(mSpeechConfidence, f);
                break;
            case 2:
                boolean z = bundle.getBoolean("is_visible", false);
                int i = bundle.getInt("sysui_color", 0);
                boolean z2 = bundle.getBoolean("animate_transition", true);
                boolean z3 = bundle.getBoolean("card_forces_scrim", false);
                mGlowController.setCardVisible(z);
                mGlowController.getScrimController().setCardVisible(z, z2, z3);
                mLightnessProvider.setCardVisible(z, i);
                mTranscriptionController.setCardVisible(z);
                break;
            case 3:
                boolean z4 = mNgaPresent;
                mNgaPresent = bundle.getBoolean("is_available", false);
                mTouchOutsidePendingIntent = bundle.getParcelable("touch_outside");
                mTouchInsidePendingIntent = bundle.getParcelable("touch_inside");
                mTaskStackNotifier.setIntent(bundle.getParcelable("task_stack_changed"));
                if (z4 && !mNgaPresent) {
                    hide();
                    break;
                }
            case 4:
                ValueAnimator valueAnimator = mInvocationAnimator;
                if (valueAnimator == null || !valueAnimator.isStarted()) {
                    if (Objects.equals(bundle.get("state"), "FULL_LISTENING") && !mShowingAssistUi) {
                        mInvocationInProgress = true;
                        onInvocationProgress(0, 1.0f);
                        mPendingEdgeLightsBundle = bundle;
                        break;
                    } else {
                        mPendingEdgeLightsBundle = null;
                        mEdgeLightsController.setState(bundle);
                        break;
                    }
                } else {
                    mPendingEdgeLightsBundle = bundle;
                    break;
                }
            case 5:
                mTranscriptionController.setTranscription(bundle.getString("text"), bundle.getParcelable("tap_action"));
                mTranscriptionController.setTranscriptionColor(bundle.getInt("text_color", 0));
                break;
            case 6:
                mTranscriptionController.setGreeting(bundle.getString("text"), mFlingVelocity, bundle.getParcelable("tap_action"));
                break;
            case 7:
                mTranscriptionController.setChips(bundle.getParcelableArrayList("chips"));
                break;
            case 8:
                mTranscriptionController.clear(bundle.getBoolean("show_animation", true));
                break;
            case 9:
                Intent intent = bundle.getParcelable("intent");
                if (intent != null) {
                    SysUiServiceProvider.getComponent(mContext, StatusBar.class).startActivity(intent, bundle.getBoolean("dismiss_shade"));
                    break;
                } else {
                    Log.e("NgaUiController", "Null intent; cannot start activity");
                    return;
                }
            case 10:
                mKeyboardRequested = true;
                mKeyboardIntent = bundle.getParcelable("tap_action");
                refresh();
                break;
            case 11:
                mKeyboardRequested = false;
                mKeyboardIntent = null;
                refresh();
                break;
            case 12:
                mZeroStateRequested = true;
                mZeroStateIntent = bundle.getParcelable("tap_action");
                refresh();
                break;
            case 13:
                mZeroStateRequested = false;
                mZeroStateIntent = null;
                refresh();
                break;
            default:
                Log.w("NgaUiController", String.format("Unknown action \"%s\"; cannot process RPC", string));
                break;
        }
        if (!"config".equals(string)) {
            mNgaPresent = true;
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
    
    public void refresh() {
        if (mDidSetParent) {
            updateShowingKeyboard();
            updateShowingZeroState();
            updateShowingAssistUi();
            updateShowingNavBar();
        }
    }

    private void updateShowingKeyboard() {
        if (!mKeyboardRequested) {
            mKeyboardIcon.hide();
        } else if (mHaveAccurateLuma) {
            mKeyboardIcon.show(mKeyboardIntent);
        }
    }

    private void updateShowingZeroState() {
        if (!mZeroStateRequested) {
            mZeroStateIcon.hide();
        } else if (mHaveAccurateLuma) {
            mZeroStateIcon.show(mZeroStateIntent);
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
                        obj = string.length();
                    }
                    objArr[0] = obj;
                    sb.append(String.format("transcription length = %s\n", objArr));
                } else if ("chips".equals(next)) {
                    sb.append(String.format("%s =\n", "chips"));
                    ArrayList parcelableArrayList = bundle.getParcelableArrayList("chips");
                    if (parcelableArrayList != null) {
                        for (Object o : parcelableArrayList) {
                            Bundle bundle2 = (Bundle) o;
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
            if (!"transcription".equals(bundle.get("action")) || !mTranscriptionController.isTranscribing()) {
                Log.i("NgaUiController", sb.toString());
            } else {
                Log.v("NgaUiController", sb.toString());
            }
        }
    }

    
    public void setHasDarkBackground(boolean z) {
        String str = "dark";
        if (mHasDarkBackground != z) {
            mHasDarkBackground = z;
            if (VERBOSE) {
                StringBuilder sb = new StringBuilder();
                sb.append("switching to ");
                if (!mHasDarkBackground) {
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
        if (mDidSetParent) {
            mTranscriptionController.setHasDarkBackground(mHasDarkBackground);
            mKeyboardIcon.setHasDarkBackground(mHasDarkBackground);
            mPromptView.setHasDarkBackground(mHasDarkBackground);
            mKeyboardIcon.setHasDarkBackground(mHasDarkBackground);
            mZeroStateIcon.setHasDarkBackground(mHasDarkBackground);
        }
    }

    private void setViewParent() {
        if (!mDidSetParent) {
            ViewGroup parent = mUiHost.getParent();
            if (parent == null) {
                Log.e("NgaUiController", "Status bar view unavailable!");
                return;
            }
            // TODO: Investigate whether we need to create a specific class for or not. And eventually change name.
            Runnable r8 = () -> lambda$setViewParent$4$NgaUiController();

            LayoutInflater from = LayoutInflater.from(mContext);
            mGlowController = new GlowController(mContext, parent, mLightnessProvider, new VisibilityListener() {

                public void onVisibilityChanged(int i) {
                    lambda$setViewParent$5$NgaUiController(i);
                }
            }, r8);
            mInvocationLightsView = (AssistantInvocationLightsView) from.inflate(R.layout.invocation_lights, parent, false);
            mInvocationLightsView.setGoogleAssistant(true);
            parent.addView(mInvocationLightsView);
            mEdgeLightsController = new EdgeLightsController(mContext, parent);
            mEdgeLightsController.addListener(mGlowController);
            mTranscriptionController = new TranscriptionController(mContext, parent, r8);
            mTranscriptionController.setListener(mGlowController.getScrimController());
            mKeyboardIcon = (KeyboardIconView) from.inflate(R.layout.keyboard_icon, parent, false);
            parent.addView(mKeyboardIcon);
            mZeroStateIcon = (ZeroStateIconView) LayoutInflater.from(mContext).inflate(R.layout.zerostate_icon, parent, false);
            parent.addView(mZeroStateIcon);
            mPromptView = (PromptView) from.inflate(R.layout.assist_prompt, parent, false);
            parent.addView(mPromptView);
            mUiHost.addMarginListener(mTranscriptionController);
            mUiHost.addMarginListener(mKeyboardIcon);
            mUiHost.addMarginListener(mZeroStateIcon);
            mUiHost.addMarginListener(mPromptView);
            mDidSetParent = true;
            dispatchHasDarkBackground();
            if (VERBOSE) {
                Log.v("NgaUiController", "Added UI");
            }
        }
    }

    public void lambda$setViewParent$4$NgaUiController() {
        PendingIntent pendingIntent = mTouchInsidePendingIntent;
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

    public void lambda$setViewParent$5$NgaUiController(int i) {
        boolean isScrimVisible = mGlowController.isScrimVisible();
        if (mScrimVisible != isScrimVisible) {
            mScrimVisible = isScrimVisible;
            refresh();
        }
        if (mGlowVisibility != i) {
            mGlowVisibility = i;
            refresh();
        }
    }

    private void closeNgaUi() {
        Dependency.get(AssistManager.class).hideAssist();
        hide();
    }

    public void hide() {
        ValueAnimator valueAnimator = mInvocationAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            mInvocationAnimator.cancel();
        }
        mInvocationInProgress = false;
        mTranscriptionController.clear(false);
        mEdgeLightsController.setState(new Gone());
        mPendingEdgeLightsBundle = null;
        mPromptView.disable();
        mZeroStateRequested = false;
        mKeyboardRequested = false;
        refresh();
    }

    private void setColorMonitoringState(boolean z) {
        if (mIsMonitoringColor != z) {
            if (!z || mGlowController.getScrimSurfaceControllerHandle() != null) {
                mIsMonitoringColor = z;
                if (mIsMonitoringColor) {
                    int rotatedHeight = (DisplayUtils.getRotatedHeight(mContext) -
                            ((int) mContext.getResources().getDimension(R.dimen.transcription_space_bottom_margin))) -
                            DisplayUtils.convertSpToPx(20.0f, mContext);
                    Rect rect = new Rect(0, rotatedHeight - DisplayUtils.convertDpToPx(160.0f, mContext), DisplayUtils.getRotatedWidth(mContext), rotatedHeight);
                    mColorMonitoringStart = SystemClock.elapsedRealtime();
                    mLightnessProvider.enableColorMonitoring(true, rect, mGlowController.getScrimSurfaceControllerHandle());
                    return;
                }
                mLightnessProvider.enableColorMonitoring(false, null, null);
                mHaveAccurateLuma = false;
                mGlowController.getScrimController().onLightnessInvalidated();
                mTranscriptionController.setHasAccurateBackground(false);
            }
        }
    }

    private void updateShowingAssistUi() {
        boolean z = !(mEdgeLightsController.getMode() instanceof Gone) || mGlowController.isVisible()
                || mInvocationInProgress || mKeyboardIcon.getVisibility() == View.VISIBLE || mZeroStateIcon.getVisibility() == View.VISIBLE;
        setColorMonitoringState(z);
        if (mShowingAssistUi != z) {
            mShowingAssistUi = z;
            SysUiServiceProvider.getComponent(mContext, ScreenDecorations.class).setAssistHintBlocked(z);
            if (mShowingAssistUi) {
                mWakeLock.acquire();
                mUiHost.getParent().getViewTreeObserver().addOnComputeInternalInsetsListener(this);
            } else {
                mWakeLock.release();
                mUiHost.getParent().getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
                ValueAnimator valueAnimator = mInvocationAnimator;
                if (valueAnimator != null && valueAnimator.isStarted()) {
                    mInvocationAnimator.cancel();
                }
            }
        }
        mEdgeLightsController.getMode();
        mUiHost.setAssistState( mShowingAssistUi, false);
    }

    private void updateShowingNavBar() {
        EdgeLightsView.Mode mode = mEdgeLightsController.getMode();
        boolean z = !mInvocationInProgress && ((mode instanceof Gone) || (mode instanceof HalfListening));
        float f = z ? 1.0f : 0.0f;
        NavigationBarView defaultNavigationBarView = Dependency.get(NavigationBarController.class).getDefaultNavigationBarView();
        if (defaultNavigationBarView != null) {
            float alpha = defaultNavigationBarView.getAlpha();
            if (f != alpha && f != mNavBarDestinationAlpha) {
                mNavBarDestinationAlpha = f;
                ValueAnimator valueAnimator = mNavBarAlphaAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                }
                mNavBarAlphaAnimator = ObjectAnimator.ofFloat(defaultNavigationBarView, View.ALPHA, alpha, f).setDuration((long) Math.abs((f - alpha) * 80.0f));
                if (z) {
                    mNavBarAlphaAnimator.setStartDelay(80);
                }
                mNavBarAlphaAnimator.start();
            }
        }
    }

    private float getAnimationProgress(int i, float f) {
        return i == 2 ? f * 0.95f : mProgressInterpolator.getInterpolation(f * 0.8f);
    }

    public void onInvocationProgress(int type, float progress) {
        ValueAnimator valueAnimator = mInvocationAnimator;
        if (valueAnimator == null || !valueAnimator.isStarted()) {
            setViewParent();
            if (mDidSetParent) {
                if (!mEdgeLightsController.getMode().preventsInvocations()) {
                    boolean z = mInvocationInProgress;
                    int i2 = (Float.compare(progress, 1.0f));
                    if (i2 < 0) {
                        mLastInvocationProgress = progress;
                        if (!z && progress > 0.0f) {
                            mLastInvocationStartTime = SystemClock.uptimeMillis();
                        }
                        mInvocationInProgress = progress > 0.0f;
                        if (!mInvocationInProgress) {
                            mPromptView.disable();
                        } else if (progress < 0.9f && SystemClock.uptimeMillis() - mLastInvocationStartTime > 200) {
                            mPromptView.enable();
                        }
                        setProgress(type, getAnimationProgress(type, progress));
                    } else {
                        ValueAnimator valueAnimator2 = mInvocationAnimator;
                        if (valueAnimator2 == null || !valueAnimator2.isStarted()) {
                            mFlingVelocity = 0.0f;
                            completeInvocation(type);
                        }
                    }
                    logInvocationProgressMetrics(type, progress, z);
                } else if (VERBOSE) {
                    Log.v("NgaUiController", "ignoring invocation; mode is " + mEdgeLightsController.getMode().getClass().getSimpleName());
                }
            }
        } else {
            Log.w("NgaUiController", "Already animating; ignoring invocation progress");
        }
    }

    private static void logInvocationProgressMetrics(int i, float f, boolean z) {
        if (!z && f > 0.0f) {
            MetricsLogger.action(new LogMaker(1716).setType(4).setSubtype(Dependency.get(AssistManager.class).toLoggingSubType(i)));
        }
        if (z && f == 0.0f) {
            MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(1));
        }
    }

    public void onGestureCompletion(float velocity) {
        setViewParent();
        if (mDidSetParent) {
            if (!mEdgeLightsController.getMode().preventsInvocations()) {
                mFlingVelocity = velocity;
                completeInvocation(1);
            } else if (VERBOSE) {
                Log.v("NgaUiController", "ignoring invocation; mode is " + mEdgeLightsController.getMode().getClass().getSimpleName());
            }
        }
    }

    private void setProgress(int type, float progress) {
        mInvocationLightsView.onInvocationProgress(
                mProgressInterpolator.getInterpolation(progress));
        mGlowController.setInvocationProgress(progress);
        mGlowController.getScrimController().setInvocationProgress(progress);
        mPromptView.onInvocationProgress(type, progress);
        updateShowingNavBar();
        refresh();
    }


    public void lambda$completeInvocation$7$NgaUiController(int i, OvershootInterpolator overshootInterpolator, ValueAnimator valueAnimator) {
        setProgress(i, overshootInterpolator.getInterpolation((Float) valueAnimator.getAnimatedValue()));
    }

    private void completeInvocation(int i) {
        if (!mNgaPresent) {
            setProgress(i, 0.0f);
            resetInvocationProgress();
            return;
        }
        mUiHandler.removeCallbacks(mSessionTimeout);
        mUiHandler.postDelayed(mSessionTimeout, INVOCATION_TIMEOUT_MS);
        mPromptView.disable();
        ValueAnimator valueAnimator = mInvocationAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            mInvocationAnimator.cancel();
        }
        float f = mFlingVelocity;
        float f2 = 3.0f;
        if (f != 0.0f) {
            f2 = MathUtils.constrain((-f) / 1.45f, 3.0f, 12.0f);
        }
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(f2);

        ValueAnimator ofFloat = ValueAnimator.ofFloat(approximateInverse(getAnimationProgress(i, mLastInvocationProgress), (v) -> Math.min(1.0f, overshootInterpolator.getInterpolation(v))));
        ofFloat.setDuration(600);
        ofFloat.setStartDelay(1);
        ofFloat.addUpdateListener(listener -> {
            setProgress(i, overshootInterpolator.getInterpolation((Float) listener.getAnimatedValue()));
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled = false;

            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (!mCancelled) {
                    if (mPendingEdgeLightsBundle != null) {
                        mEdgeLightsController.setState(mPendingEdgeLightsBundle);
                    } else {
                        mEdgeLightsController.setState(new FullListening(mContext));
                    }
                    Bundle unused = mPendingEdgeLightsBundle = null;
                }
                mUiHandler.post(() -> lambda$onAnimationEnd$0$NgaUiController$2());
            }

            public void lambda$onAnimationEnd$0$NgaUiController$2() {
                resetInvocationProgress();
            }
        });

        mInvocationAnimator = ofFloat;
        mInvocationAnimator.start();
    }

    
    public void resetInvocationProgress() {
        mInvocationInProgress = false;
        mInvocationLightsView.hide();
        mLastInvocationProgress = 0.0f;
        mGlowController.getScrimController().setInvocationProgress(0.0f);
        refresh();
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        Region region = new Region();
        if (mKeyboardIcon.getVisibility() == View.VISIBLE) {
            Rect rect = new Rect();
            mKeyboardIcon.getBoundsOnScreen(rect);
            region.union(rect);
        }
        if (mZeroStateIcon.getVisibility() == View.VISIBLE) {
            Rect rect2 = new Rect();
            mZeroStateIcon.getBoundsOnScreen(rect2);
            region.union(rect2);
        }
        Rect touchableRegion = mGlowController.getTouchableRegion();
        Rect touchableRegion2 = mTranscriptionController.getTouchableRegion();
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
        if (Looper.myLooper() != mUiHandler.getLooper()) {
            mUiHandler.post(() -> onDozingChanged(isDozing));
        } else {
            mGlowController.getScrimController().setIsDozing(isDozing);
            if (isDozing && mShowingAssistUi) {
                closeNgaUi();
            }
        }
    }

    public void onDensityOrFontScaleChanged() {
        mKeyboardIcon.onDensityChanged();
        mZeroStateIcon.onDensityChanged();
    }
}


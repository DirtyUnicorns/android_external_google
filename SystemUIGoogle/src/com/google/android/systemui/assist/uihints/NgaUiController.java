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
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsController;
import com.google.android.systemui.assist.uihints.edgelights.EdgeLightsView;
import com.google.android.systemui.assist.uihints.edgelights.mode.FullListening;
import com.google.android.systemui.assist.uihints.edgelights.mode.Gone;
import com.google.android.systemui.assist.uihints.edgelights.mode.HalfListening;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class NgaUiController implements AssistManager.UiController, ViewTreeObserver.OnComputeInternalInsetsListener, StatusBarStateController.StateListener, ConfigurationController.ConfigurationListener {

    private static final long INVOCATION_TIMEOUT_MS;
    private static final long SESSION_TIMEOUT_MS;
    private static final boolean VERBOSE;
    private static final PathInterpolator mProgressInterpolator;
    private long mColorMonitoringStart;
    private final Context mContext;
    private boolean mDidSetParent;
    private EdgeLightsController mEdgeLightsController;
    private float mFlingVelocity;
    private GlowController mGlowController;
    private int mGlowVisibility;
    private boolean mHasDarkBackground;
    private boolean mHaveAccurateLuma;
    private ValueAnimator mInvocationAnimator;
    private boolean mInvocationInProgress;
    private AssistantInvocationLightsView mInvocationLightsView;
    private boolean mIsMonitoringColor;
    private KeyboardIconView mKeyboardIcon;
    private PendingIntent mKeyboardIntent;
    private boolean mKeyboardRequested;
    private float mLastInvocationProgress;
    private long mLastInvocationStartTime;
    private final LightnessProvider mLightnessProvider;
    private ValueAnimator mNavBarAlphaAnimator;
    private float mNavBarDestinationAlpha;
    private boolean mNgaPresent;
    private Bundle mPendingEdgeLightsBundle;
    private PromptView mPromptView;
    private boolean mScrimVisible;
    private Runnable mSessionTimeout;
    private boolean mShowingAssistUi;
    private float mSpeechConfidence;
    private final TaskStackNotifier mTaskStackNotifier;
    private PendingIntent mTouchInsidePendingIntent;
    private PendingIntent mTouchOutsidePendingIntent;
    private TranscriptionController mTranscriptionController;
    private final Handler mUiHandler;
    private final OverlayUiHost mUiHost;
    private PowerManager.WakeLock mWakeLock;
    private ZeroStateIconView mZeroStateIcon;
    private PendingIntent mZeroStateIntent;
    private boolean mZeroStateRequested;
    
    static {
        VERBOSE = (Build.TYPE.toLowerCase(Locale.ROOT).contains("debug") || Build.TYPE.toLowerCase(Locale.ROOT).equals("eng"));
        SESSION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10L);
        INVOCATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(3L);
        mProgressInterpolator = new PathInterpolator(0.83f, 0.0f, 0.84f, 1.0f);
    }

    @SuppressLint("InvalidWakeLockTag")
    public NgaUiController(Context context) {
        mUiHandler = new Handler(Looper.getMainLooper());
        mDidSetParent = false;
        mNgaPresent = false;
        mHasDarkBackground = false;
        mIsMonitoringColor = false;
        mInvocationInProgress = false;
        mSpeechConfidence = 0.0f;
        mScrimVisible = false;
        mShowingAssistUi = false;
        mHaveAccurateLuma = false;
        mGlowVisibility = 8;
        mLastInvocationStartTime = 0;
        mLastInvocationProgress = 0.0f;
        mFlingVelocity = 0.0f;
        mPendingEdgeLightsBundle = null;
        mColorMonitoringStart = 0;
        mSessionTimeout = new Runnable() {
            public final void run() {
                if (mShowingAssistUi) {
                    Log.e("NgaUiController", "Timed out");
                    closeNgaUi();
                    MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(4));
                }
            }
        };
        mLightnessProvider = new LightnessProvider(new LightnessListener() {
            public void onLightnessUpdate(float f) {
                if (mColorMonitoringStart > 0) {
                    long elapsedRealtime = SystemClock.elapsedRealtime() - mColorMonitoringStart;
                    if (VERBOSE) {
                        Log.d("NgaUiController", "Got lightness update (" + f + ") after " + elapsedRealtime + " ms");
                    }
                    mColorMonitoringStart = 0;
                }
                mHaveAccurateLuma = true;
                mGlowController.setHasMedianLightness(f);
                mTranscriptionController.setHasAccurateBackground(true);
                NgaUiController ngaUiController = NgaUiController.this;
                setHasDarkBackground(!(f > getDarkUiThreshold()));
                refresh();
            }
        });
        mKeyboardRequested = false;
        mKeyboardIntent = null;
        mZeroStateRequested = false;
        mZeroStateIntent = null;
        mNavBarDestinationAlpha = -1.0f;
        mTaskStackNotifier = new TaskStackNotifier();
        mContext = context;
        mUiHost = new OverlayUiHost(context, () -> {
            closeNgaUi();
            MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(3));
        }, () -> {
            if (mTouchOutsidePendingIntent != null) {
                try {
                    mTouchOutsidePendingIntent.send();
                } catch (PendingIntent.CanceledException unused) {
                    Log.w("NgaUiController", "Touch outside PendingIntent canceled");
                }
            }
        });
        mWakeLock = ((PowerManager) Objects.requireNonNull(context.getSystemService(Context.POWER_SERVICE)))
                .newWakeLock(805306378, "Assist (NGA)");
        setViewParent();
        Dependency.get(StatusBarStateController.class).addCallback(this);
        Dependency.get(ConfigurationController.class).addCallback(this);
        refresh();
    }

    public static float getDarkUiThreshold() {
        return 0.4f;
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
        int n = -1;
        switch (string.hashCode()) {
            case -2051025175:
                if (string.equals("show_keyboard")) {
                    n = 10;
                    break;
                }
                break;
            case -2040419289:
                if (string.equals("show_zerostate")) {
                    n = 12;
                    break;
                }
                break;
            case -1354792126:
                if (string.equals("config")) {
                    n = 3;
                    break;
                }
                break;
            case -1160605116:
                if (string.equals("hide_keyboard")) {
                    n = 11;
                    break;
                }
                break;
            case -241763182:
                if (string.equals("transcription")) {
                    n = 5;
                    break;
                }
                break;
            case -207201236:
                if (string.equals("hide_zerostate")) {
                    n = 13;
                    break;
                }
                break;
            case 3046160:
                if (string.equals("card")) {
                    n = 2;
                    break;
                }
                break;
            case 94631335:
                if (string.equals("chips")) {
                    n = 7;
                    break;
                }
                break;
            case 94746189:
                if (string.equals("clear")) {
                    n = 8;
                    break;
                }
                break;
            case 205422649:
                if (string.equals("greeting")) {
                    n = 6;
                    break;
                }
                break;
            case 371207756:
                if (string.equals("start_activity")) {
                    n = 9;
                    break;
                }
                break;
            case 771587807:
                if (string.equals("edge_lights")) {
                    n = 4;
                    break;
                }
                break;
            case 1549039479:
                if (string.equals("audio_info")) {
                    n = 1;
                    break;
                }
                break;
            case 1642639251:
                if (string.equals("keep_alive")) {
                    n = 0;
                    break;
                }
                break;
        }
        switch (n) {
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
                mNgaPresent = bundle.getBoolean("is_available", false);
                mTouchOutsidePendingIntent = bundle.getParcelable("touch_outside");
                mTouchInsidePendingIntent = bundle.getParcelable("touch_inside");
                mTaskStackNotifier.setIntent(bundle.getParcelable("task_stack_changed"));
                if (!mNgaPresent) {
                    hide();
                }
                break;
            case 4:
                if (mInvocationAnimator != null && mInvocationAnimator.isStarted()) {
                    mPendingEdgeLightsBundle = bundle;
                    break;
                }
                if (Objects.equals(bundle.get("state"), "FULL_LISTENING") && !mShowingAssistUi) {
                    mInvocationInProgress = true;
                    onInvocationProgress(0, 1.0f);
                    mPendingEdgeLightsBundle = bundle;
                    break;
                }
                mPendingEdgeLightsBundle = null;
                mEdgeLightsController.setState(bundle);
                break;
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
                    ((SystemUIApplication) mContext.getApplicationContext()).getComponent(StatusBar.class).startActivity(intent, bundle.getBoolean("dismiss_shade"));
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
        return !"config".equals(bundle.getString("action")) || bundle.getBoolean("is_available", false);
    }
    
    public void refresh() {
        if (!mDidSetParent) {
            return;
        }
        updateShowingKeyboard();
        updateShowingZeroState();
        updateShowingAssistUi();
        updateShowingNavBar();
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
        if (bundle == null) {
            Log.w("NgaUiController", "Received null bundle!");
            return;
        }
        if (VERBOSE) {
            if (!"audio_info".equals(bundle.get("action"))) {
                StringBuilder sb = new StringBuilder();
                sb.append("<UiHintsBundle>\n");
                for (String s : bundle.keySet()) {
                    if ("text".equals(s)) {
                        String string = bundle.getString(s);
                        Serializable value = string == null ? "NULL" : string.length();
                        sb.append(String.format("transcription length = %s\n", value));
                    } else if ("chips".equals(s)) {
                        sb.append(String.format("%s =\n", "chips"));
                        ArrayList<Bundle> parcelableArrayList = bundle.getParcelableArrayList("chips");
                        if (parcelableArrayList == null) {
                            continue;
                        }
                        for (Bundle bundle2 : parcelableArrayList) {
                            for (final String s2 : bundle2.keySet()) {
                                sb.append(String.format("\t%s = %s\n", s2, bundle2.get(s2)));
                            }
                        }
                        sb.append("\n");
                    } else {
                        sb.append(String.format("%s = %s\n", s, bundle.get(s)));
                    }
                }
                sb.append("</UiHintsBundle>");
                if ("transcription".equals(bundle.get("action")) && mTranscriptionController.isTranscribing()) {
                    Log.v("NgaUiController", sb.toString());
                }
                else {
                    Log.i("NgaUiController", sb.toString());
                }
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
            mPromptView.setHasDarkBackground(mHasDarkBackground);
            mKeyboardIcon.setHasDarkBackground(mHasDarkBackground);
            mZeroStateIcon.setHasDarkBackground(mHasDarkBackground);
        }
    }

    private void setViewParent() {
        if (mDidSetParent) {
            return;
        }
        ViewGroup parent = mUiHost.getParent();
        if (parent == null) {
            Log.e("NgaUiController", "Status bar view unavailable!");
            return;
        }
        // TODO: Investigate whether we need to create a specific class for or not.
        Runnable r8 = () -> sendTouchInsidePendingIntent();

        LayoutInflater from = LayoutInflater.from(mContext);
        mGlowController = new GlowController(mContext, parent, mLightnessProvider, i -> {
            boolean isScrimVisible = mGlowController.isScrimVisible();
            if (mScrimVisible != isScrimVisible) {
                mScrimVisible = isScrimVisible;
                refresh();
            }
            if (mGlowVisibility != i) {
                mGlowVisibility = i;
                refresh();
            }
        }, this::sendTouchInsidePendingIntent);
        mInvocationLightsView = (AssistantInvocationLightsView) from.inflate(R.layout.invocation_lights, parent, false);
        mInvocationLightsView.setGoogleAssistant(true);
        parent.addView(mInvocationLightsView);
        mEdgeLightsController = new EdgeLightsController(mContext, parent);
        mEdgeLightsController.addListener(mGlowController);
        mTranscriptionController = new TranscriptionController(mContext, parent, this::sendTouchInsidePendingIntent);
        mTranscriptionController.setListener(mGlowController.getScrimController());
        mKeyboardIcon = (KeyboardIconView) from.inflate(R.layout.keyboard_icon, parent, false);
        parent.addView(mKeyboardIcon);
        mZeroStateIcon = (ZeroStateIconView) from.inflate(R.layout.zerostate_icon, parent, false);
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

    private void sendTouchInsidePendingIntent() {
        if (mTouchInsidePendingIntent != null) {
            try {
                mTouchInsidePendingIntent.send();
            } catch (PendingIntent.CanceledException unused) {
                Log.w("NgaUiController", "Touch outside PendingIntent canceled");
                closeNgaUi();
            }
        } else {
            closeNgaUi();
        }
        MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(2));
    }

    private void closeNgaUi() {
        Dependency.get(AssistManager.class).hideAssist();
        hide();
    }

    public void hide() {
        if (mInvocationAnimator != null && mInvocationAnimator.isStarted()) {
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
                || mInvocationInProgress || mKeyboardIcon.getVisibility() == 0 || mZeroStateIcon.getVisibility() == 0;
        setColorMonitoringState(z);
        if (mShowingAssistUi != z) {
            mShowingAssistUi = z;
            ((SystemUIApplication) mContext.getApplicationContext()).getComponent(ScreenDecorations.class).setAssistHintBlocked(z);
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
        mUiHost.setAssistState(mShowingAssistUi, mEdgeLightsController.getMode() instanceof FullListening);
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
                if (mNavBarAlphaAnimator != null) {
                    mNavBarAlphaAnimator.cancel();
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

    @Override
    public void onInvocationProgress(int type, float progress) {
        if (mInvocationAnimator != null && mInvocationAnimator.isStarted()) {
            Log.w("NgaUiController", "Already animating; ignoring invocation progress");
            return;
        }
        setViewParent();
        if (!mDidSetParent) {
            return;
        }
        if (mEdgeLightsController.getMode().preventsInvocations()) {
            if (VERBOSE) {
                String sb = "ignoring invocation; mode is " +
                        mEdgeLightsController.getMode().getClass().getSimpleName();
                Log.v("NgaUiController", sb);
            }
            return;
        }
        final float n2 = Float.compare(progress, 1.0f);
        if (n2 < 0) {
            mLastInvocationProgress = progress;
            if (!mInvocationInProgress && progress > 0.0f) {
                mLastInvocationStartTime = SystemClock.uptimeMillis();
            }
            if (!(mInvocationInProgress = (progress > 0.0f && n2 < 0))) {
                mPromptView.disable();
            }
            else if (progress < 0.9f && SystemClock.uptimeMillis() - mLastInvocationStartTime > 200) {
                mPromptView.enable();
            }
            setProgress(type, getAnimationProgress(type, progress));
        }
        else {
            if (mInvocationAnimator == null || !mInvocationAnimator.isStarted()) {
                mFlingVelocity = 0.0f;
                completeInvocation(type);
            }
        }
        logInvocationProgressMetrics(type, progress, mInvocationInProgress);
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
        if (!mDidSetParent) {
            return;
        }
        if (mEdgeLightsController.getMode().preventsInvocations()) {
            if (VERBOSE) {
                String sb = "ignoring invocation; mode is " +
                        mEdgeLightsController.getMode().getClass().getSimpleName();
                Log.v("NgaUiController", sb);
            }
            return;
        }
        mFlingVelocity = velocity;
        completeInvocation(1);
    }

    private void setProgress(int type, float progress) {
        mInvocationLightsView.onInvocationProgress(progress);
        mGlowController.setInvocationProgress(progress);
        mGlowController.getScrimController().setInvocationProgress(progress);
        mPromptView.onInvocationProgress(type, progress);
        updateShowingNavBar();
        refresh();
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
        if (mInvocationAnimator != null && mInvocationAnimator.isStarted()) {
            mInvocationAnimator.cancel();
        }
        float f = mFlingVelocity;
        float f2 = 3.0f;
        if (f != 0.0f) {
            f2 = MathUtils.constrain((-f) / 1.45f, 3.0f, 12.0f);
        }
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(f2);

        ValueAnimator ofFloat = ValueAnimator.ofFloat(approximateInverse(getAnimationProgress(i, mLastInvocationProgress),
                (v) -> Math.min(1.0f, overshootInterpolator.getInterpolation(v))));
        ofFloat.setDuration(600);
        ofFloat.setStartDelay(1);
        ofFloat.addUpdateListener(listener ->
            setProgress(i, overshootInterpolator.getInterpolation((Float) mInvocationAnimator.getAnimatedValue())));
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
                }
                mUiHandler.post(() -> resetInvocationProgress());
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
            arrayList.add(function.apply(f2));
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

    @Override
    public void onDensityOrFontScaleChanged() {
        mKeyboardIcon.onDensityChanged();
        mZeroStateIcon.onDensityChanged();
    }
}


package com.google.android.settings.aware;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DeviceConfig;
import android.provider.Settings;

import com.android.settings.aware.AwareFeatureProvider;
import com.android.settings.overlay.FeatureFactory;

public class AwareHelper {

    public final Context mContext;
    private final String SHARE_PERFS = "aware_settings";
    private final AwareFeatureProvider mFeatureProvider;
    private final SettingsObserver mSettingsObserver;

    public interface Callback {
        void onChange(Uri uri);
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri AIRPLANE_MODE = Settings.Global.getUriFor("airplane_mode_on");
        private final Uri AWARE_ALLOWED = Settings.Global.getUriFor("aware_allowed");
        private final Uri AWARE_ENABLED = Settings.Secure.getUriFor("aware_enabled");
        private Callback mCallback;

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void setCallback(Callback callback) {
            mCallback = callback;
        }

        public void observe() {
            ContentResolver contentResolver = mContext.getContentResolver();
            contentResolver.registerContentObserver(AWARE_ENABLED, false, this);
            contentResolver.registerContentObserver(AWARE_ALLOWED, false, this);
            contentResolver.registerContentObserver(AIRPLANE_MODE, false, this);
        }

        public void onChange(boolean z, Uri uri) {
            Callback callback = mCallback;
            if (callback != null) {
                callback.onChange(uri);
            }
        }
    }

    public AwareHelper(Context context) {
        mContext = context;
        mSettingsObserver = new SettingsObserver(new Handler(Looper.getMainLooper()));
        mFeatureProvider = FeatureFactory.getFactory(mContext).getAwareFeatureProvider();
    }

    public boolean isGestureConfigurable() {
        return mFeatureProvider.isSupported(mContext) && mFeatureProvider.isEnabled(mContext)
                && !isAirplaneModeOn();
    }

    public boolean isAirplaneModeOn() {
        return Settings.Global.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    public boolean isSupported() {
        return mFeatureProvider.isSupported(mContext);
    }

    public boolean isEnabled() {
        return mFeatureProvider.isEnabled(mContext);
    }

    public void register(Callback callback) {
        mSettingsObserver.observe();
        mSettingsObserver.setCallback(callback);
    }

    public void unregister() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    public static boolean isTapAvailableOnTheDevice() {
        return DeviceConfig.getBoolean("oslo", "enable_tap", true);
    }

    public void writeFeatureEnabled(String str, boolean z) {
        mContext.getSharedPreferences(SHARE_PERFS, 0).edit().putBoolean(str, z).apply();
    }

    public boolean readFeatureEnabled(String str) {
        return mContext.getSharedPreferences(SHARE_PERFS, 0).getBoolean(str, true);
    }
}

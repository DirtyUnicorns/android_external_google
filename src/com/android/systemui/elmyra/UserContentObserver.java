package com.google.android.systemui.elmyra;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import java.util.function.Consumer;

public class UserContentObserver extends ContentObserver {
    private final Consumer<Uri> mCallback;
    private final Context mContext;
    private final Uri mSettingsUri;
    private final SynchronousUserSwitchObserver mUserSwitchCallback;

    class UserSwitchCallback extends SynchronousUserSwitchObserver {
        UserSwitchCallback() {
        }

        public void onUserSwitching(int i) throws RemoteException {
            updateContentObserver();
            mCallback.accept(mSettingsUri);
        }
    }

    public UserContentObserver(Context context, Uri uri, Consumer<Uri> consumer) {
        this(context, uri, consumer, true);
    }

    public UserContentObserver(Context context, Uri uri, Consumer<Uri> consumer, boolean enabled) {
        super(new Handler(context.getMainLooper()));
        mUserSwitchCallback = new UserSwitchCallback();
        mContext = context;
        mSettingsUri = uri;
        mCallback = consumer;
        if (enabled) {
            activate();
        }
    }

    private void updateContentObserver() {
        mContext.getContentResolver().unregisterContentObserver(this);
        mContext.getContentResolver().registerContentObserver(mSettingsUri, false, this, -2);
    }

    public void activate() {
        updateContentObserver();
        try {
            ActivityManager.getService().registerUserSwitchObserver(mUserSwitchCallback, "Elmyra/UserContentObserver");
        } catch (Throwable suppressed) { /* do nothing */ }
    }

    public void deactivate() {
        this.mContext.getContentResolver().unregisterContentObserver(this);
        try {
            ActivityManager.getService().unregisterUserSwitchObserver(mUserSwitchCallback);
        } catch (Throwable suppressed) { /* do nothing */ }
    }

    public void onChange(boolean selfChange, Uri uri) {
        mCallback.accept(uri);
    }
}

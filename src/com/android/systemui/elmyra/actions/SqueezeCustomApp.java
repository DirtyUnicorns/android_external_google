package com.google.android.systemui.elmyra.actions;

import android.provider.Settings;
import android.view.View;
import android.widget.ListView;

import com.dirtyunicorns.support.preferences.AppPicker;

public class SqueezeCustomApp extends AppPicker {

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String mAppString = applist.get(position).packageName;
        String mFriendlyAppString = (String) applist.get(position).loadLabel(packageManager);

        Settings.Secure.putString(
                getContentResolver(), Settings.Secure.SQUEEZE_CUSTOM_APP, mAppString);
        Settings.Secure.putString(
                getContentResolver(), Settings.Secure.SQUEEZE_CUSTOM_APP_FR_NAME,
                mFriendlyAppString);
        finish();
    }
}

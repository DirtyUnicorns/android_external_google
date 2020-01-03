// package com.google.android.systemui.elmyra.actions;

// import android.content.pm.ActivityInfo;
// import android.os.UserHandle;
// import android.provider.Settings;
// import android.view.View;
// import android.widget.ListView;

// import com.dirtyunicorns.support.preferences.AppPicker;

// public class ShortSqueezeCustomApp extends AppPicker {

//     @Override
//     protected void onListItemClick(ListView l, View v, int position, long id) {
//         if (!mIsActivitiesList) {
//             // we are in the Apps list
//             String packageName = applist.get(position).packageName;
//             String friendlyAppString = (String) applist.get(position).loadLabel(packageManager);
//             setPackage(packageName, friendlyAppString);
//             setPackageActivity(null);
//         } else if (mIsActivitiesList) {
//             // we are in the Activities list
//             setPackageActivity(mActivitiesList.get(position));
//         }

//         mIsActivitiesList = false;
//         finish();
//     }

//     @Override
//     protected void onLongClick(int position) {
//         if (mIsActivitiesList) return;
//         String packageName = applist.get(position).packageName;
//         String friendlyAppString = (String) applist.get(position).loadLabel(packageManager);
//         // always set xxx_SQUEEZE_CUSTOM_APP so we can fallback if something goes wrong with
//         // packageManager.getPackageInfo
//         setPackage(packageName, friendlyAppString);
//         setPackageActivity(null);
//         showActivitiesDialog(packageName);
//     }

//     protected void setPackage(String packageName, String friendlyAppString) {
//         Settings.Secure.putStringForUser(getContentResolver(),
//                 Settings.Secure.SHORT_SQUEEZE_CUSTOM_APP, packageName,
//                 UserHandle.USER_CURRENT);
//         Settings.Secure.putStringForUser(getContentResolver(),
//                 Settings.Secure.SHORT_SQUEEZE_CUSTOM_APP_FR_NAME, friendlyAppString,
//                 UserHandle.USER_CURRENT);
//     }

//     protected void setPackageActivity(ActivityInfo ai) {
//         Settings.Secure.putStringForUser(
//                 getContentResolver(), Settings.Secure.SHORT_SQUEEZE_CUSTOM_ACTIVITY,
//                 ai != null ? ai.name : "NONE",
//                 UserHandle.USER_CURRENT);
//     }
// }

package org.startup.sketcher.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Util {

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

    public static void WriteSharePreference(Context context, String key, String values) {
        SharedPreferences write_Data = context.getSharedPreferences(Constant.SHARED_KEY.SHARE_PREF, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = write_Data.edit();
        editor.putString(key, values);
        editor.apply();
    }

    public static String ReadSharePreference(Context context, String key) {
        SharedPreferences read_data = context.getSharedPreferences(Constant.SHARED_KEY.SHARE_PREF, context.MODE_PRIVATE);

        return read_data.getString(key, "");
    }

}

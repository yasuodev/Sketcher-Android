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

    public static String encode(String str){

        String[] escapeChars = {";", "/", "?" , ":",
                "@", "&", "=", "+" ,
                "$" , ",", "[", "]",
                "#", "!", "'", "(",
                ")", "*", "{", "}", "<", ">", "\""};


        String[] replaceChars = {"%3B", "%2F", "%3F",
                "%3A", "%40", "%26" ,
                "%3D", "%2B", "%24" ,
                "%2C", "%5B", "%5D",
                "%23", "%21", "%27",
                "%28", "%29", "%2A", "%7B", "%7D", "%3C", "%3E", "%22"};

        int len = escapeChars.length;

        String temp = str;

        int i;
        for(i = 0; i < len; i++) {
            temp = temp.replace(escapeChars[i], replaceChars[i]);
        }

        String result = temp;
        return result;
    }

}

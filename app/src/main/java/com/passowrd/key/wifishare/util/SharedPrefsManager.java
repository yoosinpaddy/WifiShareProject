package com.passowrd.key.wifishare.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static SharedPreferences sharedPreferences;
    private static SharedPrefsManager mInstance;
    private final String SHARED_PREFS_NAME = "shared_prefs_name";
    private final String SHOW_ICON = "show_icon";
    private static final String TAG = "SharedPrefsManager";

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefsManager(context);
        }
        return mInstance;
    }
    public void shouldShowIcon(boolean shouldShow){
        sharedPreferences.edit().putBoolean(SHOW_ICON,shouldShow).apply();
    }
    public boolean shouldShowIcon(){
        return sharedPreferences.getBoolean(SHOW_ICON,false);
    }
}

package com.trichain.wifishare.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static SharedPreferences sharedPreferences;
    private static SharedPrefsManager mInstance;
    private final String SHARED_PREFS_NAME = "shared_prefs_name";
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
}

package com.androidapp.attendencecheckqrcode.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String token) {
        editor.putString("JWT_TOKEN", token).apply();
    }

    public String getToken() {
        return prefs.getString("JWT_TOKEN", null);
    }
}

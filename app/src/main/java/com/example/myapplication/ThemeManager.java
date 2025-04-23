package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ThemeManager handles storing and retrieving the user's dark mode preference.
 */
public class ThemeManager {

    // SharedPreferences file name
    private static final String PREFS_NAME = "SmartPlantPrefs";

    // Key for dark mode setting
    private static final String KEY_DARK_MODE = "darkMode";

    /**
     * Checks if dark mode is currently enabled.
     *
     * @param context Application context for accessing SharedPreferences
     * @return true if dark mode is enabled, false otherwise
     */
    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Saves the user's dark mode preference.
     *
     * @param context Application context
     * @param isDark  true to enable dark mode, false to disable
     */
    public static void setDarkMode(Context context, boolean isDark) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_DARK_MODE, isDark);
        editor.apply(); // Commit changes asynchronously
    }
}

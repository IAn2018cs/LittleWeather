package cn.ian2018.littleweather.util;

import android.content.Context;
import android.content.SharedPreferences;

import cn.ian2018.littleweather.MyApplication;

/**
 * Created by Administrator on 2017/2/23/023.
 */

public class SharedPreferencesUtil {
    private static String SHARED_PREFERENCES_NAME = "cache";
    private static SharedPreferences sSharedPreferences = MyApplication.getContext().
            getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

    public static void putString(String key, String value) {
        sSharedPreferences.edit().putString(key, value).apply();
    }

    public static void putInt(String key, int value) {
        sSharedPreferences.edit().putInt(key, value).apply();
    }

    public static void putBoolean(String key, boolean value) {
        sSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static String getString(String key, String def) {
        return sSharedPreferences.getString(key, def);
    }

    public static int getInt(String key, int def) {
        return sSharedPreferences.getInt(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return sSharedPreferences.getBoolean(key, def);
    }

    /**
     * 删除SharedPreferences中对应key的值
     *
     * @param key 要删除的值的key
     */
    public static void remove(String key) {
        sSharedPreferences.edit().remove(key).apply();
    }
}

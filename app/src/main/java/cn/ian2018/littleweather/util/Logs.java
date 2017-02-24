package cn.ian2018.littleweather.util;

import android.util.Log;

/**
 * Created by Administrator on 2017/2/23/023.
 */

public class Logs {
    private static boolean IS_DEBUG = true;
    private static final String TAG = "小天气";

    public static void d(String text) {
        if (IS_DEBUG) {
            Log.d(TAG, text);
        }
    }

    public static void w(String text) {
        if (IS_DEBUG) {
            Log.w(TAG, text);
        }
    }

    public static void v(String text) {
        if (IS_DEBUG) {
            Log.v(TAG, text);
        }
    }

    public static void i(String text) {
        if (IS_DEBUG) {
            Log.i(TAG, text);
        }
    }

    public static void e(String text) {
        if (IS_DEBUG) {
            Log.e(TAG, text);
        }
    }
}

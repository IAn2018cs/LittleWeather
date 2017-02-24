package cn.ian2018.littleweather.util;

import android.widget.Toast;

import cn.ian2018.littleweather.MyApplication;

/**
 * Created by Administrator on 2017/2/23/023.
 */

public class ToastUtil {
    public static void show(String msg) {
        Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}

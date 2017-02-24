package cn.ian2018.littleweather;

import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by Administrator on 2017/2/23/023.
 */

public class MyApplication extends LitePalApplication {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Context getContext() {
        return sContext;
    }

}

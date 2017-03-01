package cn.ian2018.littleweather;

import android.content.Context;

import org.litepal.LitePalApplication;

import cn.bmob.v3.Bmob;
import cn.ian2018.littleweather.util.Constant;

/**
 * Created by Administrator on 2017/2/23/023.
 */

public class MyApplication extends LitePalApplication {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化Bomb
        Bmob.initialize(this, Constant.BMOB_APP_ID);
        sContext = this;
    }

    public static Context getContext() {
        return sContext;
    }

}

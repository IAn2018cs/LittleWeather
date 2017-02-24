package cn.ian2018.littleweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.IOException;

import cn.ian2018.littleweather.gson.Weather;
import cn.ian2018.littleweather.util.Constant;
import cn.ian2018.littleweather.util.HttpUtil;
import cn.ian2018.littleweather.util.JsonUtil;
import cn.ian2018.littleweather.util.Logs;
import cn.ian2018.littleweather.util.SharedPreferencesUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/2/24/024.
 */

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.d("服务开启了");
        updateWeather();
        updateBingPic();
        // 设置定时更新
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anTime = 8 * 60 * 60 * 1000; // 8小时
        long triggerAtTime = SystemClock.elapsedRealtime() + anTime;
        Intent serviceIntent = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气
     */
    private void updateWeather() {
        String weatherId = SharedPreferencesUtil.getString("weather_id", "");
        String weatherUrl = Constant.WEATHER_URL + "?cityid=" + weatherId + "&key=" + Constant.WEATHER_KEY;
        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String weatherResponse = response.body().string();
                Weather weather = JsonUtil.handleWeatherResponse(weatherResponse);
                // 请求成功后 存入缓存中
                if (weather != null && weather.status.equals("ok")) {
                    SharedPreferencesUtil.putString("weather", weatherResponse);
                }
            }
        });
    }

    /**
     * 更新背景图
     */
    private void updateBingPic() {
        HttpUtil.sendHttpRequest(Constant.PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String picUrl = response.body().string();
                // 将图片地址存入缓存中
                SharedPreferencesUtil.putString("bing_pic", picUrl);
            }
        });
    }
}

package cn.ian2018.littleweather.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;

import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.gson.Forecast;
import cn.ian2018.littleweather.gson.Weather;
import cn.ian2018.littleweather.util.Constant;
import cn.ian2018.littleweather.util.HttpUtil;
import cn.ian2018.littleweather.util.JsonUtil;
import cn.ian2018.littleweather.util.Logs;
import cn.ian2018.littleweather.util.SharedPreferencesUtil;
import cn.ian2018.littleweather.util.ToastUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/2/24/024.
 */

public class WeatherActivity extends AppCompatActivity {

    private ImageView bingPicImage;
    private Button homeButton;
    private TextView cityNameText;
    private TextView updateTimeText;
    private ScrollView weatherLayout;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 让状态栏透明
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        // 初始化控件
        initUI();

        // 判断是否有缓存
        String weatherString = SharedPreferencesUtil.getString("weather", null);
        if (weatherString != null) {
            Logs.d("加载的缓存信息");
            Weather weather = JsonUtil.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            // 从服务器获取天气信息
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 从服务器获取天气信息
     * @param weatherId 城市天气id
     */
    private void requestWeather(String weatherId) {
        String weatherUrl = Constant.WEATHER_URL + "?cityid=" + weatherId + "&key=" + Constant.WEATHER_KEY;
        // 发送请求
        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {
            // 请求失败
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show("获取天气信息失败");
                    }
                });
            }

            // 请求成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherText = response.body().string();
                Logs.d(weatherText);
                final Weather weather = JsonUtil.handleWeatherResponse(weatherText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && weather.status.equals("ok")) {
                            // 将天气信息存取缓存
                            SharedPreferencesUtil.putString("weather", weatherText);
                            // 展示天气信息
                            showWeatherInfo(weather);
                        } else {
                            ToastUtil.show("加载天气信息失败");
                        }
                    }
                });
            }
        });

        // 获取背景图片地址
        loadBingPic();
    }

    /**
     * 展示天气信息
     * @param weather 解析完的Weather实例
     */
    private void showWeatherInfo(Weather weather) {
        // 设置头布局和现在天气
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];   // 根据空格将字符串分割成数组 取第二个
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        cityNameText.setText(cityName);
        updateTimeText.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        // 设置预报天气
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            // 创建view
            View view = LayoutInflater.from(this).inflate(R.layout.forcast_item, forecastLayout, false);
            // 初始化控件
            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            // 展示信息
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max + "°C");
            minText.setText(forecast.temperature.min + "°C");

            // 添加到父布局中
            forecastLayout.addView(view);
        }

        // 设置空气质量
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        // 设置生活建议
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动指数：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        // 显示天气布局
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void initUI() {
        bingPicImage = (ImageView) findViewById(R.id.bing_pic_image);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        homeButton = (Button) findViewById(R.id.home_button);
        cityNameText = (TextView) findViewById(R.id.city_name_text);
        updateTimeText = (TextView) findViewById(R.id.update_time_text);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        String bingPic = SharedPreferencesUtil.getString("bing_pic", null);
        if (bingPic != null) {
            // 如果有缓存  就通过Glide加载图片
            Glide.with(this).load(bingPic).into(bingPicImage);
        } else {
            // 从服务器获取图片地址
            loadBingPic();
        }
    }

    /**
     * 获取背景图片地址
     */
    private void loadBingPic() {
        HttpUtil.sendHttpRequest(Constant.PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String picUrl = response.body().string();
                // 将图片地址存取缓存
                SharedPreferencesUtil.putString("bing_pic", picUrl);
                // 加载图片
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(picUrl).into(bingPicImage);
                    }
                });
            }
        });
    }
}

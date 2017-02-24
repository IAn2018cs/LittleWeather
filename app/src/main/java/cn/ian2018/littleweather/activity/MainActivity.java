package cn.ian2018.littleweather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 如果有缓存 就直接跳转页面
        String weatherInfo = SharedPreferencesUtil.getString("weather", null);
        if (weatherInfo != null) {
            startActivity(new Intent(this, WeatherActivity.class));
            finish();
        }
    }
}

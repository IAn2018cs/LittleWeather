package cn.ian2018.littleweather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private int code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 获取跳转码 判断是从哪跳转过来的
        code = getIntent().getIntExtra("code",0);
        // 如果有缓存 就直接跳转页面
        String weatherInfo = SharedPreferencesUtil.getString("weather", null);
        if (code != 200 && weatherInfo != null) {
            startActivity(new Intent(this, WeatherActivity.class));
            finish();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 判断当期是从哪跳转过来
            if (code == 200) {
                startActivity(new Intent(this, WeatherActivity.class));
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

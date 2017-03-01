package cn.ian2018.littleweather.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.db.UpdateFile;
import cn.ian2018.littleweather.gson.Forecast;
import cn.ian2018.littleweather.gson.Weather;
import cn.ian2018.littleweather.service.AutoUpdateService;
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
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
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
    private String mWeatherId;
    private NavigationView navigation;
    private File mFile;
    private BmobFile mBmobFile;
    private ProgressDialog progressDialog;
    private TextView dressingText;
    private TextView sickText;
    private TextView aqiDesText;
    private ImageView condImage;
    private TextView windText;

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
        mWeatherId = SharedPreferencesUtil.getString("weather_id", "");
        if (weatherString != null) {
            Logs.d("加载的缓存信息");
            Weather weather = JsonUtil.handleWeatherResponse(weatherString);
            // 如果weatherId和缓存中的相同 就加载缓存信息
            if (mWeatherId.equals(weather.basic.weatherId)) {
                showWeatherInfo(weather);
            } else {
                // 从服务器获取天气信息
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }
        } else {
            // 从服务器获取天气信息
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        // 开启服务
        startService(new Intent(this, AutoUpdateService.class));

        //检测更新
        checkVersionCode();
    }

    /**
     * 检查更新
     */
    private void checkVersionCode() {
        BmobQuery<UpdateFile> bmobQuery = new BmobQuery<>();
        // 从bmob的数据表中查询数据
        bmobQuery.getObject(Constant.UPDATE_OBJECT_ID, new QueryListener<UpdateFile>() {
            @Override
            public void done(UpdateFile updateFile, BmobException e) {
                if (e == null) {
                    // 如果bmob中的版本号大于本地版本号
                    if (updateFile.getVersion() > getVersionCode()) {
                        // 初始化下载文件目录
                        String path = getExternalFilesDir("apk").getPath() + "/littleweather.apk";
                        mFile = new File(path);
                        mBmobFile = updateFile.getApkFile();
                        if (mFile != null) {
                            // 显示更新对话框
                            showUpdateDialog(updateFile.getDescription());
                        }
                    }
                } else {
                    Logs.d("查询更新信息失败：" + e.getMessage() + e.getErrorCode());
                }
            }
        });
    }

    /**
     * 显示更新对话框
     * @param description 更新描述
     */
    private void showUpdateDialog(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.icon);
        builder.setTitle("发现新版本");
        builder.setMessage(description);
        builder.setCancelable(false);
        // 设置积极按钮
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 下载apk
                downLoadApk();
                // 显示进度对话框
                showProgressDialog();
            }
        });
        // 设置消极按钮
        builder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 下载apk
     */
    private void downLoadApk() {
        if (mBmobFile != null) {
            // 调用bmob的下载文件api
            mBmobFile.download(mFile, new DownloadFileListener() {
                @Override
                public void done(String s, BmobException e) {
                    if (e == null) {
                        ToastUtil.show("下载成功，保存路径为：" + mFile.getPath());
                        closeProgessDialog();
                        // 安装应用
                        installApk();
                    }
                }

                @Override
                public void onProgress(Integer integer, long l) {
                    // 更新进度对话框进度
                    progressDialog.setProgress(integer);
                }
            });
        }
    }

    /**
     * 安装应用
     */
    private void installApk() {
        if (mFile != null) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            //文件作为数据源
            intent.setDataAndType(Uri.fromFile(mFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIcon(R.mipmap.icon);
            progressDialog.setTitle("下载安装包中");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgessDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 获取应用版本号
     */
    private int getVersionCode() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            // 返回应用版本号
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 从服务器获取天气信息
     * @param weatherId 城市天气id
     */
    public void requestWeather(String weatherId) {
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
                        swipeRefresh.setRefreshing(false);
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
                        swipeRefresh.setRefreshing(false);
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
            aqiDesText.setText(weather.aqi.city.airQuality);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        } else {
            aqiDesText.setText("暂无");
            aqiText.setText("暂无");
            pm25Text.setText("暂无");
        }

        // 设置生活建议
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动指数：" + weather.suggestion.sport.info;
        String dressing = "穿衣指数：" + weather.suggestion.dressingAdvice.info;
        String sick = "感冒指数：" + weather.suggestion.sick.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        dressingText.setText(dressing);
        sickText.setText(sick);

        // 设置滑动菜单头布局图片
        String condIconUrl = Constant.COND_ICON_URL + weather.now.more.code + ".png";
        Glide.with(this).load(condIconUrl).into(condImage);
        // 设置头布局风力文字
        windText.setText(weather.now.wind.info);

        // 显示天气布局
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化
     */
    private void initUI() {
        bingPicImage = (ImageView) findViewById(R.id.bing_pic_image);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigation = (NavigationView) findViewById(R.id.navigation);
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
        dressingText = (TextView) findViewById(R.id.dressing_text);
        sickText = (TextView) findViewById(R.id.sick_text);
        aqiDesText = (TextView) findViewById(R.id.aqi_des_text);

        // 配置swipeRefresh
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary , R.color.colorAccent, R.color.colorPrimaryDark);
        // 设置刷新事件
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 从服务器重新加载天气信息
                mWeatherId = SharedPreferencesUtil.getString("weather_id", "");
                requestWeather(mWeatherId);
            }
        });

        // 设置home按钮点击事件
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 配置菜单头布局
        View headerView = navigation.inflateHeaderView(R.layout.menu_nav_head);
        condImage = (ImageView) headerView.findViewById(R.id.cond_image);
        windText = (TextView) headerView.findViewById(R.id.wind_text);
        // 设置滑动菜单点击事件
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navItem1: // 选择城市
                        Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                        intent.putExtra("code", 200);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.navItem2: // 意见反馈
                        startActivity(new Intent(WeatherActivity.this, FeedbackActivity.class));
                        break;
                    case R.id.navItem3: // 关于
                        startActivity(new Intent(WeatherActivity.this, AboutActivity.class));
                        break;
                }
                return false;
            }
        });

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

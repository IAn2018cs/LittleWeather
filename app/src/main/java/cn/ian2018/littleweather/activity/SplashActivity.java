package cn.ian2018.littleweather.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

import java.util.ArrayList;
import java.util.List;

import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.util.Constant;
import cn.ian2018.littleweather.util.ToastUtil;

public class SplashActivity extends AppCompatActivity {

    private RelativeLayout container;
    private ImageView splashImage;
    private boolean canJump;    // 是否跳过广告

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        // 初始化控件
        initUI();

        // 检查运行时权限
        checkPermission();
    }

    /**
     * 检查运行时权限
     */
    private void checkPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            // 请求开屏广告
            requestAd();
        }
    }

    /**
     * 请求开屏广告
     */
    private void requestAd() {
        new SplashAD(this, container, Constant.APP_ID, Constant.AD_ID, new SplashADListener() {
            @Override
            public void onADDismissed() {
                // 广告显示完毕 进入主界面
                next();
            }

            @Override
            public void onNoAD(int i) {
                // 广告加载失败 进入主界面
                next();
            }

            @Override
            public void onADPresent() {
                // 广告加载成功后 需要隐藏预设图片
                splashImage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onADClicked() {
                // 广告被点击
            }

            @Override
            public void onADTick(long l) {
                // 倒计时回调，返回广告还将被展示的剩余时间。
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            ToastUtil.show("请您同意所有权限再使用小天气");
                            finish();
                            return;
                        }
                    }
                    requestAd();
                } else {
                    ToastUtil.show("小天气出了个小错误");
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJump = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            next();
        }
        canJump = true;
    }


    /**
     * 进入MainActivity
     */
    private void next() {
        if (canJump) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            canJump = true;
        }
    }

    private void initUI() {
        container = (RelativeLayout) findViewById(R.id.splash_container);
        splashImage = (ImageView) findViewById(R.id.splash_image);
    }
}

package cn.ian2018.littleweather.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.util.ToastUtil;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Button backButton = (Button) findViewById(R.id.back_button);
        TextView versionText = (TextView) findViewById(R.id.version_text);
        TextView emailText = (TextView) findViewById(R.id.email_text);
        ImageView blogImage = (ImageView) findViewById(R.id.blog_image);

        // 拿到包管理者
        PackageManager pm = getPackageManager();
        // 获取包的信息(Info)
        try {
            // flags：为0是获取基本信息
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            // 设置版本号
            versionText.setText("版本号："+info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 设置点击事件
        backButton.setOnClickListener(this);
        emailText.setOnClickListener(this);
        blogImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 返回按钮
            case R.id.back_button:
                finish();
                break;
            // 联系我
            case R.id.email_text:
                // 将文字复制到系统粘贴板
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("text","chenshuai@ian2018.cn");
                cm.setPrimaryClip(data);
                ToastUtil.show("已经将我的邮箱地址复制到粘贴板");
                break;
            // 博客
            case R.id.blog_image:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://ian2018.cn/"));
                startActivity(intent);
                break;
        }
    }
}

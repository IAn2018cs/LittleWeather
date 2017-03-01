package cn.ian2018.littleweather.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.db.Feedback;
import cn.ian2018.littleweather.util.Logs;
import cn.ian2018.littleweather.util.ToastUtil;

public class FeedbackActivity extends AppCompatActivity {

    private Button submitButton;
    private EditText feedbackText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // 初始化控件
        initUI();

        // 提交反馈
        submitFeedback();
    }

    private void submitFeedback() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = feedbackText.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    showProgressDialog();
                    Feedback feedback = new Feedback();
                    feedback.setInfo(message);
                    feedback.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                closeProgressDialog();
                                feedbackText.setText("");
                                ToastUtil.show("感谢您的反馈，我们会尽快处理");
                            } else {
                                closeProgressDialog();
                                ToastUtil.show("反馈失败，请您稍后重试");
                                Logs.d("反馈失败：" + e.getMessage());
                            }
                        }
                    });
                } else {
                    ToastUtil.show("反馈内容不能为空");
                }
            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("发送反馈中");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    private void initUI() {
        feedbackText = (EditText) findViewById(R.id.feedback_edit);
        submitButton = (Button) findViewById(R.id.submit_button);
        Button backButton = (Button) findViewById(R.id.back_button);

        // 设置返回按钮点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


}

package com.bolo4963gmail.motoandroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.OkHttpConnection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;

/**
 * Created by 10733 on 2016/8/23.
 */
public class Login2Activity extends BaseActivity {

    private static final String TAG = "Login2Activity";

    public static final int START_MAIN_ACTIVITY = 1;

    @BindView(R.id.login_back2) ImageView loginBack2;
    @BindView(R.id.toolbar2) Toolbar toolbar2;
    @BindView(R.id.account) EditText account;
    @BindView(R.id.password) EditText password;
    @BindView(R.id.login_table2) TableLayout loginTable2;
    @BindView(R.id.login_button) Button loginButton;

    private ProgressDialog progressDialog = null;

    String url;
    String projectName;
    String cookie;

    private static final String KEY_URL = "url";
    private static final String KEY_PROJECT_NAME = "project";

    private Handler handler = new Handler() {

        public void handleMessage(Message message) {
            switch (message.what) {
                case START_MAIN_ACTIVITY:
                    StartMainActivity();
            }
        }

    };

    public static void start(Context context,
                             String url,
                             String projectName) {

        Intent starter = new Intent(context, Login2Activity.class);
        starter.putExtra(KEY_URL, url);
        starter.putExtra(KEY_PROJECT_NAME, projectName);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        ButterKnife.bind(this);

        // TODO: 2016/9/5 delete these two lines when the project is completed
        account.setText("memory4963");
        password.setText("456rtyFGHvbn");

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        projectName = intent.getStringExtra("project");

    }

    @OnClick(R.id.login_button)
    public void OnclickLoginButton() {

        if (TextUtils.isEmpty(account.getText().toString())) {
            Toast.makeText(Login2Activity.this, "请输入账号", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(password.getText().toString())) {
            Toast.makeText(Login2Activity.this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(Login2Activity.this);
        progressDialog.setTitle("正在连接");
        progressDialog.setMessage("请稍候");
        progressDialog.show();

        new Thread(new Runnable() {

            @Override
            public void run() {
                List<String> list = new ArrayList<>();
                list.add(account.getText().toString());
                list.add(password.getText().toString());
                Response response =
                        OkHttpConnection.POSTconnection(OkHttpConnection.SpliceLoginUrl(url), list);
                if (response.code() == 302) {
                    try {
                        setCookie(response);
                        Message message = new Message();
                        message.what = START_MAIN_ACTIVITY;
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "run: the code is not 302");
                }
            }
        }).start();
    }

    @OnClick(R.id.login_back2)
    public void OnclickLoginBack2() {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setCookie(Response response) throws NullPointerException {
        cookie = response.header("Set-Cookie");
        if (cookie == null) {
            throw new NullPointerException("cookie is null");
        }
    }

    private void StartMainActivity() {
        progressDialog.dismiss();
        MainActivity.start(Login2Activity.this, url, projectName, cookie);
        finish();
    }

}

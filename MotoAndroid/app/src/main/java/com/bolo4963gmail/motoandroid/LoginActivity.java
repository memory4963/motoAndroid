package com.bolo4963gmail.motoandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.OkHttpConnection;
import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends BaseActivity{

    private static final String TAG = "LoginActivity";

    private SQLiteDatabase db = null;

    private ProgressDialog progressDialog = null;
    public static final int START_LOGIN2ACTIVITY = 1;

    @BindView(R.id.login_back) ImageView loginBack;
    @BindView(R.id.server_address) EditText address;
    @BindView(R.id.project_name) EditText projectName;
    @BindView(R.id.login_connect_button) Button connectButton;

    private Handler handler = new Handler() {

        public void handleMessage(Message message) {
            switch (message.what) {
                case START_LOGIN2ACTIVITY:
                    Log.d(TAG, "handleMessage: invoked startLogin2Activity method");
                    startLogin2Activity();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        ThisDatabaseHelper dbHelper = ThisDatabaseHelper.getDatabaseHelper();//获得APP自带数据库
        db = dbHelper.getWritableDatabase();

        // TODO: 2016/9/5 delete these two lines when the project is completed
        address.setText("115.29.114.77");
        projectName.setText("testOne");


        loginBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @OnClick(R.id.login_connect_button)
    public void OnclickConnectButton() {

        if (TextUtils.isEmpty(address.getText().toString())) {
            Toast.makeText(LoginActivity.this, "请输入服务器地址", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            db.execSQL(
                    "insert into " + ThisDatabaseHelper.SERVER_NAMES_TABLE + " (server) values(?)",
                    new String[]{address.getText().toString()});//向APP自带数据库中添加此服务器名称

            Log.d(TAG, "onClick: " + projectName.getText().toString());

            if (TextUtils.isEmpty(projectName.getText().toString())) {
                Toast.makeText(LoginActivity.this, "请稍后在设置中输入项目名称", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("正在连接");
        progressDialog.setMessage("请稍候");
        progressDialog.show();

        /*
        start a new thread to confirm the link is available
         */
        new Thread(new Runnable() {

            @Override
            public void run() {

                String string =
                        OkHttpConnection.SpliceGetUrl(null, null, address.getText().toString());
                Log.d(TAG, "run: string =" + string);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(string).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "run: connected");
                if (response.code() == 403) {
                    Log.d(TAG, "run: connected successfully");
                    Message message = new Message();
                    message.what = START_LOGIN2ACTIVITY;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startLogin2Activity() {
        progressDialog.dismiss();
        Login2Activity.start(LoginActivity.this, address.getText().toString(),
                             projectName.getText().toString());
        finish();
    }

}

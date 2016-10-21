package com.bolo4963gmail.motoandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.OkHttpConnection;
import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

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
    public static final int RESULT_FOR_LOGIN_ACTIVITY = 2;
    public static final String DATA_RETURN = "dataReturn";

    @BindView(R.id.login2_toolbar) Toolbar toolbar2;
    @BindView(R.id.account) EditText account;
    @BindView(R.id.password) EditText password;
    @BindView(R.id.login_table2) TableLayout loginTable2;
    @BindView(R.id.login_button) Button loginButton;

    private ProgressDialog progressDialog = null;
    private SQLiteDatabase db = null;

    String server;
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

    public static void start(BaseActivity activity, String url, String projectName) {

        Intent starter = new Intent(activity, Login2Activity.class);
        starter.putExtra(KEY_URL, url);
        starter.putExtra(KEY_PROJECT_NAME, projectName);
        activity.startActivityForResult(starter, RESULT_FOR_LOGIN_ACTIVITY);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar2);

        ThisDatabaseHelper dbHelper = ThisDatabaseHelper.getDatabaseHelper();
        db = dbHelper.getWritableDatabase();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar2.setTitle("持续集成系统");

        Intent intent = getIntent();
        server = intent.getStringExtra("url");
        projectName = intent.getStringExtra("project");
        Log.d(TAG, "onCreate: projectName = " + projectName);

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

        /**
         * save the account and password
         */
        Cursor cursor = db.rawQuery(
                "select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE + " where server = ?",
                new String[]{server});
        Integer serverId = -1;
        if (cursor.moveToFirst()) {
            serverId = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        db.execSQL("insert into " + ThisDatabaseHelper.ACCOUNT_TABLE
                           + "(account, password, serverId, cookie) values(?,?,?,?)", new String[]{
                account.getText().toString(), password.getText().toString(), serverId.toString(), ""
        });

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
                        OkHttpConnection.POSTconnection(OkHttpConnection.SpliceLoginUrl(server),
                                                        list);
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

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setCookie(Response response) throws NullPointerException {
        cookie = response.header("Set-Cookie");
        Cursor cursor = db.rawQuery(
                "select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE + " where server = ?",
                new String[]{server});
        Integer serverId = -1;
        if (cursor.moveToFirst()) {
            serverId = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();

        if (cookie == null) {
            throw new NullPointerException("cookie is null");
        }
        db.execSQL(
                "update " + ThisDatabaseHelper.ACCOUNT_TABLE + " set cookie = ? where serverId = ?",
                new String[]{cookie, serverId.toString()});
    }

    private void StartMainActivity() {
        progressDialog.dismiss();
        setResult(RESULT_OK, new Intent());
        MainActivity.start(Login2Activity.this, server, projectName, cookie);
        finish();
    }

}

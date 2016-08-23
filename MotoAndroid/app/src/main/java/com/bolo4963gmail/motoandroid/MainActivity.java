package com.bolo4963gmail.motoandroid;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.Connection;
import com.bolo4963gmail.motoandroid.javaClass.JsonData;
import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {

    public static final int UPDATE_TEXT = 1;

    private static final String TAG = "MainActivity";

    public static boolean ifFirstTime;

    List<Map<String, Object>> viewData;

    private boolean ifNew = true;

    ListView listView;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    listView.setAdapter(new ArrayAdapter<>(MainActivity.this,
                                                                              R.layout.list_view,
                                                                              viewData));
                default:
                    break;
            }
        }

    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ifFirstTime) {
            ifFirstTime = false;
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);//第一次打开APP时启动LoginActivity
            finish();
        }

        Intent intent = getIntent();

        if ((intent.getStringExtra("urlStr") != null) && (intent.getStringExtra("projectName")
                != null)) {//服务器名和项目名同时存在

            String urlStr = intent.getStringExtra("urlStr");
            String projectName = intent.getStringExtra("projectName");
            ThisDatabaseHelper dbHelper =
                    new ThisDatabaseHelper(MainActivity.this, "motoAndroid.db",
                                           null, 1, "AddressName");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select id from AddressName where address = ?",
                                        new String[]{urlStr});
            Integer num = 0;
            if (cursor.moveToFirst())
                num = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();

            pullData(num, projectName, urlStr);

        } else if ((intent.getStringExtra("urlStr") == null) && (
                intent.getStringExtra("projectName") != null)) {//服务器地址不存在
            Toast.makeText(this, "请在设置中输入服务器地址", Toast.LENGTH_SHORT).show();
        } else if ((intent.getStringExtra("urlStr") != null) && (
                intent.getStringExtra("projectName") == null)) {//项目名不存在
            Toast.makeText(this, "请在设置中输入项目名称", Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_button:
                Toast.makeText(this, "you clicked the refresh button", Toast.LENGTH_SHORT).show();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void pullData(final Integer num, final String projectName, final String urlStr) {

        viewData = new ArrayList<>();

        ThisDatabaseHelper UserHelper = new ThisDatabaseHelper(MainActivity.this,
                                                               "Database"
                                                                       + num.toString()
                                                                       + ".db",
                                                               null, 1,
                                                               projectName);
        final SQLiteDatabase UserDb = UserHelper.getWritableDatabase();
        Cursor checkCursor = UserDb.rawQuery("select * from " + projectName, null);

        int i1 = 1;
        if (checkCursor.getCount() > 0) {
            checkCursor.moveToLast();
            i1 = checkCursor.getInt(checkCursor.getColumnIndex("id"));
            ifNew = false;
            for (int j = 1; j < i1; j++) {
                Integer k = j;
                Cursor oldData = UserDb.rawQuery("select * from " + projectName + " whrere id = ?",
                                                 new String[]{k.toString()});
                addData(oldData);
            }
        }

        checkCursor.close();

        final int i2 = i1;

        new Thread(new Runnable() {

            @Override
            public void run() {

                Integer i = i2;

                while (true) {
                    URL Address = Connection.SpliceURL(projectName, i, urlStr);
                    JsonData jsonData = Connection.connecting(Address);
                    if (jsonData == null && i == 1) {
                        LoginWebViewActivity.startAction(MainActivity.this, urlStr, projectName,
                                                         true);
                        finish();
                    } else if (jsonData == null) {
                        break;
                    } else {
                        Integer result = null;
                        switch (jsonData.getResult()) {
                            case JsonData.SUCCESS:
                                result = 1;
                                break;
                            case JsonData.FAILURE:
                                result = 0;
                                break;
                            default:
                                Log.w(TAG, "run: result isn't Success or Failure");
                                break;
                        }

                        UserDb.execSQL("insert into " + projectName
                                               + " (id, result, address, project_name) values(?, ?, ?, ?)",
                                       new String[]{
                                               num.toString(), result.toString(), urlStr,
                                               projectName
                                       });
                        Cursor cursor =
                                UserDb.rawQuery("select * from " + projectName + " where id = ?",
                                                new String[]{num.toString()});
                        addData(cursor);

                        i++;
                    }
                }

            }
        }).start();

    }

    private void addData(Cursor cursor) {
        Map<String, Object> map = new HashMap<>();

        map.put("list_id", cursor.getInt(cursor.getColumnIndex("id")));
        map.put("list_address", cursor.getString(cursor.getColumnIndex("address")));
        map.put("list_text_name", cursor.getString(cursor.getColumnIndex("project_name")));

        if (cursor.getInt(cursor.getColumnIndex("result")) == 1) {
            map.put("list_result", R.mipmap.result_y);
        } else if (cursor.getInt(cursor.getColumnIndex("result")) == 0) {
            map.put("list_result", R.mipmap.result_n);
        } else {
            map.put("list_result", R.mipmap.ic_launcher);
        }

        viewData.add(map);
        Message message = new Message();
        message.what = UPDATE_TEXT;
        handler.sendMessage(message);
    }

    public static void startAction(Context context, String urlStr, String projectName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("urlStr", urlStr);
        intent.putExtra("projectName", projectName);
        context.startActivity(intent);
    }
}

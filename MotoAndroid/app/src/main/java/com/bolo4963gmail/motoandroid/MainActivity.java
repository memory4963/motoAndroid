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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.JsonData;
import com.bolo4963gmail.motoandroid.javaClass.OkHttpConnection;
import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private static final int UPDATE_TEXT = 1;

    private static final String TAG = "MainActivity";

    private SQLiteDatabase db;

    List<Map<String, Object>> viewDataList;
    List<String> serverSpinnerString;
    List<String> projectSpinnerString;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.list_view) ListView listView;
    @BindView(R.id.server_spinner) Spinner serverSpinner;
    @BindView(R.id.project_spinner) Spinner projectSpinner;

    String server;
    String project;
    String cookie;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    Collections.reverse(viewDataList);
                    listView.setAdapter(
                            new SimpleAdapter(MainActivity.this, viewDataList, R.layout.list_view,
                                              new String[]{
                                                      "list_id", "list_project", "list_server",
                                                      "list_result"
                                              }, new int[]{
                                    R.id.list_id, R.id.list_project, R.id.list_server,
                                    R.id.list_result
                            }));
                    Collections.reverse(viewDataList);
                    break;
                default:
                    break;
            }
        }

    };

    private static final String KEY_URL_STRING = "urlstr";
    private static final String KEY_PROJECT_NAME = "project";
    private static final String KEY_COOKIE = "cookie";

    public static void start(Context context, String urlstr, String projectName, String cookie) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.putExtra(KEY_URL_STRING, urlstr);
        starter.putExtra(KEY_PROJECT_NAME, projectName);
        starter.putExtra(KEY_COOKIE, cookie);
        context.startActivity(starter);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        //get database
        ThisDatabaseHelper dbHelper = ThisDatabaseHelper.getDatabaseHelper();
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE,
                                    new String[]{});

        if (!cursor.moveToFirst()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        //set serverSpinnerString and serverSpinnerAdapter
        if (cursor.moveToFirst()) {
            serverSpinnerString = new ArrayList<>();
            do {
                serverSpinnerString.add(cursor.getString(cursor.getColumnIndex("server")));
            } while (cursor.moveToNext());
            cursor.close();
            ArrayAdapter<String> serverSpinnerAdapter =
                    new ArrayAdapter<>(this, R.layout.spinner_text_view, serverSpinnerString);
            serverSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            serverSpinner.setAdapter(serverSpinnerAdapter);
        }

        //set projectSpinnerString and serverSpinnerAdapter
        cursor = db.rawQuery(
                "select * from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE + " where serverId = ?",
                new String[]{serverSpinnerString.get(0)});
        if (cursor.moveToFirst()) {
            projectSpinnerString = new ArrayList<>();
            do {
                projectSpinnerString.add(cursor.getString(cursor.getColumnIndex("project")));
            } while (cursor.moveToNext());
            cursor.close();
            ArrayAdapter<String> projectSpinnerAdapter =
                    new ArrayAdapter<>(this, R.layout.spinner_text_view,
                                             projectSpinnerString);
            projectSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            projectSpinner.setAdapter(projectSpinnerAdapter);
        }

        Intent intent = getIntent();

        if (intent.getStringExtra(KEY_COOKIE) != null) {
            cookie = intent.getStringExtra(KEY_COOKIE);
        }

        if ((intent.getStringExtra(KEY_URL_STRING) != null) && (
                intent.getStringExtra(KEY_PROJECT_NAME) != null)) {//服务器名和项目名同时存在

            server = intent.getStringExtra(KEY_URL_STRING);
            project = intent.getStringExtra(KEY_PROJECT_NAME);

            cursor = db.rawQuery(
                    "select id from " + ThisDatabaseHelper.SERVER_NAMES_TABLE + " where server = ?",
                    new String[]{server});
            int serverId = -1;
            if (cursor.moveToFirst()) {
                serverId = cursor.getInt(cursor.getColumnIndex("id"));
            }
            Log.d(TAG, "onCreate: serverId = " + serverId);
            cursor.close();

            pullData(serverId);

        } else if ((intent.getStringExtra(KEY_URL_STRING) == null) && (
                intent.getStringExtra(KEY_PROJECT_NAME) != null)) {//服务器地址不存在
            Toast.makeText(this, "请在设置中输入服务器地址", Toast.LENGTH_SHORT).show();
        } else if ((intent.getStringExtra(KEY_URL_STRING) != null) && (
                intent.getStringExtra(KEY_PROJECT_NAME) == null)) {//项目名不存在
            Toast.makeText(this, "请在设置中输入项目名称", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: 2016/9/21  
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

    private void pullData(int num) {

        viewDataList = new ArrayList<>();

        Integer serverId = num;
        Cursor cursor = db.rawQuery("select id from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                            + " where serverId = ? and project = ?",
                                    new String[]{serverId.toString(), project});
        /**
         * check if the project has been added in the table
         */
        if (!cursor.moveToFirst()) {
            cursor.close();
            db.execSQL("insert into " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                               + "(project, serverId) values(?, ?)",
                       new String[]{project, serverId.toString()});
            cursor = db.rawQuery("select id from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                         + " where serverId = ? and project = ?",
                                 new String[]{serverId.toString(), project});
        }

        /**
         * get project's id
         */
        final Integer projectId;
        if (cursor.moveToFirst()) {
            projectId = cursor.getInt(cursor.getColumnIndex("id"));
        } else {
            projectId = -1;
        }
        cursor.close();

        /**
         * get data from result table
         */
        cursor = db.rawQuery(
                "select result from " + ThisDatabaseHelper.RESULT_TABLE + " where projectId = ?",
                new String[]{projectId.toString()});

        /**
         * add data to listView if it has data
         */
        int idNow = 1;
        if (cursor.moveToFirst()) {
            for (int i = 1; ; i++) {
                addData(cursor.getInt(cursor.getColumnIndex("result")), i);
                Log.d(TAG, "pullData: getting data from database");
                if (!cursor.moveToNext()) {
                    idNow = i;
                    Log.d(TAG, "pullData: got data from database");
                    break;
                }
            }
        }

        /**
         * pull data from server and add data to listView
         */
        final int finalIdNow = idNow;
        new Thread(new Runnable() {

            @Override
            public void run() {

                Integer id = finalIdNow;

                while (true) {

                    if (id == 40) {
                        break;
                    }
                    Response response = OkHttpConnection.GETconnecting(
                            OkHttpConnection.SpliceGetUrl(project, id, server), cookie);
                    JsonData jsonData = OkHttpConnection.setJsonDataFromResponse(response);

                    if (jsonData == null && id == 1) {
                        // TODO: 2016/9/1 connect again
                        finish();
                    } else if (jsonData == null) {
                        break;
                    } else {
                        Integer result = -1;
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

                        db.execSQL("insert into " + ThisDatabaseHelper.RESULT_TABLE
                                           + " (result, projectId) values(?, ?)", new String[]{
                                result.toString(), projectId.toString()
                        });
                        addData(result, id);
                        id++;
                    }
                }

            }
        }).start();

    }

    private void addData(int result, int id) {
        Map<String, Object> map = new HashMap<>();

        map.put("list_id", id);
        map.put("list_project", project);
        map.put("list_server", server);

        if (result == 1) {
            map.put("list_result", R.mipmap.result_y);
        } else if (result == 0) {
            map.put("list_result", R.mipmap.result_n);
        } else {
            map.put("list_result", R.mipmap.ic_launcher);
        }

        viewDataList.add(map);
        Message message = new Message();
        message.what = UPDATE_TEXT;
        handler.sendMessage(message);
    }

    private void addData(Cursor cursor, int id) {
        Map<String, Object> map = new HashMap<>();

        map.put("list_id", id);
        map.put("list_project", project);
        map.put("list_server", server);

        if (cursor.getInt(cursor.getColumnIndex("result")) == 1) {
            map.put("list_result", R.mipmap.result_y);
        } else if (cursor.getInt(cursor.getColumnIndex("result")) == 0) {
            map.put("list_result", R.mipmap.result_n);
        } else {
            map.put("list_result", R.mipmap.ic_launcher);
        }

        viewDataList.add(map);
        Message message = new Message();
        message.what = UPDATE_TEXT;
        handler.sendMessage(message);
    }
}

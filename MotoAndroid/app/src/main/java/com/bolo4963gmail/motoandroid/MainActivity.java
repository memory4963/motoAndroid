package com.bolo4963gmail.motoandroid;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.ActivityCollector;
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
import butterknife.OnClick;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private static final int UPDATE_TEXT = 1;
    private static final int SET_REFRESH_FALSE = 2;
    private static final int CONNECT_FALSE = 3;
    private static final int SEND_NOTIFICATION = 4;
    private static final int SETTING_RESULT = 5;

    private static final String TAG = "MainActivity";

    private SQLiteDatabase db = null;
    private AlertDialog alertDialog = null;
    private Intent mStartIntent = null;
    private long time = 0;

    private List<Map<String, Object>> viewDataList;
    private List<String> serverSpinnerString;
    private List<String> projectSpinnerString;
    private ArrayAdapter<String> serverSpinnerAdapter;
    private ArrayAdapter<String> projectSpinnerAdapter;
    private String serverNow;
    private String projectNow;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.list_view) ListView listView;
    @BindView(R.id.server_spinner) Spinner serverSpinner;
    @BindView(R.id.project_spinner) Spinner projectSpinner;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fabBtn) FloatingActionButton floatingActionButton;

    String server;
    String project;
    String cookie;

    Integer testNumber = -1;
    int notificationId = 0;
    boolean sendMassageResult;

    private SimpleAdapter adapter;
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    Collections.reverse(viewDataList);
                    adapter.notifyDataSetChanged();
                    break;
                case SET_REFRESH_FALSE:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case CONNECT_FALSE:
                    final AlertDialog.Builder alertDialogBuilder =
                            new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setTitle("连接出错");
                    alertDialogBuilder.setMessage("连接出错，请重新连接。");
                    alertDialogBuilder.setPositiveButton("确定",
                                                         new DialogInterface.OnClickListener() {

                                                             @Override
                                                             public void onClick(DialogInterface dialog,
                                                                                 int which) {
                                                                 if (alertDialog != null) {
                                                                     alertDialog.dismiss();
                                                                 }
                                                             }
                                                         });
                    alertDialog = alertDialogBuilder.show();
                    break;
                case SEND_NOTIFICATION:
                    NotificationManager manager =
                            (NotificationManager) MainActivity.this.getSystemService(
                                    Context.NOTIFICATION_SERVICE);
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(MainActivity.this, 0, intent,
                                                      PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder =
                            new Notification.Builder(MainActivity.this).setContentTitle(
                                    "MotoAndroid")
                                    .setContentText(
                                            testNumber.toString() + " " + project + " " + server)
                                    .setWhen(System.currentTimeMillis())
                                    .setContentIntent(pendingIntent);
                    if (sendMassageResult) {
                        builder.setSmallIcon(R.mipmap.result_y);
                    } else {
                        builder.setSmallIcon(R.mipmap.result_n);
                    }
                    Notification notification = builder.build();
                    manager.notify(notificationId, notification);
                    notificationId++;
                    break;
                default:
                    break;
            }
        }

    };

    private MotoAndroidPullDataService.MyBinder mBinder;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MotoAndroidPullDataService.MyBinder) service;
            mBinder.setActivity(MainActivity.this);
            Log.d(TAG, "onServiceConnected: setActivity");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTING_RESULT:
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra(SettingsActivity.RESTART, true)) {
                        checkServerAndProject();
                    }
                    if (data.getBooleanExtra(SettingsActivity.REFRESH, true)) {
                        pullData();
                    }
                }
                break;
        }
    }

    private static final String KEY_SERVER_NAME = "urlstr";
    private static final String KEY_PROJECT_NAME = "project";
    private static final String KEY_COOKIE = "cookie";

    public static void start(Context context, String server, String project, String cookie) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.putExtra(KEY_SERVER_NAME, server);
        starter.putExtra(KEY_PROJECT_NAME, project);
        starter.putExtra(KEY_COOKIE, cookie);
        context.startActivity(starter);
    }

    // TODO: 2016/9/25 每次启动时重新拉取cookie
    // TODO: 2016/9/27 后台服务监视
    // TODO: 2016/9/27 更新信息推送
    // TODO: 2016/9/27 接收全部结果
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = new Intent(MainActivity.this, MotoAndroidPullDataService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean bool =
                this.getApplicationContext().bindService(intent, connection, BIND_AUTO_CREATE);
        Log.d(TAG, "onCreate: bindService bool = " + bool);
        startService(intent);

        setSupportActionBar(toolbar);

        viewDataList = new ArrayList<>();
        adapter =
                new SimpleAdapter(MainActivity.this, viewDataList, R.layout.list_view, new String[]{
                        "list_id", "list_project", "list_server", "list_result"
                }, new int[]{
                        R.id.list_id, R.id.list_project, R.id.list_server, R.id.list_result
                });
        listView.setAdapter(adapter);

        //set swipeRefreshLayout
        swipeRefreshLayout.setProgressViewOffset(false, -180, 48);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                pullData();
            }
        });

        mStartIntent = getIntent();

        setSpinnersOnClick();

        setData();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting_button:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTING_RESULT);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (time == 0 || (System.currentTimeMillis() - time > 2700)) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            time = System.currentTimeMillis();
        } else {
            Log.d(TAG, "onBackPressed: finishAll");
            ActivityCollector.finishAll();
        }

//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinder.removeActivity();
        unbindService(connection);
    }

    private void setData() {

        //get database
        ThisDatabaseHelper dbHelper = ThisDatabaseHelper.getDatabaseHelper();
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE,
                                    new String[]{});

        if (!cursor.moveToFirst()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        cursor.close();

        resetSpinners();

        if ((mStartIntent.getStringExtra(KEY_SERVER_NAME) != null) && (
                mStartIntent.getStringExtra(KEY_PROJECT_NAME) != null)) {//服务器名和项目名同时存在

            server = mStartIntent.getStringExtra(KEY_SERVER_NAME);
            project = mStartIntent.getStringExtra(KEY_PROJECT_NAME);

            setCookie(mStartIntent);

            //set serverSpinner default
            for (int i = 0; i < serverSpinnerString.size(); i++) {
                if (serverSpinnerString.get(i).equals(server)) {
                    serverSpinner.setSelection(i);
                    break;
                }
            }

            //set projectSpinner default
            if (projectSpinnerString != null) {
                for (int i = 0; i < projectSpinnerString.size(); i++) {
                    if (projectSpinnerString.get(i).equals(project)) {
                        projectSpinner.setSelection(i);
                        Log.d(TAG, "setData: SelectedItemPosition1 = "
                                + projectSpinner.getSelectedItemPosition());
                        break;
                    }
                }
            }

//            pullData();

        } else if ((mStartIntent.getStringExtra(KEY_SERVER_NAME) != null) && (
                mStartIntent.getStringExtra(KEY_PROJECT_NAME) == null)) {//项目名不存在
            server = mStartIntent.getStringExtra(KEY_SERVER_NAME);

            setCookie(mStartIntent);

            //set serverSpinner default
            for (int i = 0; i < serverSpinnerString.size(); i++) {
                if (serverSpinnerString.get(i).equals(server)) {
                    serverSpinner.setSelection(i);
                    break;
                }
            }

            if (!(projectSpinner.getCount() < 1)) {
                project = projectSpinner.getSelectedItem().toString();

                setCookie(mStartIntent);

                pullData();

            } else {
                Toast.makeText(this, "请在设置中输入项目名称", Toast.LENGTH_SHORT).show();
            }
        } else {

            //get default spinner's item
            server = serverSpinner.getSelectedItem().toString();
            project = projectSpinner.getSelectedItem().toString();

            setCookie(mStartIntent);

//            pullData();
        }

    }

    private void resetSpinners() {

        Cursor cursor = db.rawQuery("select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE,
                                    new String[]{});
        cursor.moveToFirst();

        Integer serverId = -1;

        do {
            if (mStartIntent.getStringExtra(KEY_SERVER_NAME) != null && mStartIntent.getStringExtra(
                    KEY_SERVER_NAME).equals(cursor.getString(cursor.getColumnIndex("server")))) {
                serverId = cursor.getInt(cursor.getColumnIndex("id"));
                break;
            }
        } while (cursor.moveToNext());

        cursor.moveToFirst();
        if (serverId == -1) {
            serverId = cursor.getInt(cursor.getColumnIndex("id"));
        }

        //set serverSpinnerString and serverSpinnerAdapter
        serverSpinnerString = new ArrayList<>();
        do {
            serverSpinnerString.add(cursor.getString(cursor.getColumnIndex("server")));
        } while (cursor.moveToNext());
        cursor.close();
        serverSpinnerAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_text_view, serverSpinnerString);
        serverSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        serverSpinner.setAdapter(serverSpinnerAdapter);
        serverNow = serverSpinnerString.get(0);

        //set projectSpinnerString and projectSpinnerAdapter
        projectSpinnerString = new ArrayList<>();
        Log.d(TAG, "onCreate: projectSpinnerString is not null");
        cursor = db.rawQuery(
                "select * from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE + " where serverId = ?",
                new String[]{serverId.toString()});

        if (cursor.moveToFirst()) {
            do {
                projectSpinnerString.add(cursor.getString(cursor.getColumnIndex("project")));
            } while (cursor.moveToNext());
            projectSpinnerAdapter =
                    new ArrayAdapter<>(this, R.layout.spinner_text_view, projectSpinnerString);
            projectSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            projectSpinner.setAdapter(projectSpinnerAdapter);
            projectNow = projectSpinnerString.get(0);
            Log.d(TAG, "setData: as i thought " + projectNow);
        }
    }

    private void setSpinnersOnClick() {
        serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                server = serverSpinnerString.get(position);
                Cursor cursor = db.rawQuery("select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE
                                                    + " where server = ?", new String[]{server});

                Integer serverId = -1;
                if (cursor.moveToFirst()) {
                    serverId = cursor.getInt(cursor.getColumnIndex("id"));
                }
                cursor.close();

                cursor = db.rawQuery("select * from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                             + " where serverId = ?",
                                     new String[]{serverId.toString()});
                if (cursor.moveToFirst()) {
                    project = cursor.getString(cursor.getColumnIndex("project"));

                    if ((!serverSpinnerString.get(position).equals(serverNow))
                            && (projectNow.equals(project))) {
                        serverNow = serverSpinnerString.get(position);
                    }

                    if (projectSpinnerString != null) {
                        cursor.close();
                        return;
                    }

                    projectSpinnerString = new ArrayList<>();
                    do {
                        projectSpinnerString.add(
                                cursor.getString(cursor.getColumnIndex("project")));
                    } while (cursor.moveToNext());
                    ArrayAdapter<String> projectSpinnerAdapter =
                            new ArrayAdapter<>(MainActivity.this, R.layout.spinner_text_view,
                                               projectSpinnerString);
                    projectSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    projectSpinner.setAdapter(projectSpinnerAdapter);

                    cursor.close();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        projectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                project = projectSpinnerString.get(position);
                if ((projectNow != null) && (!projectSpinnerString.get(position)
                        .equals(projectNow))) {
                    pullData();
                    Log.d(TAG, "onItemSelected: SelectedItemPosition2 = "
                            + projectSpinner.getSelectedItemPosition() + " " + project);
                    projectNow = projectSpinnerString.get(position);
                } else if (projectNow == null) {
                    projectNow = projectSpinnerString.get(position);
                } else if (projectNow != null && projectSpinnerString.get(position)
                        .equals(projectNow)) {
                    pullData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

    @OnClick(R.id.fabBtn)
    public void OnFabBtnClicked() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    public void rePullData() {
        pullData();
    }

    private void pullData() {

        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        Cursor cursor = db.rawQuery(
                "select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE + " where server = ?",
                new String[]{server});
        Integer serverId = -1;
        if (cursor.moveToFirst()) {
            serverId = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();

        cursor = db.rawQuery("select id from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
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

        Log.d(TAG, "pullData: project = " + project);
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
        viewDataList.clear();
        int idNow = 1;
        if (cursor.moveToFirst()) {
            for (int i = 1; ; i++) {
                addData(cursor.getInt(cursor.getColumnIndex("result")), i);
                if (!cursor.moveToNext()) {
                    idNow = i + 1;
                    break;
                }
            }
        }
        cursor.close();

        /**
         * pull data from server and add data to listView
         */
        final int finalIdNow = idNow;
        new Thread(new Runnable() {

            @Override
            public void run() {

                Integer id = finalIdNow;

                while (true) {
                    Response response = OkHttpConnection.GETconnecting(
                            OkHttpConnection.SpliceGetUrl(project, id, server), cookie);
                    try {
                        JsonData jsonData = OkHttpConnection.setJsonDataFromResponse(response);
                        if (jsonData == null && id == 1) {
                            Message message = new Message();
                            message.what = CONNECT_FALSE;
                            handler.sendMessage(message);
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

                            SharedPreferences sharedPreferences = getSharedPreferences(
                                    ThisDatabaseHelper.SWITCH_SHARED_PREFERENCES, MODE_PRIVATE);
                            if (finalIdNow > 1) {
                                testNumber = id;
                                if (result == 1 && sharedPreferences.getBoolean(
                                        ThisDatabaseHelper.BUILD_SUCCESS, true)) {
                                    sendMassageResult = true;
                                } else if (sharedPreferences.getBoolean(
                                        ThisDatabaseHelper.BUILD_FAILURE, true)) {
                                    sendMassageResult = false;
                                } else {
                                    continue;
                                }
                                Message message = new Message();
                                message.what = SEND_NOTIFICATION;
                                handler.sendMessage(message);
                            }

                        }
                    } catch (Exception e) {

                        Message message = new Message();
                        message.what = SET_REFRESH_FALSE;
                        handler.sendMessage(message);

                        Message message1 = new Message();
                        message1.what = UPDATE_TEXT;
                        handler.sendMessage(message1);
                        return;
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
        SharedPreferences sharedPreferences =
                getSharedPreferences(ThisDatabaseHelper.SWITCH_SHARED_PREFERENCES, MODE_PRIVATE);


        if (result == 1 && sharedPreferences.getBoolean(ThisDatabaseHelper.BUILD_SUCCESS, true)) {
            map.put("list_result", R.mipmap.result_y);
        } else if (result == 0 && sharedPreferences.getBoolean(ThisDatabaseHelper.BUILD_FAILURE,
                                                               true)) {
            map.put("list_result", R.mipmap.result_n);
        } else {
            return;
        }

        viewDataList.add(map);
    }

    /**
     * @deprecated
     */
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

    private void setCookie(Intent intent) {
        Cursor cursor;
        if (intent.getStringExtra(KEY_COOKIE) != null) {
            cookie = intent.getStringExtra(KEY_COOKIE);
        } else {
            /**
             * get the cookie from database
             */
            cursor = db.rawQuery(
                    "select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE + " where server = ?",
                    new String[]{server});
            Integer serverId = -1;
            if (cursor.moveToFirst()) {
                serverId = cursor.getInt(cursor.getColumnIndex("id"));
            }
            cursor.close();

            if (serverId != -1) {
                cursor = db.rawQuery(
                        "select * from " + ThisDatabaseHelper.ACCOUNT_TABLE + " where serverId = ?",
                        new String[]{serverId.toString()});
                if (cursor.moveToFirst()) {
                    cookie = cursor.getString(cursor.getColumnIndex("cookie"));
                }
                cursor.close();
            }
        }

        /**
         * test if cookie is available
         */


    }

    private void checkServerAndProject() {
        //check server
        Cursor cursor = db.rawQuery("select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE,
                                    new String[]{});
        if (cursor.moveToFirst()) {
            Integer serverId;
            while (true) {
                if (server.equals(cursor.getString(cursor.getColumnIndex("server")))) {
                    serverId = cursor.getInt(cursor.getColumnIndex("id"));
                    break;
                }
                if (!cursor.moveToNext()) {
                    cursor.moveToFirst();
                    mStartIntent.removeExtra(KEY_SERVER_NAME);
                    mStartIntent.putExtra(KEY_SERVER_NAME,
                                          cursor.getString(cursor.getColumnIndex("server")));
                    project = null;
                    if (!TextUtils.isEmpty(mStartIntent.getStringExtra(KEY_PROJECT_NAME))) {
                        mStartIntent.removeExtra(KEY_PROJECT_NAME);
                    }
                    cursor.close();
                    setData();
                    return;
                }
            }
            cursor.close();

            //check project
            cursor = db.rawQuery("select * from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                         + " where serverId = ?",
                                 new String[]{serverId.toString()});
            if (cursor.moveToFirst() && project != null) {
                while (true) {
                    if (project.equals(cursor.getString(cursor.getColumnIndex("project")))) {
                        resetSpinners();
                        break;
                    }
                    if (!cursor.moveToNext()) {
                        cursor.moveToFirst();
                        mStartIntent.removeExtra(KEY_PROJECT_NAME);
                        mStartIntent.putExtra(KEY_PROJECT_NAME,
                                              cursor.getString(cursor.getColumnIndex("project")));
                        cursor.close();
                        setData();
                        return;
                    }
                }
                cursor.close();
            } else {
                cursor.close();
                project = null;
                if (!TextUtils.isEmpty(mStartIntent.getStringExtra(KEY_PROJECT_NAME))) {
                    mStartIntent.removeExtra(KEY_PROJECT_NAME);
                }
                setData();
            }
        } else {
            cursor.close();
            server = null;
            project = null;
            setData();
        }
    }
}

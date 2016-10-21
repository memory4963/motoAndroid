package com.bolo4963gmail.motoandroid;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.bolo4963gmail.motoandroid.javaClass.JsonData;
import com.bolo4963gmail.motoandroid.javaClass.OkHttpConnection;
import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class MotoAndroidPullDataService extends Service {

    private static final String TAG = "Service";

    private MyBinder mBinder;
    private MainActivity mActivity;
    private SQLiteDatabase db;
    private boolean sendMassageResult = true;

    private final int SEND_NOTIFICATION = 0;
    private final int PULL_DATA = 1;

    private Handler handler = new Handler() {

        int notificationId = 0;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_NOTIFICATION:

                    String project = msg.getData().getString("project");
                    String server = msg.getData().getString("server");
                    Integer id = msg.getData().getInt("id");

                    NotificationManager manager =
                            (NotificationManager) MotoAndroidPullDataService.this.getSystemService(
                                    Context.NOTIFICATION_SERVICE);
                    Intent intent = new Intent(MotoAndroidPullDataService.this, MainActivity.class);
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(MotoAndroidPullDataService.this, 0, intent,
                                                      PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(
                            MotoAndroidPullDataService.this).setContentTitle("MotoAndroid")
                            .setContentText(id.toString() + " " + project + " " + server)
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
                case PULL_DATA:
                    mActivity.rePullData();
                    break;
                default:
                    break;
            }
        }

    };

    class MyBinder extends Binder {

        public void setActivity(MainActivity mainActivity) {
            mActivity = mainActivity;
        }

        public void removeActivity() {
            if (mActivity != null) {
                mActivity = null;
            }
        }

    }

    public MotoAndroidPullDataService() {
    }

    /*
    MainActivity种绑定service
    定时拉取数据，如果有新数据，存入数据库，发出通知
    Activity如果在工作，发送通知让它重新pullData
     */

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ThisDatabaseHelper thisDatabaseHelper = ThisDatabaseHelper.getDatabaseHelper();
        db = thisDatabaseHelper.getWritableDatabase();
        Log.d(TAG, "onCreate: created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: started");
        new Thread(new Runnable() {

            @Override
            public void run() {
                pullData();
            }
        }).start();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long interval = 2 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + interval;
        Intent intent1 = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent1, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /*
    遍历数据库
    循环server，循环project
    每个都要拉取，有新的build信息就发出通知，把这个新的build添加进数据库
    MainActivity运行的时候通知它刷新
     */
    private void pullData() {

        final List<String> servers = new ArrayList<>();
        final List<String> projects = new ArrayList<>();
        final List<String> cookies = new ArrayList<>();

        Cursor serverCursor = db.rawQuery("select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE,
                                          new String[]{});
        // TODO: 2016/10/21 修改cookie储存方式

        if (serverCursor.moveToFirst()) {
            do {
                servers.add(serverCursor.getString(serverCursor.getColumnIndex("server")));
                Integer serverId = serverCursor.getInt(serverCursor.getColumnIndex("id"));
                Cursor accountCursor = db.rawQuery(
                        "select * from " + ThisDatabaseHelper.ACCOUNT_TABLE + " where serverId = ?",
                        new String[]{serverId.toString()});
                if (accountCursor.moveToFirst()) {
                    cookies.add(accountCursor.getString(accountCursor.getColumnIndex("cookie")));
                }
                accountCursor.close();
            } while (serverCursor.moveToNext());
        } else {
            serverCursor.close();
            return;
        }

        serverCursor.moveToFirst();

        for (int i = 0; i < servers.size(); i++) {

            Integer serverId = serverCursor.getInt(serverCursor.getColumnIndex("id"));

            Cursor projectCursor = db.rawQuery(
                    "select * from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                            + " where serverId = ?", new String[]{serverId.toString()});

            projects.clear();
            if (projectCursor.moveToFirst()) {
                do {
                    projects.add(projectCursor.getString(projectCursor.getColumnIndex("project")));
                } while (projectCursor.moveToNext());
            } else {
                continue;
            }

            for (int j = 0; j < projects.size(); j++) {

                final Integer projectId = serverCursor.getInt(serverCursor.getColumnIndex("id"));

                /**
                 * get data from result table
                 */
                Cursor resultCursor = db.rawQuery(
                        "select result from " + ThisDatabaseHelper.RESULT_TABLE
                                + " where projectId = ?", new String[]{projectId.toString()});

                /**
                 * add data to listView if it has data
                 */
                int idNow = 1;
                if (resultCursor.moveToFirst()) {
                    do {
                        idNow += 1;
                    } while (resultCursor.moveToNext());
                }
                resultCursor.close();

                /**
                 * pull data from server and add data to listView
                 */
                Integer id = idNow;

                while (true) {
                    Response response = OkHttpConnection.GETconnecting(
                            OkHttpConnection.SpliceGetUrl(projects.get(j), id, servers.get(i)),
                            cookies.get(i));
                    try {
                        JsonData jsonData = OkHttpConnection.setJsonDataFromResponse(response);
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
                        id++;

                        SharedPreferences sharedPreferences =
                                getSharedPreferences(ThisDatabaseHelper.SWITCH_SHARED_PREFERENCES,
                                                     MODE_PRIVATE);
                        if (idNow > 1) {
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
                            message.getData().putString("project", projects.get(j));
                            message.getData().putString("server", servers.get(i));
                            message.getData().putInt("id", id - 1);
                            message.what = SEND_NOTIFICATION;
                            handler.sendMessage(message);
                        }

                    } catch (Exception e) {

                        if (mActivity != null && !(mActivity.isDestroyed()
                                || (mActivity.isFinishing()))) {

                            Message message = new Message();
                            message.what = PULL_DATA;
                            handler.sendMessage(message);
                        }

                        break;
                    }

                }

                if (!projectCursor.moveToNext()) {
                    projectCursor.close();
                }
            }
            if (!serverCursor.moveToNext()) {
                serverCursor.close();
            }
        }
    }
}


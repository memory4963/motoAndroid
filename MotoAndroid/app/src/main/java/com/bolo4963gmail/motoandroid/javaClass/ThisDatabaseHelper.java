package com.bolo4963gmail.motoandroid.javaClass;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bolo4963gmail.motoandroid.App;

/**
 * Created by 10733 on 2016/8/3.
 */
public class ThisDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "ThisDatabaseHelper";

    private static final String ORIGINAL_DATABASE = "MotoAndroid.db";

    public static final String SERVER_NAMES_TABLE = "ServerName";
    public static final String PROJECT_NAMES_TABLE = "ProjectName";
    public static final String RESULT_TABLE = "Result";

    private static final int DB_VERSION = 1;

    private SQLiteDatabase db;

    private ThisDatabaseHelper() {
        super(App.getContext(), ORIGINAL_DATABASE, null, DB_VERSION);
        Log.d(TAG, "ThisDatabaseHelper: ThisDatabaseHelper has created");
        db = getWritableDatabase();
    }

    public static ThisDatabaseHelper getDatabaseHelper() {
        if (DBHelperController.thisDatabaseHelper == null) {
            DBHelperController.thisDatabaseHelper = new ThisDatabaseHelper();
        }
        return DBHelperController.thisDatabaseHelper;
    }

    private static class DBHelperController {

        private static ThisDatabaseHelper thisDatabaseHelper = null;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createServerNameTable(db);
        createProjectNameTale(db);
        createResultTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * creates ServerName table which contents two things: id and server
     */
    public void createServerNameTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + SERVER_NAMES_TABLE + " ("
                           + "id integer primary key autoincrement, " + "server text)");
    }

    /**
     * creates ProjectName table which contents three things: id and project and serverId
     */
    public void createProjectNameTale(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + PROJECT_NAMES_TABLE + "("
                           + "id integer primary key autoincrement, " + "project text, "
                           + "serverId integer)");
    }

    public void createResultTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + RESULT_TABLE + "("
                           + "id integer primary key autoincrement, " + "result integer, "
                           + "projectId integer)");
    }
}

package com.bolo4963gmail.motoandroid.javaClass;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bolo4963gmail.motoandroid.MainActivity;

/**
 * Created by 10733 on 2016/8/3.
 */
public class ThisDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "ThisDatabaseHelper";

    private String createTable = "";

    private Context mContext;

    public ThisDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, String tableName) {
        super(context, name, factory, version);
        setNewTableName(tableName);
        mContext = context;
        Log.d(TAG, "ThisDatabaseHelper: ThisDatabaseHelper has created");
    }

    public ThisDatabaseHelper(Context context,
                              String name,
                              SQLiteDatabase.CursorFactory factory,
                              int version,
                              String tableName,
                              boolean truth) {

        super(context, name, factory, version);
        mContext = context;
        createTable = tableName;
        Log.d(TAG, "ThisDatabaseHelper: ThisDatabaseHelper has created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);
        if (createTable.equals(
                "create table if not exists AddressName ("
                        + "id integer primary key autoincrement, "
                        + "address text)")) {
            MainActivity.ifFirstTime = true;
            Log.d(TAG, "onCreate: OK");
        } else {
            Log.d(TAG, "onCreate: createTable : " + createTable);
        }
    }

    public void setNewTableName(String name) {
        createTable = "create table if not exists " + name + " ("
                + "id integer primary key autoincrement, "
                + "result integer, "
                + "address text, "
                + "project_name text)";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

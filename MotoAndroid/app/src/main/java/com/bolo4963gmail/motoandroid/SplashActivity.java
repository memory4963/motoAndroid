package com.bolo4963gmail.motoandroid;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

import static com.bolo4963gmail.motoandroid.R.layout.layout_splash;

/**
 * Created by 10733 on 2016/7/30.
 */
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";

    private ThisDatabaseHelper dbHelper;

    private boolean ifBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout_splash);
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        if (!ifBackPressed) {
            Log.d(TAG, "onCreate: mainactivity has started");
            dbHelper = new ThisDatabaseHelper(this, "MotoAndroid.db", null, 1,
                                              "create table if not exists AddressName ("
                                                      + "id integer primary key autoincrement, "
                                                      + "address text)", true);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("insert into AddressName (address) values(?)", new String[]{"default"});
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        ifBackPressed = true;
        finish();
    }
}

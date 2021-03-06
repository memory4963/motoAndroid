package com.bolo4963gmail.motoandroid;

import android.content.Intent;
import android.content.SharedPreferences;
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
        if (!ifBackPressed) {
            Log.d(TAG, "onCreate: mainActivity has started");

            /**
             * create database
             */
            ThisDatabaseHelper.getDatabaseHelper();

            SharedPreferences.Editor editor =
                    getSharedPreferences(ThisDatabaseHelper.SWITCH_SHARED_PREFERENCES, MODE_PRIVATE)
                            .edit();
            editor.putBoolean(ThisDatabaseHelper.BUILD_SUCCESS, true);
            editor.putBoolean(ThisDatabaseHelper.BUILD_FAILURE, true);
            editor.apply();

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
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

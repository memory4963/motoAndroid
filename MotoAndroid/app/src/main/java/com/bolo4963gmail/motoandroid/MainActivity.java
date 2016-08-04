package com.bolo4963gmail.motoandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.Connection;
import com.bolo4963gmail.motoandroid.javaClass.JsonData;

import java.net.URL;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    public static boolean ifFirstTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.list_view);

        if (ifFirstTime) {
            ifFirstTime = false;
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        Intent intent = getIntent();
        final String urlStr;
        final String projectName;

        if (((urlStr = intent.getStringExtra("urlStr")) != null) && (
                (projectName = intent.getStringExtra("projectName")) != null)) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    Integer i = 1;
                    while (true) {
                        URL Address = Connection.SpliceURL(projectName, i, urlStr);
                        JsonData jsonData = Connection.connecting(Address);
                        if (jsonData == null && i == 1) {

                        }
                    }

                }
            }).start();
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

    public static void startAction(Context context, String urlStr, String projectName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("urlStr", urlStr);
        intent.putExtra("projectName", projectName);
        context.startActivity(intent);
    }
}

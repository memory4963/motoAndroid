package com.bolo4963gmail.motoandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.Connection;

/**
 * Created by 10733 on 2016/8/4.
 */
public class LoginWebViewActivity extends BaseActivity {

    private String urlStr;

    private String projectName;

    private static final String TAG = "LoginWebViewActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_webview);

        Intent intent = getIntent();
        urlStr = intent.getStringExtra("urlStr");
        projectName = intent.getStringExtra("projectName");
        String urlString;

        boolean ifWrong = intent.getBooleanExtra("ifWrong", false);

        WebView webView = (WebView) findViewById(R.id.web_view_login);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        if (ifWrong) {
            urlString = Connection.SpliceString(projectName, 1, urlStr);
            Toast.makeText(this, "连接服务器出错", Toast.LENGTH_LONG).show();
        } else {
            urlString = urlStr;
            Toast.makeText(this, "记得勾选“在这台计算机上保持登录状态”哦~", Toast.LENGTH_LONG).show();
        }
        webView.loadUrl(urlString);

        ImageView webViewBack = (ImageView) findViewById(R.id.login_webview_back);
        webViewBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.startAction(LoginWebViewActivity.this, urlStr, projectName);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        MainActivity.startAction(LoginWebViewActivity.this, urlStr, projectName);
        finish();
    }

    public static void actionStart(Context context, String urlStr, String projectName,boolean ifWrong) {
        Intent intent = new Intent(context, LoginWebViewActivity.class);
        intent.putExtra("urlStr", urlStr);
        intent.putExtra("projectName", projectName);
        if (ifWrong) {
            intent.putExtra("ifWrong", true);
        } else {
            intent.putExtra("ifWrong", false);
        }
        context.startActivity(intent);
    }


}

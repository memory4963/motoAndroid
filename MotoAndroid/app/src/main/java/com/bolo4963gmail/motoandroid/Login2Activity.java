package com.bolo4963gmail.motoandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by 10733 on 2016/8/23.
 */
public class Login2Activity extends BaseActivity {

    private ImageView back = null;
    private EditText account = null;
    private EditText password = null;
    private Button login = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        back = (ImageView) findViewById(R.id.login_back2);
        account = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login_button);


    }

    public static void startAction(Context context, String url, String projectName) {
        Intent intent = new Intent(context, Login2Activity.class);
        intent.putExtra("url", url);
        intent.putExtra("projectName", projectName);
        context.startActivity(intent);
    }

}

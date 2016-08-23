package com.bolo4963gmail.motoandroid;

import com.bolo4963gmail.motoandroid.javaClass.JsonData;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 *
 * Created by 10733 on 2016/8/21.
 */
public class LearnHttp {

    public static void connection(String username, String pwd) {

        FormBody body = new FormBody.Builder().add("j_username", username).add("j_password", pwd)
//                .add("Jenkins-Crumb", "4d2f4ef3621fb58674b2e6afb226bbff")
                .build();

        Request request = new Request.Builder().url("http://115.29.114.77/j_acegi_security_check")
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();
        Call call = client.newCall(request);

//        call.enqueue(new Callback() {
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//        });

        try {
            Response response = call.execute();

            int code = response.code();
            if (code == 302) {
                Headers headers = response.headers();
                String setCookie = headers.get("Set-Cookie");
                String[] split = setCookie.split(";");
                String cookie = split[0];

                // cookie

                getJson(client, cookie);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getJson(OkHttpClient client, String cookie) throws IOException {
        Request request = new Request.Builder().url(
                "http://115.29.114.77/job/testOne/1/api/json?pretty=true").addHeader("Cookie", cookie).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            String string = body.string();

            JsonData jsonData = new Gson().fromJson(string, JsonData.class);
            String jsonData_class = jsonData.get_class();

            System.out.println(jsonData_class);
        }
    }
}

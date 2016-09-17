package com.bolo4963gmail.motoandroid.javaClass;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 10733 on 2016/8/31.
 */
public class OkHttpConnection {

    private static final String TAG = "OkHttpConnection";

    public static final String EXTRASTR_FIRST = "job/";
    public static final String EXTRASTR_SECONED = "/api/json?pretty=true";
    public static final String LOGIN_URL = "j_acegi_security_check";

    public static Response response = null;

    @Deprecated
    @Nullable
    public static URL SpliceURL(String projectName, Integer num, String urlStr) {
        try {

            Log.d(TAG, "SpliceURL: urlStr:" + urlStr);
            Log.d(TAG, "SpliceURL: projectName:" + projectName);
            String urlString;
            if (!((urlStr.substring(urlStr.length() - 1)).equals("/"))) {
                urlString = "http://" + urlStr + "/";
            } else {
                urlString = "http://" + urlStr;
            }

            if (!TextUtils.isEmpty(projectName)) {
                urlString += EXTRASTR_FIRST + projectName + "/" + num.toString() + EXTRASTR_SECONED;
            }

            return new URL(urlString);//还需要确定。。。。

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, "SpliceURL: returned null");
            return null;
        }
    }

    @Nullable
    public static String SpliceLoginUrl(String urlStr) {
        try {
            String urlString;
            if (!((urlStr.substring(urlStr.length() - 1)).equals("/"))) {
                urlString = "http://" + urlStr + "/";
            } else {
                urlString = "http://" + urlStr;
            }
            return urlString + LOGIN_URL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static String SpliceGetUrl(String projectName, Integer num, String urlStr) {
        try {
            String urlString;
            if (!((urlStr.substring(urlStr.length() - 1)).equals("/"))) {
                urlString = "http://" + urlStr + "/";
            } else {
                urlString = "http://" + urlStr;
            }

            return TextUtils.isEmpty(projectName) ? urlString
                    : (urlString + EXTRASTR_FIRST + projectName + "/" + num.toString()
                            + EXTRASTR_SECONED);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * need to run in a thread
     */
    @Nullable
    public static Response GETconnecting(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * need to run in a thread
     */
    @Nullable
    public static Response GETconnecting(String url, String cookie) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).addHeader("Cookie", cookie).build();
        try {
            return client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * need to run in a thread
     * list need two variables : username and password
     */
    @Nullable
    public static Response POSTconnection(String url, List<String> list) {
        OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();

        RequestBody formBody = new FormBody.Builder().add("j_username", list.get(0))
                .add("j_password", list.get(1))
                .add("json",
                     "{\"j_username\": \"" + list.get(0) + "\", \"j_password\": \"" + list.get(1)
                             + "\", \"remember_me\": true, \"from\": \"/\"}")
                .add("Submit", "登录")
                .build();

        Request request = new Request.Builder().url(url).post(formBody).build();

        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonData setJsonDataFromResponse(Response response) {
        Gson gson = new Gson();
        return gson.fromJson(response.body().charStream(), JsonData.class);
    }

    public static String getCoockies(Response response) {
        if (response != null) {
            return response.header("Cookie");
        }
        return null;
    }

    public static JsonData parseJSONWithGSON(String result) {
        JsonData jsonData = null;
        try {
            if (!(result.equals(""))) {
                Gson gson = new Gson();
                jsonData = gson.fromJson(result, new TypeToken<JsonData>() {

                }.getType());
                if (jsonData == null) {
                    NullPointerException e = new NullPointerException();
                    throw e;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "parseJSONWithGSON: account haven't login");
            e.printStackTrace();
            Log.d(TAG, "parseJSONWithGSON: jsonData is null");
            return null;
        }
        Log.d(TAG, "parseJSONWithGSON: jsonData is not null");
        return jsonData;
    }

//    private static class mRunnable implements Runnable {
//
//        public static final int CONNECTING = 0;
//        public static final int CONNECTION_SUCCESSFUL = 1;
//        public static final int CONNECTION_FAILURE = 2;
//
//        private String url = null;
//        private List<String> list = null;
//        private int isSuccessful = CONNECTING;
//
//        public int getSuccessful() {
//            return isSuccessful;
//        }
//
//        public Response getResponse() {
//            return response;
//        }
//
//        private Response response = null;
//
//        public mRunnable(String url) {
//            this.url = url;
//        }
//
//        public mRunnable(String url, List<String> list) {
//            this.url = url;
//            this.list = list;
//        }
//
//        @Override
//        public void run() {
//            try {
//                Log.d(TAG, "run: begin try");
//                OkHttpClient client = new OkHttpClient().newBuilder()
//                        .connectTimeout(10, TimeUnit.SECONDS)
//                        .writeTimeout(10, TimeUnit.SECONDS)
//                        .readTimeout(30, TimeUnit.SECONDS)
//                        .build();
//                Request request = new Request.Builder().url(url).build();
//
//                if (list != null) {
//                    for (int i = 0; i < list.size(); i += 2) {
//                        request.newBuilder().addHeader(list.get(i), list.get(i + 1));
//                    }
//                }
//                Calendar calendar = Calendar.getInstance();
//                Log.d(TAG, "run: time is " + calendar.get(Calendar.SECOND));
//
//                response = client.newCall(request).execute();
//
//                calendar = Calendar.getInstance();
//                Log.d(TAG, "run: after execute, the time is " + calendar.get(Calendar.SECOND));
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "run: isSuccessful is successful");
//                    isSuccessful = CONNECTION_SUCCESSFUL;
//                } else {
//                    Log.d(TAG, "run: isSuccessful is failure");
//                    isSuccessful = CONNECTION_FAILURE;
//                }
//                return;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            response = null;
//
//        }
//    }

}

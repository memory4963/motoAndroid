package com.bolo4963gmail.motoandroid.javaClass;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 10733 on 2016/8/3.
 */
public class Connection {

    private static final String TAG = "Connection";

    private static String responseAll;

    private static HttpURLConnection connection;

    public static final String EXTRASTRFIRST = "job/";

    public static final String EXTRASTRSECONED = "/api/json?pretty=true";

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

            String completeStr =
                    urlString + Connection.EXTRASTRFIRST + projectName + "/" + num.toString()
                            + Connection.EXTRASTRSECONED;

            URL url = new URL(completeStr);//还需要确定。。。。

            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String SpliceString(String projectName, Integer num, String urlStr) {
        try {

            String urlString;
            if (!((urlStr.substring(urlStr.length() - 1)).equals("/"))) {
                urlString = urlStr + "/";
            } else {
                urlString = urlStr;
            }

            return (urlString + Connection.EXTRASTRFIRST + projectName + "/" + num.toString()
                    + Connection.EXTRASTRSECONED);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonData connecting(final URL url) {

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            responseAll = response.toString();
            Log.d(TAG, "connecting: (parseJSONWithGSON方法已运行)responseAll:" + responseAll);

            return parseJSONWithGSON(responseAll);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    private static JsonData parseJSONWithGSON(String result) {
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

}

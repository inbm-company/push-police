package kr.go.police.bgidocsubmit;

import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class _web {

    static OkHttpClient mClient;
    static Request request;
    
    //Singleton 
    public static OkHttpClient okHttpClient() {
        if (mClient == null) {

            mClient = new OkHttpClient();
//                    .connectTimeout(20, TimeUnit.SECONDS)
//                    .writeTimeout(20, TimeUnit.SECONDS)
//                    .readTimeout(20, TimeUnit.SECONDS)
//                    .build();
        }
        return mClient;
    }

    public static String get(String url) throws IOException {
        String html;
        
        OkHttpClient client = okHttpClient();

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        html = response.body().string();

        return html;
    }

    public static String post(String url, String json) {
        String html = "";
        Log.d("in post", url);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = okHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();

        Response response;

        try {
            response = client.newCall(request).execute();
            html = response.body().string();
        } catch (IOException e) {
            _log.m("post error");
        }

        return html;
    }


    //TODO REVIEW
    public static String delete(String url, String json) {
        String html = "";
        
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = okHttpClient();

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).delete(body).build();

        Response response;

        try {
            response = client.newCall(request).execute();
            html = response.body().string();
        } catch (IOException e) {
            _log.m("IOException");
        }
        return html;
    }


    public static String postWithHeaders(String url, String json, String headers) {
        String html = "";
        
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = okHttpClient();

        RequestBody body = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();

        JSONObject header = null;
        try {
            header = new JSONObject(headers);
            Iterator<String> header_keys = header.keys();
            while (header_keys.hasNext()) {
                String header_key = header_keys.next();
                builder.addHeader(header_key, header.getString(header_key));
            }
        } catch (JSONException e) {
            _log.m("JSONException");
        }

        Request request = builder.url(url).post(body).build();
        Response response;

        try {
            response = client.newCall(request).execute();
            html = response.body().string();
        } catch (IOException e) {
            _log.m("IOException");
        }
        return html;
    }

    public static String getWithHeaders(String url, String json) {
        String html = "";
        
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = okHttpClient();

        Request.Builder builder = new Request.Builder();

        JSONObject header = null;
        try {
            header = new JSONObject(json);
            Iterator<String> header_keys = header.keys();
            while (header_keys.hasNext()) {
                String header_key = header_keys.next();
                builder.addHeader(header_key, header.getString(header_key));
            }
        } catch (JSONException e) {
            _log.m("JSONException");
        }

        Request request = builder.url(url).build();

        Response response;

        try {
            response = client.newCall(request).execute();
            html = response.body().string();
        } catch (IOException e) {
            _log.m("IOException");
        }
        return html;
    }


    public static String deleteWithHeaders(String url, String json, String headers) {
        String html = "";
        
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = okHttpClient();

        RequestBody body = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();

        JSONObject header = null;
        try {
            header = new JSONObject(headers);
            Iterator<String> header_keys = header.keys();
            while (header_keys.hasNext()) {
                String header_key = header_keys.next();
                builder.addHeader(header_key, header.getString(header_key));
            }
        } catch (JSONException e) {
            _log.m("JSONException");
        }

        Request request = builder.url(url).delete(body).build();
        Response response;

        try {
            response = client.newCall(request).execute();
            html = response.body().string();
        } catch (IOException e) {
            _log.m("IOException");
        }
        return html;
    }
}
package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity {
    private String serverUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _log.simple("notice test");
        new Thread(() -> {
            try {
//                String result = _web.get(serverUrl+"getAllowed?id="+userId);

//                JSONObject resultObj = new JSONObject(result);
//                Boolean isAllowed = (Boolean) resultObj.get("isAllowed");
//                _log.simple("is allow"+  isAllowed);
//                setCheckBoxValue(isAllowed);

            } catch (Exception e) {
                _log.e(e.getMessage());
            }
        }).start();
    }
}

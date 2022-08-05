package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class SettingActivity extends AppCompatActivity {
    private final String serverUrl = "http://192.168.10.152:3000/";
    private CheckBox checkBox;
    private String userId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        checkBox = findViewById(R.id.allowPushCheck);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    setIsAllowedPush();
                } catch (JSONException e) {
                    _log.e(e.getMessage());
                }
            }
        });


        getIsAllawedPush();
    }

    public void clickBack(View view){
        onBackPressed();
    }

//    현재 사용자의 푸시 허용상태를 가져옴
    public void getIsAllawedPush(){
        new Thread(() -> {
            try {
              String result = _web.get(serverUrl+"getAllowed?id="+userId);

              JSONObject resultObj = new JSONObject(result);
              Boolean isAllowed = (Boolean) resultObj.get("isAllowed");
              _log.simple("is allow"+  isAllowed);
              setCheckBoxValue(isAllowed);

            } catch (Exception e) {
                _log.e(e.getMessage());
            }
        }).start();
    }

    //    현재 사용자의 푸시 허용상태를 바꾸는것을 요청함
    public void setIsAllowedPush() throws JSONException {
        Boolean allow = checkBox.isChecked();
        JSONObject paramObject = new JSONObject();
        paramObject.put("userId", userId);
        paramObject.put("isAllowed", allow);
        Log.d("sssssssssss", String.valueOf(paramObject) );
        new Thread(() -> {
            try {
                _web.post(serverUrl+"setAllowed","{\"userId\":\"userid04\",\"isAllowed\":false}");
            } catch (Exception e) {
                _log.e(e.getMessage());
            }
        }).start();
    }

    public void setCheckBoxValue (Boolean isAllow) {
        checkBox.setChecked(isAllow);
    }
}

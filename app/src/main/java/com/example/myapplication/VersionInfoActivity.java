package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class VersionInfoActivity extends AppCompatActivity {
    private final String serverUrl = "http://192.168.10.152:3000/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        initDisplay();
    }

    public void initDisplay() {
        TextView tvNoti = findViewById(R.id.tv_noti);
        TextView tvVersion = findViewById(R.id.tv_version);
        Button btnUpdate = findViewById(R.id.btn_update);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

//    public void clickBack(View view){
//        onBackPressed();
//    }

//    현재 사용자의 푸시 허용상태를 가져옴
//    public void getIsAllawedPush(){
//        new Thread(() -> {
//            try {
//              String result = _web.get(serverUrl+"getAllowed?id="+userId);
//
//              JSONObject resultObj = new JSONObject(result);
//              Boolean isAllowed = (Boolean) resultObj.get("isAllowed");
//              _log.simple("is allow"+  isAllowed);
//              setCheckBoxValue(isAllowed);
//
//            } catch (Exception e) {
//                _log.e(e.getMessage());
//            }
//        }).start();
//    }

    //    현재 사용자의 푸시 허용상태를 바꾸는것을 요청함
//    public void setIsAllowedPush() throws JSONException {
//        Boolean allow = checkBox.isChecked();
//        JSONObject paramObject = new JSONObject();
//        paramObject.put("userId", userId);
//        paramObject.put("isAllowed", allow);
//        Log.d("sssssssssss", String.valueOf(paramObject) );
//        new Thread(() -> {
//            try {
//                _web.post(serverUrl+"setAllowed","{\"userId\":\"userid04\",\"isAllowed\":false}");
//            } catch (Exception e) {
//                _log.e(e.getMessage());
//            }
//        }).start();
//    }
}

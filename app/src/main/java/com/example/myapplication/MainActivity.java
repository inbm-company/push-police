package com.example.myapplication;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private WebView webView;
    private FrameLayout frameLayout;

    private final String serverUrl = "http://192.168.10.152:3000/";

    private final String homeUrl = "http://192.168.10.152:5500/";
    private final String noticeUrl = "http://192.168.10.152:5500/notice";
    private final String chatbotUrl = "http://192.168.10.152:5500/chatbot";
    private final String purchaseUrl = "http://192.168.10.152:5500/";
    private final String pushHistoryUrl = "http://192.168.10.152:5500/history";

    private String url = homeUrl;

    private AndroidBridge androidBridge;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Boolean doClickEvent = intent.getBooleanExtra("notiClick",false);

//        push 클릭하여 앱을 실행할경우 이벤트 시행
        if(doClickEvent){
            notiClickEvent();
        }

        setLayout();
        setWebView();

//        토큰이 새로 발행되었는데 onNewToken이 발생하지 않을경우 호출 할것
        uploadToken();
//        현재 테스트를위해 web의 호출이 없더라도 splash 화면을 지움
        removeSplashScreen();
    }

    private void setLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerView);
        frameLayout = (FrameLayout)  findViewById(R.id.mainFrameLayout);
    }

    protected void removeSplashScreen(){
        frameLayout.removeAllViews();
    }

    private void setWebView() {
        webView = (WebView) findViewById(R.id.mainWebView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClientClass());

        androidBridge = new AndroidBridge(webView, MainActivity.this);

        webView.addJavascriptInterface(androidBridge, "Android");
    }


    private void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void  closeDrawer(DrawerLayout drawerLayout){
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void clickMenu(View view) {
        openDrawer(drawerLayout);
    }


    public void setUserId(String id) {
        _log.simple("set id"+id);
        this.userId = id;
    }

    public String getUserId() {
        return userId;
    }

    public void notiClickEvent(){
        new Thread(()->{
            try {
                _web.post(serverUrl+"read","{\"userId\":\"userid04\",\"msgId\":\"msg1234\"}");
            } catch (Exception e) {
                _log.e(e.getMessage());
            }
        }).start();
    }

//=======================================================================
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void clickHome(View view){
        changeUrl(homeUrl);
        closeDrawer(drawerLayout);
    }

    public void clickNotice(View view) {
        changeUrl(noticeUrl);
        closeDrawer(drawerLayout);
    }

    public void clickChatbot(View view){
        changeUrl(chatbotUrl);
        closeDrawer(drawerLayout);
    }

    public void clickPurchase(View view){
        changeUrl(purchaseUrl);
        closeDrawer(drawerLayout);
    }

    public void clickPushHistory (View view){
        changeUrl(pushHistoryUrl);
        closeDrawer(drawerLayout);
    }

    private void changeUrl(String newUrl){
        androidBridge.setUrl(newUrl);
    }

    public void clickSetting(View view) {
        Intent intent = new Intent(this, SettingActivity.class );
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    public void clickLogout(View view) {
        androidBridge.callJsLogout();
    }



    private static class WebViewClientClass extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void uploadToken(){
//        현재 기기의 토큰값을 가져옴
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if(!task.isSuccessful()){
                    Log.e("fcm fail get token", "fail to get token");
                    return;
                }
                String token = task.getResult();
                Log.d("fcm token onComplete", token);
                new Thread(()->{
                    Log.d("token to server" , token);
//                    TODO: body 로 묶기 {userId, token }
                    try {
                        _web.post(serverUrl+"token", "{\"userId\":\"userid04\",\"token\": \""+token +"\"}");
                    } catch (Exception e) {
                        _log.e(e.getMessage());
                    }
                }).start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("err:failed get token", e.getLocalizedMessage());
            }
        });
    }

}
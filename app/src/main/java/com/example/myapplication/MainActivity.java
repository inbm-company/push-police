package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private WebView webView;
    private FrameLayout frameLayout;

    private final String serverUrl = "http://192.168.10.152:3000/";

    private final String homeUrl = "https://bgi.police.go.kr/police/";
    private final String noticeUrl = homeUrl+"notice.do";
    private final String chatbotUrl = homeUrl+"user-chatbot.do";
    private final String purchaseUrl = homeUrl+"user-sc.do";
    private final String pushHistoryUrl = homeUrl+"user-notification.do";

    private String url = homeUrl+"user-login.do";

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
//        uploadToken();
//        현재 테스트를위해 web의 호출이 없더라도 splash 화면을 지움
  //            removeSplashScreen();
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

        // javaScript 허용
        webView.getSettings().setJavaScriptEnabled(true);
        // local storage 허용
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");

        webView.loadUrl(url);
        androidBridge = new AndroidBridge(webView, MainActivity.this);

        // for webview debugging in chrome
        webView.setWebContentsDebuggingEnabled(true);
        webView.setWebChromeClient(new WebChromeClientClass());
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                _log.e("setWebView onPageStarted url:: "+ url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                _log.e("setWebView onPageStarted url:: "+ url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                _log.e("setWebView onReceivedError error:: "+ error.getErrorCode());
            }
        });

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
        Intent intent = new Intent(this, VersionInfoActivity.class );
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    public void clickLogout(View view) {
        androidBridge.clickLogout();
    }

    private static class WebChromeClientClass extends WebChromeClient {

        @Override public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        public boolean onConsoleMessage(ConsoleMessage cm) {
            _log.simple("MyApplication"+cm.message() + " -- From line "+ cm.lineNumber() + " of "+ cm.sourceId() );
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
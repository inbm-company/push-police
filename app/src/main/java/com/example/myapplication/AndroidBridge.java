package com.example.myapplication;

import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;


public class AndroidBridge {

    private String TAG = "AndroidBridge";
    final public Handler handler = new Handler();

    private WebView webView;
    private MainActivity mainActivity;

    public AndroidBridge(WebView _webView, MainActivity _mainActivity) {
        webView = _webView;
        mainActivity = _mainActivity;
    }

    public void setUrl(String url){
        webView.loadUrl(url);
    }

    // web 로그인 페이지 로딩 완료
    @JavascriptInterface
    public void readyWebview( ){
        _log.simple("로그인 페이지");
        Toast.makeText(mainActivity.getApplicationContext(), "로그인 페이지", Toast.LENGTH_SHORT).show();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mainActivity.removeSplashScreen();
            }
        });
    }

    // web 로그인 완료 후 userID 전달
    @JavascriptInterface
    public void readyWebview(String userID ){
        Toast.makeText(mainActivity.getApplicationContext(), userID, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void clickLogout(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:logout()");
            }
        });
    }

// web에서 Android.showToast() 호출시 앱에서 토스트 뛰움
    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(mainActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

    }

// web에서 렌더링 완료시 Android.onReady() 호출
    @JavascriptInterface
    public void onReady(String userId) {
        mainActivity.setUserId(userId);
        mainActivity.removeSplashScreen();

    }

}

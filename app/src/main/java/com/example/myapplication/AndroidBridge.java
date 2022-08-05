package com.example.myapplication;

import android.os.Handler;
import android.util.Log;
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

//    web에 로그를 찍어줌
    @JavascriptInterface
    public void callJsLog( final String _message){
        Log.d(TAG, _message);
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:alert('["+ _message +"]   ')");
            }
        });
    }

//   web의 로그아웃 기능 호출
    @JavascriptInterface
    public void callJsLogout(){
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

package com.example.myapplication;

import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
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
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mainActivity.setUserId(userID);
                // 토큰 정보 전달
                mainActivity.uploadToken();
            }
        });
    }

    public void clickLogout(){
        mainActivity.runOnUiThread(() ->
            webView.evaluateJavascript("javascript:clickLogout()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    _log.e("test logout result");
                }
            })
        );
    }

    // web에서 Android.showToast() 호출시 앱에서 토스트 뛰움
    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(mainActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

    }

}

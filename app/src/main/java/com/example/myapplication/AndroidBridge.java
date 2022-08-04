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

    @JavascriptInterface
    public void call_log( final String _message){
        Log.d(TAG, _message);
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:alert('["+ _message +"]   ')");
            }
        });
    }

    @JavascriptInterface
    public void call_js_func(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:showAndroidToast('test')");
            }
        });
    }

    @JavascriptInterface
    public void call_logout_func(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:logout()");
            }
        });
    }

    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(mainActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

    }

    @JavascriptInterface
    public void onReady(String userId) {
        mainActivity.setUserId(userId);
        mainActivity.removeSplashScreen();

    }

}

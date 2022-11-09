package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.myapplication.fragment.TgtrFragment;

import androidx.fragment.app.Fragment;

public class AndroidBridge {

    private String TAG = "AndroidBridge";
    final public Handler handler = new Handler();

    private WebView webView;
    private Fragment fragment;

    public AndroidBridge(Fragment fragment, WebView webView) {
        this.webView = webView;
        this.fragment = fragment;
    }

    public void setUrl(String url){
        webView.loadUrl(url);
    }

    // web 로그인 페이지 로딩 완료
    @JavascriptInterface
    public void readyWebview( ){
        _log.e("test 메인화면 페이지");
        Toast.makeText(fragment.getContext(), "메인 페이지", Toast.LENGTH_SHORT).show();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                _log.e("test removesplace");
                ((TgtrFragment) fragment).removeSplashScreen();
            }
        });
    }

    // web 로그인 완료 후 userID 전달
    @JavascriptInterface
    public void readyWebview(String userCI ){
        Toast.makeText(fragment.getContext(), userCI, Toast.LENGTH_SHORT).show();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ((TgtrFragment) fragment).setUserCI(userCI);
            }
        });
    }

    // 토큰 값 요청 호출
    @JavascriptInterface
    public String getToken(){
        Toast.makeText(fragment.getContext(), "gettoken", Toast.LENGTH_SHORT).show();

        _log.e("test app token::" + ((TgtrFragment) fragment).getMainActivity().getToken());
        return ((TgtrFragment) fragment).getMainActivity().getToken();
    }

    public void clickLogout(){
        webView.evaluateJavascript("javascript:clickLogout()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                _log.e("test logout result");
                ((TgtrFragment) fragment).setUserCI("");
            }
        });
    }

    public void clearStorage() {
        webView.evaluateJavascript("javascript:clearStorage()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                _log.e("test clearstorage result");
            }
        });
    }

    // web에서 Android.showToast() 호출시 앱에서 토스트 뛰움
    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(fragment.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

}

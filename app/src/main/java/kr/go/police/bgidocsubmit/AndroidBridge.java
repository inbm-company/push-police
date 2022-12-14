package kr.go.police.bgidocsubmit;

import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import kr.go.police.bgidocsubmit.fragment.TgtrFragment;

import androidx.fragment.app.Fragment;

public class AndroidBridge {

    private String TAG = "AndroidBridge";

    private WebView webView;
    private Fragment fragment;

    public AndroidBridge(Fragment fragment, WebView webView) {
        this.webView = webView;
        this.fragment = fragment;
    }

    public void setUrl(String url){
        webView.loadUrl(url);
    }

    // 키패드 핼퍼 객체 생성 및 설정
//    @JavascriptInterface
//    public void setKeyCrypt(final String inputLabel, final int inputType, final int maxLens, final String title, final String hint, final String serverKey, final String uid) {
//        ((TgtrFragment) fragment).setKeyCrypt(inputLabel, inputType, maxLens, title, hint, serverKey, uid);
//    }

    // web 로그인 완료 후 userID 전달
    @JavascriptInterface
    public void readyWebview(String userCI ){
        //Toast.makeText(fragment.getContext(), userCI, Toast.LENGTH_SHORT).show();
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
       // Toast.makeText(fragment.getContext(), "gettoken", Toast.LENGTH_SHORT).show();

        _log.e("test app token::" + ((TgtrFragment) fragment).getMainActivity().getToken());
        return ((TgtrFragment) fragment).getMainActivity().getToken();
    }

    // 읽지 않은 알림 갯수
    @JavascriptInterface
    public String getNewMsgCnt(int cnt){
      //  Toast.makeText(fragment.getContext(), "getNewMsgCnt", Toast.LENGTH_SHORT).show();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ((TgtrFragment) fragment).getMainActivity().setPushCnt(cnt);
            }
        });

        _log.e("test app token::" + ((TgtrFragment) fragment).getMainActivity().getToken());
        return ((TgtrFragment) fragment).getMainActivity().getToken();
    }

    /**
     * 로그 아웃 호출
     */
    public void clickLogout(){
        webView.evaluateJavascript("javascript:clickLogout()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                _log.e("test logout result");
               // ((TgtrFragment) fragment).setUserCI("");
            }
        });
    }

    /**
     * localstorage clear 삭제
     */
    public void clearStorage() {
        _log.e("test fragment clearStorage");
        webView.evaluateJavascript("javascript:localStorage.clear()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                _log.e("test clearstorage result"+ value);
            }
        });
    }

    // web에서 Android.showToast() 호출시 앱에서 토스트 뛰움
    @JavascriptInterface
    public void showToast(String msg) {
        //Toast.makeText(fragment.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

}

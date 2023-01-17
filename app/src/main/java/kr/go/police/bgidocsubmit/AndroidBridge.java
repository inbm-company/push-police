package kr.go.police.bgidocsubmit;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

public class AndroidBridge {

    private WebView webView;
    private MainActivity mainActivity;
    private Context context;

    public AndroidBridge(WebView _webView , MainActivity _activity) {
        this.webView = _webView;
        this.mainActivity = _activity;
        context = _activity.getApplicationContext();
    }

    public void setUrl(String url){
        webView.loadUrl(url);
    }

    // 키패드 핼퍼 객체 생성 및 설정
    @JavascriptInterface
    public void setKeyCrypt(final String inputLabel, final int inputType, final int maxLens, final String title, final String hint, final String serverKey, final String uid) {
        if (TextUtils.isEmpty(serverKey)){
            return;
        }

        Toast.makeText(context, "키패드 호출", Toast.LENGTH_SHORT).show();
        mainActivity.setKeyCrypt(inputLabel, inputType, maxLens, title, hint, serverKey, uid);
    }

    // 키패드 핼퍼 객체 생성 및 설정
    @JavascriptInterface
    public void consoleLog(final String type, final String msg) {
        _log.e("tgtr web consolelog::"+ type+"/"+ msg);
    }

    // web 로그인 완료 후 userID 전달
    @JavascriptInterface
    public void readyWebview(String userCI ){
        Toast.makeText(context, userCI, Toast.LENGTH_SHORT).show();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mainActivity.setUserCI(userCI);
            }
        });
    }

    // 토큰 값 요청 호출
    @JavascriptInterface
    public String getToken(){
        Toast.makeText(context, "gettoken", Toast.LENGTH_SHORT).show();
        _log.e("test app token::" + mainActivity.getToken());
        return mainActivity.getToken();
    }

    // 읽지 않은 알림 갯수
    @JavascriptInterface
    public void getNewMsgCnt(int cnt){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mainActivity.setPushCnt(cnt);
            }
        });

        Toast.makeText(context, "getNewMsgCnt", Toast.LENGTH_SHORT).show();
    }

    /**
     * 로그 아웃 호출
     */
    public void clickLogout(){
        webView.evaluateJavascript("javascript:clickLogout()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                _log.e("test logout result");
            }
        });
    }

    /**
     * 키보드 보안
     */
    public void setKeycryptResult(String label, String uid, String enc, String key, String dummy) {
        webView.evaluateJavascript("javascript:setKeycryptResult('" + label + "','" + uid + "','" + enc + "','" + key + "','" +dummy + "')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                _log.e("test web setKeycryptResult");
            }
        });
    }

    /**
     * localstorage clear 삭제
     */
    public void clearStorage() {
        _log.e("test  clearStorage");
        webView.evaluateJavascript("javascript:localStorage.clear()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                _log.e("test web sclearStorage");
            }
        });
    }

    // web에서 Android.showToast() 호출시 앱에서 토스트 뛰움
    @JavascriptInterface
    public void showToast(String msg) {
        //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}

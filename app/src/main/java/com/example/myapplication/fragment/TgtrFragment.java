package com.example.myapplication.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.example.myapplication.AndroidBridge;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication._log;
import com.example.myapplication.common.Constants;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class TgtrFragment extends Fragment {

    private static final String TAG = "TgtrFragment";

    private WebView webView;

    private View view;

    private AndroidBridge androidBridge;

    private LinearLayout networkErrorLl;

    public static TgtrFragment newInstance() {
        TgtrFragment  fragment = new TgtrFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tgtr, container, false);
        webView = view.findViewById(R.id.mainWebView);
        networkErrorLl = view.findViewById(R.id.network_error_ll);

        if (savedInstanceState == null) {
            setWebView();
        } else {
            webView.restoreState(savedInstanceState);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setTextZoom(100);               // 웹뷰 폰트 크기 고정
        webSettings.setDomStorageEnabled(true);     // local storage 허용
        webSettings.setUseWideViewPort(true);       // wide viewport를 사용하도록 설정
        webSettings.setLoadWithOverviewMode(true);  // 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
        webSettings.setBuiltInZoomControls(true);   // 줌 아이콘 사용
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);

        String userAgent = webView.getSettings().getUserAgentString();
        webSettings.setUserAgentString(userAgent+" android_app");
        webSettings.setDefaultTextEncodingName("UTF-8");

        androidBridge = new AndroidBridge(this, webView);
        webView.addJavascriptInterface(androidBridge, "android");

        webView.loadUrl(Constants.mainUrl);

        webView.setWebContentsDebuggingEnabled(true);   // for webview debugging in chrome
        webView.setBackgroundColor(0x00000000);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                _log.simple("MyApplication"+cm.message() + " -- From line "+ cm.lineNumber() + " of "+ cm.sourceId() );
                return true;
            }
        });

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

        setOnBackPressed(); // 뒤로가기

    }

    //network check
    public void networkCheck() {

//        if (!netWorkConnected) {
//            llDisconnectedstate.setVisibility(View.VISIBLE);
//        } else {
//            llDisconnectedstate.setVisibility(View.GONE);
//            webView.reload();
//        }
    }

    /**
     * splash 안보이게 처리
     */
    public void removeSplashScreen(){
        ((MainActivity)getActivity()).removeSplashScreen();
    }

    /**
     * 로그인 직후 호출 및 사용자 정보 수정시에 사용 함.
     * @param userID
     */
    public void setUserId(String userID){
        if(!userID.isEmpty()){
            // 로그인 직후 호출시
            ((MainActivity) getActivity()).uploadToken();
        }

        ((MainActivity) getActivity()).changeLoginAndLogoutMenu();
        ((MainActivity) getActivity()).setUserId(userID);

    }

    /**
     * webview url 변경
     * @param newUrl
     */
    public void changeUrl(String newUrl){
        androidBridge.setUrl(newUrl);
    }

    /**
     * 로그아웃 호출 (app > web 함수)
     */
    public void clickLogout(){
        androidBridge.clickLogout();
    }

    /**
     * 프래그먼트 갱신시 호출
     */
    public void fragmentRefresh(){
        _log.e("tgtrfragment fragmentRefresh");
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.detach(this).attach(this).commitAllowingStateLoss();
    }

    /**
     * 백 버튼 시 호출
     */
    public void setOnBackPressed() {
        webView.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return true;
            _log.e("test back"+view.getId()+ keyEvent.getAction()+"/"+i);
            if (i == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else
                   ((MainActivity)getActivity()).onBackPressed();
                return true;
            }
            return false;
        });
    }
}

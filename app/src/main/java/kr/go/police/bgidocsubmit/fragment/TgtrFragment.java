package kr.go.police.bgidocsubmit.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import kr.go.police.bgidocsubmit.AndroidBridge;
import kr.go.police.bgidocsubmit.MainActivity;
import com.example.myapplication.R;
import kr.go.police.bgidocsubmit._log;
import kr.go.police.bgidocsubmit.common.Constants;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class TgtrFragment extends Fragment {
//public class TgtrFragment extends Fragment implements View.OnTouchListener {

    private static final String TAG = "TgtrFragment";

    private WebView webView;
    private View view;
    private ProgressBar progressBar;
    private ValueCallback mFilePathCallback;

    private boolean isFirstLoad = true;

    private AndroidBridge androidBridge;

    // 보안 키패드 관련 변수
//    private IxKeypadManageHelper keypadMngHelper;
//    private IxConfigureInputItemW inputConfig;
//    private EditText hiddenInputBox;
//    private final int REQUEST_CODE_KEYPAD = 0x2231;

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

        // webview 로딩시 프로그래스바
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        if (savedInstanceState == null) {
            _log.e("test fragment savedInstanceState null:"+isFirstLoad);
            setWebView();
        } else {
            _log.e("test fragment savedInstanceState refresh:"+isFirstLoad);
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

        // 키패드 핼퍼 객체 생성 및 설정
        //keypadMngHelper = new IxKeypadManageHelper(App.getStaticContext(), REQUEST_CODE_KEYPAD);
       // keypadMngHelper.setUiVisibility(Defines.FLAG_INHERIT_UI_VISIBILITY | Defines.FLAG_UI_NO_NUMBER_PAD_HIDEBUTTON | Defines.FLAG_USE_INPUT_ACTIVITY);

        // 입력창 객체 생성 및 설정
        //hiddenInputBox = new EditText(getStaticContext());
       // hiddenInputBox.setFocusable(false);

        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setTextZoom(100);               // 웹뷰 폰트 크기 고정
        settings.setDomStorageEnabled(true);     // local storage 허용
        settings.setUseWideViewPort(true);       // wide viewport를 사용하도록 설정
        settings.setLoadWithOverviewMode(true);  // 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
        settings.setDisplayZoomControls(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);       //
       // settings.setAllowContentAccess(true);    //
       // settings.setAllowFileAccessFromFileURLs(true);
        //settings.setAllowUniversalAccessFromFileURLs(true);
        //webSettings.setAppCacheEnabled(true);       // 캐시 삭제

        String userAgent = webView.getSettings().getUserAgentString();
        settings.setUserAgentString(userAgent+" android_app");

        androidBridge = new AndroidBridge(this, webView);
        webView.addJavascriptInterface(androidBridge, "android");
        //webView.setOnTouchListener(this);

        webView.setWebChromeClient(new TgtrWebChromeClient());
        webView.setWebViewClient(new TgtrWebViewClientClass(this.getContext()));
        webView.setWebContentsDebuggingEnabled(true);   // for webview debugging in chrome
        webView.setBackgroundColor(0x00000000);

        // targetSdkVersion이 21 이상일 경우 웹뷰 추가 설정 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.loadUrl(Constants.mainUrl);
        webView.clearHistory();

        setOnBackPressed(); // 뒤로가기
    }

    public class TgtrWebChromeClient extends WebChromeClient {

        // web 콘솔 로그 출력
        public boolean onConsoleMessage(ConsoleMessage cm) {
            _log.simple("tgtr web:: "+cm.message() + " -- From line "+ cm.lineNumber() + " of "+ cm.sourceId() );
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            //Toast.makeText(view.getContext(), "window.open 협의가 필요합니다.", Toast.LENGTH_LONG).show();
            WebView newWebView = new WebView(view.getContext());
            WebSettings settings = newWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setDomStorageEnabled(true);
            settings.setSupportMultipleWindows(true);
            settings.setLoadWithOverviewMode(true);  // 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
            settings.setDisplayZoomControls(true);
            settings.setSupportZoom(true);

            final Dialog dialog = new Dialog(view.getContext(), R.style.Theme_Transparent);
            dialog.setContentView(newWebView);
            dialog.show();

            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                    if(keyCode == KeyEvent.KEYCODE_BACK) {
                        if(newWebView.canGoBack()){
                            newWebView.goBack();
                        }else{
                            //Toast.makeText(view.getContext(), "Window.open 종료", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            webView.reload();

                        }
                        return true;
                    }else{
                        return false;
                    }
                }
            });

            newWebView.setWebViewClient(new WebViewClient());
            newWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
                    dialog.dismiss();
                }
            });

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            _log.e(getClass().getName()+ "onCloseWindow");
            window.setVisibility(View.GONE);
            window.destroy();
            super.onCloseWindow(window);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            _log.e(getClass().getName()+ "onJsAlert() url:"+url+", message:"+message);
            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            _log.e(getClass().getName()+ "onJsConfirm() url:"+url+", message"+message);

            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();

                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .create()
                    .show();
            return true;
        }


        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            _log.e("test onShowFileChooser::"+ webView+"/"+fileChooserParams);
            try{
                if(mFilePathCallback != null){
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }

                mFilePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  // 파일지정 (image, pdf...)
                intent.addCategory(Intent.CATEGORY_OPENABLE); // 카테고리 지정
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 파일 다중 선택 지정
                intent.setType("*/*");
                //intent.setType("application/pdf|image/*");

                startActivityResult.launch(intent);
            } catch(Exception e){
                _log.e("test error:"+e.toString());
            }

            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

            if(newProgress == 100){
                progressBar.setVisibility(View.INVISIBLE);
                _log.e("test web 로딩 중~~~"+ newProgress);
            }
        }
    }

    /**
     * 파일 선택 완료 후 처리
     */
    public ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            _log.e("test onActivityResult:: "+result.getResultCode()+"/"+result.getData());
            //fileChooser 로 파일 선택 후 onActivityResult 에서 결과를 받아 처리함
            int resultCode = result.getResultCode();
            Intent data = result.getData();

            if(resultCode == Activity.RESULT_OK) {
                //파일 선택 완료 했을 경우
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                }else{
                    mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
                }
                mFilePathCallback = null;
            } else {
                //cancel 했을 경우
                if(mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
            }
        }
    });

    public class TgtrWebViewClientClass extends WebViewClient {
        private Context mApplicationContext =null;

        public TgtrWebViewClientClass(Context _applicationContext) {
            mApplicationContext = _applicationContext;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);

            if(isFirstLoad){
                clearStorage();
            }
            isFirstLoad = false;
            _log.e("test onPageStarted(url:"+url+ ", isFirstLoad:"+isFirstLoad+")");
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                onReceivedError(error.getErrorCode(),String.valueOf(error.getDescription()));
            }
        }

        private void onReceivedError(int errorCode, String description){
            _log.e("onReceivedError() " + errorCode + " ---> " + description);
            switch (errorCode) {
                case WebViewClient.ERROR_TIMEOUT:   //연결 시간 초과
                case WebViewClient.ERROR_CONNECT:   //서버로 연결 실패
                    //case WebViewClient.ERROR_UNKNOWN:   // 일반 오류
                case WebViewClient.ERROR_FILE_NOT_FOUND: //404
                case WebViewClient.ERROR_HOST_LOOKUP :
                case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME:
                case WebViewClient.ERROR_AUTHENTICATION:
                case WebViewClient.ERROR_PROXY_AUTHENTICATION:
                case WebViewClient.ERROR_IO:
                case WebViewClient.ERROR_REDIRECT_LOOP:
                case WebViewClient.ERROR_UNSUPPORTED_SCHEME:
                case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                case WebViewClient.ERROR_BAD_URL:
                case WebViewClient.ERROR_FILE:
                case WebViewClient.ERROR_TOO_MANY_REQUESTS:
                case WebViewClient.ERROR_UNSAFE_RESOURCE:
                    //Toast.makeText(mApplicationContext,"WebViewClient,onReceivedError("+errorCode+") 에러 발생 " , Toast.LENGTH_LONG).show();
                    _log.e("WebViewClient,onReceivedError("+errorCode+") 에러 발생 "  );
                    ((MainActivity) getActivity()).networkCheck();
                    break;
            }
        }
    }

    public void webViewReload(){
        if(webView != null){
            webView.reload();
        }
    }

    public MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }

    /**
     * 로그인 직후 호출 및 사용자 정보 수정시에 사용 함.
     * @param userCI
     */
    public void setUserCI(String userCI){
        if(!userCI.isEmpty()){
            // 로그인 직후 token 전달 api 실행
            ((MainActivity) getActivity()).appToken();
        }else{
            // 로그아웃 상태
            ((MainActivity) getActivity()).setPushCnt(0);
        }

        ((MainActivity) getActivity()).setUserCI(userCI);
        ((MainActivity) getActivity()).changeLoginAndLogoutMenu();
        ((MainActivity) getActivity()).setVisibilityToolbarIcon();
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
     * 앱 최초 로딩시 localstorage 삭제 요청 (로그인 정보 삭제 위해)
     */
    public void clearStorage(){
        _log.e("test clearStorage ~~~");
        androidBridge.clearStorage();
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
     * 보안 키패드 호출
     */
//    public void setKeyCrypt(final String inputLabel, final int inputType, final int maxLens, final String title, final String hint, final String serverKey, final String uid) {
//        if ( TextUtils.isEmpty(serverKey) ) return;
//
//        // 입력창 config 설정
//        if ( 0 == inputType ) {
//            inputConfig = new IxConfigureInputItemW(hiddenInputBox, Defines.KEYPAD_TYPE_NUMBER, Defines.SHUFFLE_TYPE_GAPKEY);
//        } else {
//            inputConfig = new IxConfigureInputItemW(hiddenInputBox, Defines.KEYPAD_TYPE_QWERTY, Defines.SHUFFLE_TYPE_GAPKEY);
//        }
//
//        if ( 0 < maxLens )
//            inputConfig.setMaxLength(maxLens);
//
//        inputConfig.setMinLength(1);
//
//        inputConfig.setLabel(inputLabel);
//        inputConfig.setUid(uid);
//        inputConfig.setTitle(title);
//        inputConfig.setHint(hint);
//
//        // 키패드 핼퍼에 서버키 설정
//        try {
//            keypadMngHelper.setServerKey(serverKey);
//        } catch (InvaildServerKeyException e) {
//            return;
//        } catch (CryptoOperationException e) {
//            return;
//        }
//
//        // 보안 키패드 시작
//        keypadMngHelper.configureInputBoxAndStart(hiddenInputBox, inputConfig);
//    }

//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if (v == webView && v.hasFocus() == false)
//            v.requestFocus();
//
//        return false;
//    }

    /**
     * 백 버튼 시 호출
     */
    public void setOnBackPressed() {
        webView.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return true;
            _log.e("test back:: "+webView.canGoBack()+"/"+webView.getUrl()+"/"+ webView.getUrl().indexOf("user-index.do"));
            if (i == KeyEvent.KEYCODE_BACK) {

                boolean isDrawerOpen = ((MainActivity) getActivity()).getIsDrawerOpen();
                _log.e("test bac::"+isDrawerOpen);

                // drawer open시
                if(isDrawerOpen){
                    ((MainActivity) getActivity()).callCloseDrawer();
                    return true;
                }

                if (webView.canGoBack()) {
                    if(webView.getUrl().contains("user-index.do") || webView.getUrl().contains("user-app-main.do") ){
                        webView.clearCache(true);
                        ((MainActivity) getActivity()).onBackPressed();
                    }else{
                        webView.goBack();
                    }
                } else {
                    webView.clearCache(true);
                    ((MainActivity) getActivity()).onBackPressed();
                    return true;
                }
            }
            return false;
        });
    }
}

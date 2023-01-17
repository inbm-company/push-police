package kr.go.police.bgidocsubmit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import kr.go.police.bgidocsubmit.common.Constants;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nprotect.keycryptm.Defines;
import com.nprotect.keycryptm.IxConfigureInputItemW;
import com.nprotect.keycryptm.IxKeypadManageHelper;
import com.nprotect.keycryptm.IxResultItem;
import com.nprotect.truemessage.CryptoOperationException;
import com.nprotect.truemessage.InvaildServerKeyException;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private DrawerLayout drawerLayout;

    private ConstraintLayout pushHistoryCl;
    private ConstraintLayout purchaseCl;
    private ConstraintLayout noticeBoardItemCl;

    private TextView loginLogoutTv;
    private TextView pushBadge;
    private TextView barPushBadge;

    private ConstraintLayout networkErrorLl;
    private Button btnRetryConnect;

    private String userId = "";
    private String backStr;

    private Toast toast;

    private long backKeyPressedTime = 0;

    private SharedPreferences pref;

    private ConnectivityManager cm;
    public static boolean networkChangeFlag = false;
    public static boolean netWorkConnected = false;

    private WebView webView;
    private View view;
    private ProgressBar progressBar;
    private ValueCallback mFilePathCallback;

    private boolean isFirstLoad = true;

    private AndroidBridge androidBridge;

    // 보안 키패드 관련 변수
    private IxKeypadManageHelper keypadMngHelper;
    private IxConfigureInputItemW inputConfig;
    private EditText hiddenInputBox;

    //접근성 커스텀 입력 버튼
    private final String[][] lowCaseArray = {{"커스텀 1", "커스텀 2", "커스텀 3", "커스텀 4", "커스텀 5", "커스텀 6", "커스텀 7", "커스텀 8", "커스텀 9", "커스텀 0"},
            {"커스텀 q", "커스텀 w", "커스텀 e", "커스텀 r", "커스텀 t", "커스텀 y", "커스텀 u", "커스텀 i", "커스텀 o", "커스텀 p"},
            {"커스텀 a", "커스텀 s", "커스텀 d", "커스텀 f", "커스텀 g", "커스텀 h", "커스텀 j", "커스텀 k", "커스텀 l"},
            {"sh", "커스텀 z", "커스텀 x", "커스텀 c", "커스텀 v", "커스텀 b", "커스텀 n", "커스텀 m", "bs"},
            {"func", "func", "func", "func", "func", "func"}};

    private final String[][] upCaseArray = {{"1 버튼", "2 버튼", "3 버튼", "4 버튼", "5 버튼", "6 버튼", "7 버튼", "8 버튼", "9 버튼", "0 버튼"},
            {"Q 버튼", "W 버튼", "E 버튼", "R 버튼", "T 버튼", "Y 버튼", "U 버튼", "I 버튼", "O 버튼", "P 버튼"},
            {"A 버튼", "S 버튼", "D 버튼", "F 버튼", "G 버튼", "H 버튼", "J 버튼", "K 버튼", "L 버튼"},
            {"sh", "Z 버튼", "X 버튼", "C 버튼", "V 버튼", "B 버튼", "N 버튼", "M 버튼", "bs"},
            {"func", "func", "func", "func", "func", "func"}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // 네트워크 체크
        NetworkRequest.Builder networkBuilder = new NetworkRequest.Builder();
        networkBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        cm.registerNetworkCallback(networkBuilder.build(), networkCallback);

        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("police", MODE_PRIVATE);
        _log.e("test main create savedInstanceState::"+ savedInstanceState +"/"+ getUserCI());

        uploadToken();
        setLayout();

        if (savedInstanceState == null) {
            setWebView();
        } else {
            if(webView != null) webView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        _log.e("test main onNewIntent::"+ intent);
        setIntent(intent);
        gotoMainAndNotic(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 키패드 완료 버튼 클릭시 (-1: 완료, 0: 취소)
        if (requestCode == Constants.REQUEST_CODE_KEYPAD && resultCode == -1) {
            IxResultItem[] resultItem =  keypadMngHelper.getInputResults();
            _log.e("test onActivityResult keypad~~"+ inputConfig.getLabel()+"/"+ resultItem);
            setKeycryptResult(resultItem);
        }
    }

    private void setWebView() {
        // 키패드 핼퍼 객체 생성 및 설정
        keypadMngHelper = new IxKeypadManageHelper(this, Constants.REQUEST_CODE_KEYPAD);
        keypadMngHelper.setUiVisibility(Defines.FLAG_UI_NO_NUMBER_PAD_HIDEBUTTON | Defines.FLAG_USE_INPUT_ACTIVITY);
//        keypadMngHelper.setUiVisibility(Defines.FLAG_INHERIT_UI_VISIBILITY | Defines.FLAG_UI_NO_NUMBER_PAD_HIDEBUTTON | Defines.FLAG_USE_INPUT_ACTIVITY);

        // 키패드 커스텀 접근성
        keypadMngHelper.setCustomAccessibility(lowCaseArray, upCaseArray);

        // 입력창 객체 생성 및 설정
        hiddenInputBox = new EditText(this);
        hiddenInputBox.setFocusable(false);

        webView = findViewById(R.id.mainWebView);

        // webview 로딩시 프로그래스바
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

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
        // 키보드 보안설정
        //settings.setAllowFileAccess(true);       // 파일 액세스 허용 여부
        //settings.setAllowContentAccess(true);    // 웹뷰를 통해 Content URL 에 접근할지 여부
        //settings.setAllowFileAccessFromFileURLs(true);
        //settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAppCacheEnabled(true);       // 캐시 삭제

        String userAgent = webView.getSettings().getUserAgentString();
        settings.setUserAgentString(userAgent+" android_app");

        androidBridge = new AndroidBridge(webView, MainActivity.this);
        webView.addJavascriptInterface(androidBridge, "android");

        webView.setWebChromeClient(new TgtrWebChromeClient());
        webView.setWebViewClient(new TgtrWebViewClientClass(this));
        webView.setWebContentsDebuggingEnabled(true);   // for webview debugging in chrome
        webView.setBackgroundColor(0x00000000);

        // targetSdkVersion이 21 이상일 경우 웹뷰 추가 설정 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        webView.clearCache(true);
        webView.clearHistory();
        webView.loadUrl(Constants.mainUrl);

        setOnBackPressed(); // 뒤로가기
    }

    private void gotoMainAndNotic(Intent intent){
        Boolean doClickEvent = intent.getBooleanExtra("notiClick", false);
        _log.e("test gotoMainAndNotic"+   doClickEvent );

        // push 클릭하여 앱을 실행할경우 이벤트 시행
        if (doClickEvent) {
            notiClickEvent();
        }
    }

    private void setLayout() {
        drawerLayout = findViewById(R.id.mainDrawerView);
        pushHistoryCl = findViewById(R.id.push_history_cl);
        purchaseCl = findViewById(R.id.purchase_cl);
        noticeBoardItemCl = findViewById(R.id.notice_board_item_cl);
        loginLogoutTv = findViewById(R.id.login_logout_txt);

        networkErrorLl = findViewById(R.id.network_error_ll);
        btnRetryConnect = networkErrorLl.findViewById(R.id.btn_retry);

        pushBadge = findViewById(R.id.push_badge);
        barPushBadge = findViewById(R.id.bar_push_badge);

        //네트워크 재시도 버튼
        btnRetryConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkCheck();
            }
        });

        backStr = this.getResources().getString(R.string.back_presssed);

        changeLoginAndLogoutMenu();
        setVisibilityToolbarIcon();
    }

    //network check
    public void networkCheck() {
        if (!isNetWorkConnected()) {
            networkErrorLl.setVisibility(View.VISIBLE);
            Toast.makeText(App.getStaticContext(), R.string.retry_msg, Toast.LENGTH_LONG).show();
        } else {
            networkErrorLl.setVisibility(View.GONE);
            webViewReload();
        }
    }

    private void webViewReload(){
        if(webView != null){
            webView.reload();
        }
    }

    /**
     * DrawerLayout open
     */
    private void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.END);
    }

    /**
     * drawerLayout close
     */
    private void closeDrawer(DrawerLayout drawerLayout) {
        drawerLayout.closeDrawer(GravityCompat.END);
    }

    /**
     * drawer layout close
     */
    public void callCloseDrawer(){
        if(drawerLayout != null){
            noticeBoardItemCl.setVisibility(View.GONE);
            closeDrawer(drawerLayout);
        }
    }

    /**
     * drawer layout 오픈 상태 리턴
     */
    private boolean getIsDrawerOpen(){
        return drawerLayout.isDrawerOpen(GravityCompat.END);
    }

    /**
     * 햄버거 메뉴 버튼
     */
    public void clickMenu(View view) {
        openDrawer(drawerLayout);
    }

    /**
     * toolbar icon visibility
     */
    private void setVisibilityToolbarIcon(){
        ImageButton btnPushHistory = findViewById(R.id.btn_push_history);
        ImageButton btnPurchase = findViewById(R.id.btn_purchase);
        ImageButton btnChatbot = findViewById(R.id.btn_chatbot);

        runOnUiThread(() -> {
            btnPushHistory.setVisibility(View.GONE);
            btnPurchase.setVisibility(View.GONE);
            btnChatbot.setVisibility(View.VISIBLE);

            if (!getUserCI().isEmpty()) {
                btnPushHistory.setVisibility(View.VISIBLE);
                btnPurchase.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 앱 최초 로딩시 localstorage 삭제 요청 (로그인 정보 삭제 위해)
     */
    private void clearStorage(){
        _log.e("test clearStorage ~~~");
        androidBridge.clearStorage();
    }

    /**
     * 로그인 user 정보
     */
    public void setUserCI(String userCI) {
        _log.simple("set id" + userCI);
        if(!userCI.isEmpty()){
            //로그인 직후 token 전달 api 실행
            appToken();
        }else{
            // 로그아웃상태
            setPushCnt(0);
        }
        this.userId = userCI;
        changeLoginAndLogoutMenu();
        setVisibilityToolbarIcon();
    }

    public String getUserCI() {
       return this.userId;
    }

    /**
     * 토큰 저장
     * @param token
     */
    public void setToken(String token) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        _log.e("test getToken::"+ pref.getString("token", ""));
        return pref.getString("token", "");
    }

    /**
     * webview url 변경
     * @param newUrl
     */
    private void changeUrl(String newUrl){
        _log.e("test changeUrl"+ newUrl);
        try{
            androidBridge.setUrl(newUrl);
        }catch(NullPointerException e){
            _log.e("test changeUrl null exception");
        }
    }

    /**
     * 로그인/로그아웃 메뉴 상태 수정
     */
    public void changeLoginAndLogoutMenu() {
        runOnUiThread(() -> {
            pushHistoryCl.setVisibility(View.GONE);
            purchaseCl.setVisibility(View.GONE);

            if (getUserCI().isEmpty()) {
                loginLogoutTv.setText("로그인");
            } else {
                loginLogoutTv.setText("로그아웃");
                pushHistoryCl.setVisibility(View.VISIBLE);
                purchaseCl.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * push 알림 항목 클릭
     */
    public void notiClickEvent() {
        if (getUserCI().isEmpty()) {
            changeUrl(Constants.mainUrl);
        } else {
            changeUrl(Constants.pushHistoryUrl);
        }
    }

    /**
     * 로고 클릭
     */
    public void gotoMain(View view) {
        changeUrl(Constants.mainUrl);
    }

    /**
     * 닫기
     * @param view
     */
    public void clickCloseDrawer(View view) {
        callCloseDrawer();
    }

    /**
     * 알림목록 메뉴 클릭
     * @param view
     */
    public void clickPushHistory(View view) {
        changeUrl(Constants.pushHistoryUrl);
        callCloseDrawer();
    }

    /**
     * 제출한 증명서 메뉴 클릭
     * @param view
     */
    public void clickPurchase(View view) {
        changeUrl(Constants.purchaseUrl);
        callCloseDrawer();
    }

    /**
     * 게시판 폴더 클릭
     * @param view
     */
    public void clickNoticeBoard(View view) {
        int visibility = noticeBoardItemCl.getVisibility();
        _log.e("test 게시판 폴더 클릭::"+ view +"/"+ visibility);
        if (visibility == 0) {
            noticeBoardItemCl.setVisibility(View.GONE);
        } else {
            noticeBoardItemCl.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 공지사항 메뉴 클릭
     * @param view
     */
    public void clickNotice(View view) {
        changeUrl(Constants.noticeUrl);
        callCloseDrawer();
    }

    /**
     * 자주 묻는 질문 메뉴 클릭
     * @param view
     */
    public void clickFaq(View view) {
        changeUrl(Constants.faqUrl);
        callCloseDrawer();
    }

    /**
     * Q&A 메뉴 클릭
     * @param view
     */
    public void clickQa(View view) {
        changeUrl(Constants.qaUrl);
        callCloseDrawer();
    }

    /**
     * 챗봇 메뉴 클릭
     * @param view
     */
    public void clickChatbot(View view) {
        changeUrl(Constants.chatbotUrl);
        callCloseDrawer();
    }

    /**
     * 버전정보 메뉴 클릭
     * @param view
     */
    public void clickVersion(View view) {
        callCloseDrawer();

        Intent intent = new Intent( this, VersionInfoActivity.class);
        startActivity(intent);
    }

    /**
     * 로그인/로그아웃 메뉴 클릭
     * @param view
     */
    public void clickLoginAndLogout(View view) {
        _log.e("test login and logout" + getUserCI());
        if (getUserCI().isEmpty()) {
            changeUrl(Constants.loginUrl);
        } else {
            androidBridge.clickLogout();
        }

        callCloseDrawer();
    }

    /**
     * token api
     */
    public void appToken() {
        new Thread(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("appToken", getToken());
                // 대상자로 변경해야 함.
                data.put("memberCi", getUserCI());
                _log.e("apptoken result:: "+ getUserCI());
                String result = _web.post(Constants.appToken, data.toString());
               // handler.sendEmptyMessage(100);
            } catch (Exception e) {
                _log.e("Exception");
            }
        }).start();
    }

    /**
     * 토근 값 전송
     */
    public void uploadToken() {
        // 현재 기기의 토큰값을 가져옴
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if (!task.isSuccessful()) {
                    _log.e("fcm fail get token");
                    return;
                }

                String token = task.getResult();
                setToken(token);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("failed get token", e.getLocalizedMessage());
            }
        });
    }

    public void setPushCnt(int cnt){
        _log.e("test setPushCnt"+ getIsDrawerOpen()+"/"+cnt);

        runOnUiThread(() -> {
            if(cnt == 0){
                pushBadge.setVisibility(View.GONE);
                barPushBadge.setVisibility(View.GONE);
            }else{
                pushBadge.setVisibility(View.VISIBLE);
                barPushBadge.setVisibility(View.VISIBLE);

                String cntStr = String.valueOf(cnt);
                if(drawerLayout != null){
                    pushBadge.setText(cntStr);
                    barPushBadge.setText(cntStr);
                }
            }
        });
    }

    /**
     * 보안 키패드 호출
     */
    public void setKeyCrypt(final String inputLabel, final int inputType, final int maxLens, final String title, final String hint, final String serverKey, final String uid) {
        // 입력창 config 설정
        if ( 0 == inputType ) {
            inputConfig = new IxConfigureInputItemW(hiddenInputBox, Defines.KEYPAD_TYPE_NUMBER, Defines.SHUFFLE_TYPE_GAPKEY);
        } else {
            inputConfig = new IxConfigureInputItemW(hiddenInputBox, Defines.KEYPAD_TYPE_QWERTY, Defines.SHUFFLE_TYPE_GAPKEY);
        }

        if ( 0 < maxLens ) inputConfig.setMaxLength(maxLens);

        inputConfig.setMinLength(1);
        inputConfig.setLabel(inputLabel);
        inputConfig.setUid(uid);
        inputConfig.setTitle(title);
        inputConfig.setHint(hint);

        inputConfig.enableGapResources(false , false);

        // 키패드 핼퍼에 서버키 설정
        try {
            keypadMngHelper.setServerKey(serverKey.trim());
        } catch (InvaildServerKeyException e) {
            _log.e("test setKeyCrypt InvaildServerKeyException error ");
            return;
        } catch (CryptoOperationException e) {
            _log.e("test setKeyCrypt CryptoOperationException error ");
            return;
        }

        // 보안 키패드 시작
        keypadMngHelper.configureInputBoxAndStart(hiddenInputBox, inputConfig);
    }

    /**
     * 키보드 보안 입력 창 값 web 전달
     */
    public void setKeycryptResult(IxResultItem[] resultItem) {

        if ( null != resultItem && null != resultItem[0] ) {
            IxResultItem inputResult = resultItem[0];

            // 입력창에 넣을 더미 문자 생성
            StringBuilder dummy = new StringBuilder();
            for (int i = 0; i < inputResult.getEncDataLen(); i++) {
                dummy.append("*");
            }

            String label = inputConfig.getLabel();
            String uid = inputConfig.getUid();
            String enc = inputResult.getEncData();
            String key = inputResult.getKey();
            String dummyStr = dummy.toString();
            _log.e("test onActivityResult keypad result~~"+ label+"/"+ uid+"/"+enc+"/"+key+"/"+dummyStr);

            androidBridge.setKeycryptResult(label, uid, enc, key, dummyStr);
        }
    }


    /** 네트워크 체크 **/
    private ConnectivityManager.NetworkCallback  networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onLost(Network network) {   //네트워크 끊길 때
            _log.e("abs onLost");
            networkChangeFlag = true;
            netWorkConnected = false;
        }

        @Override
        public void onAvailable(Network network) {   //네트워크 연결될때 호출
            _log.e("abs onAvailable::"+ network);
            networkChangeFlag = true;
            netWorkConnected = true;
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            _log.e("abs onCapabilitiesChanged::"+ networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));

            //네트워크 연결 유무.
            if(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)){
                networkChangeFlag = true;
                netWorkConnected = true;
            }
        }
    };

    public boolean isNetWorkConnected(){
        boolean result = false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if(capabilities != null){
            _log.e("abs isNetWorkConnected " + capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)+"/"+
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                netWorkConnected = true;
                return true;
            }else if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                netWorkConnected = true;
                return true;
            }
        }
        netWorkConnected = false;
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v == webView && v.hasFocus() == false){
            v.requestFocus();
        }
        return false;
    }

    /** 웹뷰 설정 부분 **/
    public class TgtrWebChromeClient extends WebChromeClient {

        // web 콘솔 로그 출력
        public boolean onConsoleMessage(ConsoleMessage cm) {
            _log.simple("tgtr web:: "+cm.message()+ " -- From line "+ cm.lineNumber() + " of "+ cm.sourceId() );
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Toast.makeText(view.getContext(), "window.open 협의가 필요합니다."+ isDialog, Toast.LENGTH_LONG).show();
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

            _log.e("test window open dialog::"+ dialog);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                    if(keyCode == KeyEvent.KEYCODE_BACK) {
                        _log.e("test window open back::"+ keyCode+"/"+newWebView.canGoBack());
                        if(newWebView.canGoBack()){
                            newWebView.goBack();
                        }else{
                            //Toast.makeText(view.getContext(), "Window.open 종료", Toast.LENGTH_LONG).show();
                            newWebView.loadUrl("javascript:self.close();");
                            dialog.dismiss();
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
                    newWebView.loadUrl("javascript:self.close();");
                    if( dialog != null ) dialog.dismiss();
                    window.destroy();
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
            window.loadUrl("javascript:self.close();");
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
            try {
                if (mFilePathCallback != null) {
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
            }catch(IllegalStateException e){
                _log.e("IllegalStateException");
            } finally {
                _log.e("Exception");
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
                    networkCheck();
                    break;
            }
        }
    }

    /**
     * 백 버튼 시 호출
     */
    public void setOnBackPressed() {
        webView.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return true;
            _log.e("test back:: "+webView.canGoBack()+"/"+webView.getUrl()+"/"+ webView.getUrl().indexOf("user-index.do"));
            if (i == KeyEvent.KEYCODE_BACK) {

                boolean isDrawerOpen = getIsDrawerOpen();
                _log.e("test bac::"+isDrawerOpen);

                // drawer open시
                if(isDrawerOpen){
                    callCloseDrawer();
                    return true;
                }

                if (webView.canGoBack()) {
                    if(webView.getUrl().contains("user-index.do") || webView.getUrl().contains("user-app-main.do") ){
                        webView.clearCache(true);
                        onBackPressed();
                    }else{
                        webView.goBack();
                    }
                } else {
                    webView.clearCache(true);
                    onBackPressed();
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        // drawer open시
        if(getIsDrawerOpen()){
            callCloseDrawer();
            return;
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();

            toast = Toast.makeText(this, backStr, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            toast.cancel();
            this.finish();
            finishAffinity();
            System.runFinalization();
        }
    }
}
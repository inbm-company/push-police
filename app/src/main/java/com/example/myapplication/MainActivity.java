package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.common.Constants;
import com.example.myapplication.fragment.TgtrFragment;
import com.example.myapplication.fragment.VersionInfoFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private FrameLayout splashFl;

    private RelativeLayout toolbarIcon;

    private Toolbar toolbar;

    private ConstraintLayout pushHistoryCl;
    private ConstraintLayout purchaseCl;
    private ConstraintLayout noticeBoardItemCl;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TextView loginLogoutTv;
    private TextView pushBadge;
    private TextView barPushBadge;

    private String userId = "";
    private String backStr;

    private Toast toast;

    private long backKeyPressedTime = 0;

    private TgtrFragment tgtrFragment;
    private VersionInfoFragment versionInfoFragment;

    private SharedPreferences pref;

    private ConnectivityManager cm;
    public static boolean networkChangeFlag = false;
    public static boolean netWorkConnected = false;

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

        Intent intent = getIntent();
        Boolean doClickEvent = intent.getBooleanExtra("notiClick", false);

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float dd = metrics.densityDpi;
        _log.e("test device dpi => " + dd);


        // push 클릭하여 앱을 실행할경우 이벤트 시행
        if (doClickEvent) {
            notiClickEvent();
        }

        uploadToken();
        setLayout();
        setupFragment();
    }

    private void setLayout() {
        drawerLayout = findViewById(R.id.mainDrawerView);
        pushHistoryCl = findViewById(R.id.push_history_cl);
        purchaseCl = findViewById(R.id.purchase_cl);
        noticeBoardItemCl = findViewById(R.id.notice_board_item_cl);
        loginLogoutTv = findViewById(R.id.login_logout_txt);

        pushBadge = findViewById(R.id.push_badge);
        barPushBadge = findViewById(R.id.bar_push_badge);

        backStr = this.getResources().getString(R.string.back_presssed);

        changeLoginAndLogoutMenu();
        setVisibilityToolbarIcon();
    }

    /**
     * 앱 실행시 시작하는 프래그먼트
     */
    private void setupFragment() {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        tgtrFragment = TgtrFragment.newInstance();
        transaction.add(R.id.frame_layout, tgtrFragment).commitNow();
    }

    /**
     * DrawerLayout open
     *
     * @param drawerLayout
     */
    private void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.END);
    }

    /**
     * drawerLayout close
     *
     * @param drawerLayout
     */
    private void closeDrawer(DrawerLayout drawerLayout) {
        drawerLayout.closeDrawer(GravityCompat.END);
    }

    /**
     * drawer layout close
     */
    public void callCloseDrawer(){
        _log.e("test close::"+ drawerLayout );
        if(drawerLayout != null){
            _log.e("test close::"+ drawerLayout );
            closeDrawer(drawerLayout);
        }
    }

    /**
     * drawer layout 오픈 상태 리턴
     * @return
     */
    public boolean getIsDrawerOpen(){
        return drawerLayout.isDrawerOpen(GravityCompat.END);
    }

    /**
     * toolbar icon visibility
     *
     */
    public void setVisibilityToolbarIcon(){
        ImageButton btnPushHistory = findViewById(R.id.btn_push_history);
        ImageButton btnPurchase = findViewById(R.id.btn_purchase);
        ImageButton btnChatbot = findViewById(R.id.btn_chatbot);

        runOnUiThread(() -> {
            btnPushHistory.setVisibility(View.GONE);
//            barPushBadge.setVisibility(View.GONE);
            btnPurchase.setVisibility(View.GONE);
            btnChatbot.setVisibility(View.VISIBLE);

            if (!getUserCI().isEmpty()) {
                btnPushHistory.setVisibility(View.VISIBLE);
//                barPushBadge.setVisibility(View.VISIBLE);
                btnPurchase.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * toolbar visibility
     *
     * @param visibility
     */
    public void setVisibilityToolbar(int visibility) {
        _log.e("test setVisibilityToolbar::"+visibility);
        //toolbar.setVisibility(visibility);
    }

    public void changeFragment(String page) {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        if (page.equals("version")) {
            setVisibilityToolbar(View.GONE);
            if (versionInfoFragment == null) {
                versionInfoFragment = VersionInfoFragment.newInstance();
                transaction.add(R.id.frame_layout, versionInfoFragment).commit();
            } else {
                transaction.replace(R.id.frame_layout, versionInfoFragment).commit();
            }
        } else {
            setVisibilityToolbar(View.VISIBLE);

            if (tgtrFragment == null) {
                tgtrFragment = TgtrFragment.newInstance();
                transaction.add(R.id.frame_layout, tgtrFragment).commitNow();
            } else {
                transaction.replace(R.id.frame_layout, tgtrFragment).commit();
            }
        }
    }

    // 햄버거 메뉴 버튼
    public void clickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public void setUserCI(String id) {
        _log.simple("set id" + id);
        this.userId = id;
    }

    public String getUserCI() {
       return this.userId;
    }

    public void setToken(String token) {
        _log.e("test setToken::"+ token);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        _log.e("test setToken::"+ pref.getString("token", ""));
        return pref.getString("token", "");
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
               // setVisibilityToolbar(View.visi);
            } else {
                loginLogoutTv.setText("로그아웃");
                pushHistoryCl.setVisibility(View.VISIBLE);
                purchaseCl.setVisibility(View.VISIBLE);
                //setVisibilityToolbar(View.VISIBLE);
            }
        });
    }

    /**
     * push 알림 항목 클릭
     */
    public void notiClickEvent() {
        new Thread(() -> {
            try {
                _web.post(Constants.serverUrl + "read", "{\"userId\":\"userid04\",\"msgId\":\"msg1234\"}");
            } catch (Exception e) {
                _log.e(e.getMessage());
            }
        }).start();
    }

    /**
     * 로고 클릭
     */
    public void gotoMain(View view) {
        if (tgtrFragment != null) {
//            if(getUserCI().isEmpty()){                                                                                                                            
                tgtrFragment.changeUrl(Constants.mainUrl);
//            }else{
//                tgtrFragment.changeUrl(Constants.indexUrl);
         //   }

        }
    }

    public void clickCloseDrawer(View view) {
        closeDrawer(drawerLayout);
    }

    /**
     * 알림목록 메뉴 클릭
     * @param view
     */
    public void clickPushHistory(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.pushHistoryUrl);
        }

        closeDrawer(drawerLayout);
    }

    /**
     * 제출한 증명서 메뉴 클릭
     * @param view
     */
    public void clickPurchase(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.purchaseUrl);
        }

        closeDrawer(drawerLayout);
    }

    /**
     * 게시판 폴더 클릭
     * @param view
     */
    public void clickNoticeBoard(View view) {
        int visibility = noticeBoardItemCl.getVisibility();

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
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.noticeUrl);
        }

        closeDrawer(drawerLayout);
    }

    /**
     * 자주 묻는 질문 메뉴 클릭
     * @param view
     */
    public void clickFaq(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.faqUrl);
        }

        closeDrawer(drawerLayout);
    }

    /**
     * Q&A 메뉴 클릭
     * @param view
     */
    public void clickQa(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.qaUrl);
        }

        closeDrawer(drawerLayout);
    }

    /**
     * 챗봇 메뉴 클릭
     * @param view
     */
    public void clickChatbot(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.chatbotUrl);
        }

        closeDrawer(drawerLayout);
    }

    /**
     * 버전정보 메뉴 클릭
     * @param view
     */
    public void clickVersion(View view) {
        closeDrawer(drawerLayout);
        changeFragment("version");
    }

    /**
     * 로그인/로그아웃 메뉴 클릭
     * @param view
     */
    public void clickLoginAndLogout(View view) {
        _log.e("test login and logout" + getUserCI());
        if (getUserCI().isEmpty()) {
            if (tgtrFragment != null) {
                tgtrFragment.changeUrl(Constants.loginUrl);
            }
        } else {
            if (tgtrFragment != null) {
                tgtrFragment.clickLogout();
            }
        }

        closeDrawer(drawerLayout);
    }

    /**
     * splash 화면 안보이게 처리
     */
    public void removeSplashScreen() {
        runOnUiThread(new Runnable() {
            public void run() {
                splashFl.removeAllViews();
            }
        });
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
                String result = _web.post(Constants.appToken, data.toString());

               // handler.sendEmptyMessage(100);
            } catch (Exception e) {
                _log.e(e.getMessage());
            }
        }).start();
    }

//    class ValueHandler extends Handler {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            _log.e("test handdle::"+ msg);
//            if(msg.what == 100){
//                tgtrFragment.getPushCnt();
//            }
//        }
//    }


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
            System.exit(0);
        }
    }
}
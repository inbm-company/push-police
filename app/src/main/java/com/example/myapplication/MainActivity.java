package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
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

    private LinearLayout pushHistoryLl;
    private LinearLayout purchaseLl;
    private LinearLayout noticeBoardItemLl;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TextView loginLogoutTv;

    private String userId = "";
    private String backStr;

    private Toast toast;

    private long backKeyPressedTime = 0;

    private TgtrFragment tgtrFragment;
    private VersionInfoFragment versionInfoFragment;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("police", MODE_PRIVATE);

        Intent intent = getIntent();
        Boolean doClickEvent = intent.getBooleanExtra("notiClick", false);

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
        splashFl = findViewById(R.id.splash_fl);
        toolbarIcon = findViewById(R.id.toolbar_icon);
        toolbar = findViewById(R.id.toolbar);
        pushHistoryLl = findViewById(R.id.push_history_ll);
        purchaseLl = findViewById(R.id.purchase_ll);
        noticeBoardItemLl = findViewById(R.id.notice_board_item_ll);
        loginLogoutTv = findViewById(R.id.login_logout_tv);

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
            btnPurchase.setVisibility(View.GONE);
            btnChatbot.setVisibility(View.VISIBLE);

            if (!getUserId().isEmpty()) {
                btnPushHistory.setVisibility(View.VISIBLE);
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
        toolbar.setVisibility(visibility);
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

    public void setUserId(String id) {
        _log.simple("set id" + id);
        this.userId = id;
    }

    public String getUserId() {
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
            pushHistoryLl.setVisibility(View.GONE);
            purchaseLl.setVisibility(View.GONE);

            if (getUserId().isEmpty()) {
                loginLogoutTv.setText("로그인");
               // setVisibilityToolbar(View.visi);
            } else {
                loginLogoutTv.setText("로그아웃");
                pushHistoryLl.setVisibility(View.VISIBLE);
                purchaseLl.setVisibility(View.VISIBLE);
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
            if(getUserId().isEmpty()){
                tgtrFragment.changeUrl(Constants.mainUrl);
            }else{
                tgtrFragment.changeUrl(Constants.indexUrl);
            }

        }
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
        int visibility = noticeBoardItemLl.getVisibility();

        if (visibility == 0) {
            noticeBoardItemLl.setVisibility(View.GONE);
        } else {
            noticeBoardItemLl.setVisibility(View.VISIBLE);
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

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 100);
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
        _log.e("test login and logout" + getUserId());
        if (getUserId().isEmpty()) {
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
                data.put("perCrtUid", "(주)강원랜드-2022-0071-0001");
                _web.post(Constants.appToken, data.toString());
            } catch (Exception e) {
                _log.e(e.getMessage());
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
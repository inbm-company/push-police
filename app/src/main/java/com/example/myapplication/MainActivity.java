package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.widget.FrameLayout;
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
    private LinearLayout introducingItemLl;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Boolean doClickEvent = intent.getBooleanExtra("notiClick", false);

        // push 클릭하여 앱을 실행할경우 이벤트 시행
        if (doClickEvent) {
            notiClickEvent();
        }

        setLayout();
        setupFragment();
        changeLoginAndLogoutMenu();
    }

    private void setLayout() {
        drawerLayout = findViewById(R.id.mainDrawerView);
        splashFl = findViewById(R.id.splash_fl);
        toolbarIcon = findViewById(R.id.toolbar_icon);
        toolbar = findViewById(R.id.toolbar);
        pushHistoryLl = findViewById(R.id.push_history_ll);
        purchaseLl = findViewById(R.id.purchase_ll);
        introducingItemLl = findViewById(R.id.introducing_item_ll);
        noticeBoardItemLl = findViewById(R.id.notice_board_item_ll);
        loginLogoutTv = findViewById(R.id.login_logout_tv);

        backStr = this.getResources().getString(R.string.back_presssed);
        setToolbar("nor");
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
     * 로그상태에 따른 메뉴 분기
     *
     * @param status
     */
    private void setToolbar(String status) {
        if (status.equals("nor")) {
            // 메인 화면, 로그인 화면
            toolbarIcon.setVisibility(View.GONE);
        } else {
            // 나머지 화면
            toolbarIcon.setVisibility(View.VISIBLE);
        }
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

    public void changeFragment(String page) {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        if (page.equals("version")) {
            toolbar.setVisibility(View.GONE);
            if (versionInfoFragment == null) {
                versionInfoFragment = VersionInfoFragment.newInstance();
                transaction.add(R.id.frame_layout, versionInfoFragment).commit();
            } else {
                transaction.replace(R.id.frame_layout, versionInfoFragment).commit();
            }
        } else {
            toolbar.setVisibility(View.VISIBLE);

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
        return userId;
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
            } else {
                loginLogoutTv.setText("로그아웃");
                pushHistoryLl.setVisibility(View.VISIBLE);
                purchaseLl.setVisibility(View.VISIBLE);
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
     * 소개 폴더 클릭
     * @param view
     */
    public void clickIntroducing(View view) {
        int visibility = introducingItemLl.getVisibility();

        if (visibility == 0) {
            introducingItemLl.setVisibility(View.GONE);
        } else {
            introducingItemLl.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 시스템 소개 메뉴 클릭
     * @param view
     */
    public void clickIntroSystem(View view) {
        _log.e("시스템 소개 클릭::" + view);
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.introducingUrl);
        }

        closeDrawer(drawerLayout);
    }

    /**
     * 법적근거 메뉴 클릭
     * @param view
     */
    public void clickLegalBasis(View view) {
        _log.e("법적근거 클릭::" + view);
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.legalBasisUrl);
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
                new Thread(() -> {
                    try {
                        JSONObject data = new JSONObject();
                        data.put("appToken", token);
                        data.put("perCrtUid", "(주)강원랜드-2022-0071-0001");
                        _web.post(Constants.serverUrl + "app/token.do", data.toString());
                    } catch (Exception e) {
                        _log.e(e.getMessage());
                    }
                }).start();
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
            // 메뉴 언어 변경 후 백키로 종료, 앱 실행시 언어 변경이 안돼 주석 처리 함.
            System.exit(0);
        }
    }
}
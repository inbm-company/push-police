package kr.go.police.bgidocsubmit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import kr.go.police.bgidocsubmit.R;
import kr.go.police.bgidocsubmit.common.Constants;
import kr.go.police.bgidocsubmit.fragment.TgtrFragment;
import kr.go.police.bgidocsubmit.fragment.VersionInfoFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private ConstraintLayout pushHistoryCl;
    private ConstraintLayout purchaseCl;
    private ConstraintLayout noticeBoardItemCl;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TextView loginLogoutTv;
    private TextView pushBadge;
    private TextView barPushBadge;

    private ConstraintLayout networkErrorLl;
    private Button btnRetryConnect;

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

        // ???????????? ??????
        NetworkRequest.Builder networkBuilder = new NetworkRequest.Builder();
        networkBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        cm.registerNetworkCallback(networkBuilder.build(), networkCallback);

        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("police", MODE_PRIVATE);

        Intent intent = getIntent();
        Boolean doClickEvent = intent.getBooleanExtra("notiClick", false);

        // push ???????????? ?????? ??????????????? ????????? ??????
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

        networkErrorLl = findViewById(R.id.network_error_ll);
        btnRetryConnect = networkErrorLl.findViewById(R.id.btn_retry);

        pushBadge = findViewById(R.id.push_badge);
        barPushBadge = findViewById(R.id.bar_push_badge);

        //???????????? ????????? ??????
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
            if(tgtrFragment != null){
                tgtrFragment.webViewReload();
            }
        }
    }

    /**
     * ??? ????????? ???????????? ???????????????
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
     * @param drawerLayout
     */
    private void closeDrawer(DrawerLayout drawerLayout) {
        drawerLayout.closeDrawer(GravityCompat.END);
    }

    /**
     * drawer layout close
     */
    public void callCloseDrawer(){
        if(drawerLayout != null){
            _log.e("test close::"+ drawerLayout );
            noticeBoardItemCl.setVisibility(View.GONE);
            closeDrawer(drawerLayout);
        }
    }

    /**
     * drawer layout ?????? ?????? ??????
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

            if (!getUserCI().isEmpty()) {
                btnPushHistory.setVisibility(View.VISIBLE);
                btnPurchase.setVisibility(View.VISIBLE);
            }
        });
    }

    public void changeFragment(String page) {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        if (page.equals("version")) {
            if (versionInfoFragment == null) {
                versionInfoFragment = VersionInfoFragment.newInstance();
                transaction.add(R.id.frame_layout, versionInfoFragment).commit();
            } else {
                transaction.replace(R.id.frame_layout, versionInfoFragment).commit();
            }
        } else {
            if (tgtrFragment == null) {
                tgtrFragment = TgtrFragment.newInstance();
                transaction.add(R.id.frame_layout, tgtrFragment).commitNow();
            } else {
                transaction.replace(R.id.frame_layout, tgtrFragment).commit();
            }
        }
    }

    // ????????? ?????? ??????
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
     * ?????????/???????????? ?????? ?????? ??????
     */
    public void changeLoginAndLogoutMenu() {
        runOnUiThread(() -> {
            pushHistoryCl.setVisibility(View.GONE);
            purchaseCl.setVisibility(View.GONE);

            if (getUserCI().isEmpty()) {
                loginLogoutTv.setText("?????????");
            } else {
                loginLogoutTv.setText("????????????");
                pushHistoryCl.setVisibility(View.VISIBLE);
                purchaseCl.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * push ?????? ?????? ??????
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
     * ?????? ??????
     */
    public void gotoMain(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.mainUrl);
        }
    }

    public void clickCloseDrawer(View view) {
        callCloseDrawer();
    }

    /**
     * ???????????? ?????? ??????
     * @param view
     */
    public void clickPushHistory(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.pushHistoryUrl);
        }

        callCloseDrawer();
    }

    /**
     * ????????? ????????? ?????? ??????
     * @param view
     */
    public void clickPurchase(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.purchaseUrl);
        }

        callCloseDrawer();
    }

    /**
     * ????????? ?????? ??????
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
     * ???????????? ?????? ??????
     * @param view
     */
    public void clickNotice(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.noticeUrl);
        }

        callCloseDrawer();
    }

    /**
     * ?????? ?????? ?????? ?????? ??????
     * @param view
     */
    public void clickFaq(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.faqUrl);
        }

        callCloseDrawer();
    }

    /**
     * Q&A ?????? ??????
     * @param view
     */
    public void clickQa(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.qaUrl);
        }

        callCloseDrawer();
    }

    /**
     * ?????? ?????? ??????
     * @param view
     */
    public void clickChatbot(View view) {
        if (tgtrFragment != null) {
            tgtrFragment.changeUrl(Constants.chatbotUrl);
        }

        callCloseDrawer();
    }

    /**
     * ???????????? ?????? ??????
     * @param view
     */
    public void clickVersion(View view) {
        callCloseDrawer();

        Intent intent = new Intent( this, VersionInfoActivity.class);
        startActivity(intent);
        //changeFragment("version");
    }

    /**
     * ?????????/???????????? ?????? ??????
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
                // ???????????? ???????????? ???.
                data.put("memberCi", getUserCI());
                String result = _web.post(Constants.appToken, data.toString());

               // handler.sendEmptyMessage(100);
            } catch (Exception e) {
                _log.e(e.getMessage());
            }
        }).start();
    }

    /**
     * ?????? ??? ??????
     */
    public void uploadToken() {
        // ?????? ????????? ???????????? ?????????
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
        public void onLost(Network network) {   //???????????? ?????? ???
            _log.e("abs onLost");
            networkChangeFlag = true;
            netWorkConnected = false;
        }

        @Override
        public void onAvailable(Network network) {   //???????????? ???????????? ??????
            _log.e("abs onAvailable::"+ network);
            networkChangeFlag = true;
            netWorkConnected = true;
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            _log.e("abs onCapabilitiesChanged::"+ networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));

            //???????????? ?????? ??????.
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
        // drawer open???
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
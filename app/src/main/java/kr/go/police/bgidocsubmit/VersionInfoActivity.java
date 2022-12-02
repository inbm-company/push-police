package kr.go.police.bgidocsubmit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import kr.go.police.bgidocsubmit.common.GetMarketVersion;


public class VersionInfoActivity extends AppCompatActivity {

    // 경창청 스토어로 수정 필요
    private final String STORE_URL = "https://play.google.com/store/apps/details?id=com.police";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_versioninfo);

        initDisplay();
    }

    public void initDisplay() {
        ImageButton ivToolbarBack = findViewById(R.id.iv_back);
        TextView tvVersion = findViewById(R.id.tv_version);
        TextView tvNoti = findViewById(R.id.tv_noti);
        Button btnUpdate = findViewById(R.id.btn_update);

        ivToolbarBack.setOnClickListener(v -> {
            this.finish();
        });

        tvNoti.setText(R.string.new_version);
        String version = getResources().getString(R.string.app_cur_ver) + getVersionInfo(App.getStaticContext());
        //버전정보 게시
        tvVersion.setText(version);
        //btnUpdate.setVisibility(View.GONE);

        //버전정보 맞지않으면 업데이트 버튼 보이게
        new GetMarketVersion(App.getStaticContext()).execute();

        btnUpdate.setVisibility(View.GONE);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent marketLaunch = new Intent(
                        Intent.ACTION_VIEW);
                marketLaunch.setData(Uri
                        .parse(STORE_URL));
                startActivity(marketLaunch);
            }
        });

    }

    /**
     * 앱 버전 정보
     * @param mContext
     * @return
     */
    public String getVersionInfo(Context mContext) {
        String version = null;

        try {
            PackageInfo i = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }

    public void clickBack(View view){
        onBackPressed();
    }
}

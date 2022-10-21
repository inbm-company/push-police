package com.example.myapplication.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication._log;
import com.example.myapplication.common.GetMarketVersion;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class VersionInfoFragment extends Fragment {

    View view;

    ImageButton ivToolbarBack;
    TextView tvVersion;
    TextView tvNotification;
    TextView tvNonNewVersion;
    Button btnUpdate;

    // 경창청 스토어로 수정 필요
    private final String STORE_URL = "https://play.google.com/store/apps/details?id=com.police";

    public static VersionInfoFragment newInstance() {
        VersionInfoFragment  fragment = new VersionInfoFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_versioninfo, container, false);
        buildUI();
        return view;
    }

    private void buildUI() {
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvVersion = view.findViewById(R.id.tv_version);
        tvNotification = view.findViewById(R.id.tv_notification);
        tvNonNewVersion = view.findViewById(R.id.tv_non_new_version);
        btnUpdate = view.findViewById(R.id.btn_update);

        ivToolbarBack.setOnClickListener(v -> {
            ((MainActivity) getActivity()).changeFragment("tgtr");
        });
        btnUpdate.setVisibility(View.GONE);

        String version = getResources().getString(R.string.app_cur_ver) + getVersionInfo(this.getContext());
        //버전정보 게시
        tvVersion.setText(version);

        //버전정보 맞지않으면 업데이트 버튼 보이게
        new GetMarketVersion(this.getContext()).execute();

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

    public String getVersionInfo(Context mContext) {
        String version = null;

        try {
            PackageInfo i = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }

    public void fragmentRefresh(){
        _log.e("VersionInfoFragment fragmentRefresh");
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.detach(this).attach(this).commitAllowingStateLoss();
    }
}
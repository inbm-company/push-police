package kr.go.police.bgidocsubmit.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import kr.go.police.bgidocsubmit._log;

public class GetMarketVersion extends AsyncTask<Void, Void, String> {

    private final String APP_VERSION_NAME;
    // 경창청 스토어로 수정 필요
    private final String STORE_URL = "https://play.google.com/store/apps/details?id=com.police";
    private Context mContext = null;

    public GetMarketVersion(Context context){
        mContext = context;
        APP_VERSION_NAME = getVersionInfo(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
 //       try {
//            Document doc = Jsoup.connect(STORE_URL)
//                    .referrer("https://play.google.com/")
//                    .userAgent("Chrome")
//                    .ignoreHttpErrors(true)
////                    .header("scheme", "https")
////                    .header("accept", "*/*")
////                    .header("accept-encoding", "gzip, deflate, br")
////                    .header("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
////                    .timeout(1000 * 5)
//                    .get();
//
//            Elements Version = doc.select(".htlgb");
//            _log.e("test version::" + doc+"/"+Version);
//
//            for (int i = 0; i < Version.size(); i++) {
//
//
//                String VersionMarket = Version.get(i).text();
//                _log.e("test version::" + VersionMarket);
//
//                if (Pattern.matches("^[0-9]{2}.[0-9]{2}.[0-9]{2}$", VersionMarket)) {
//                    _log.e("test onPostExecute ggg::" + VersionMarket);
//                    return VersionMarket;
//                }
//            }
//        }catch (HttpStatusException e1){
//            _log.e("test jsoup::"+ e1.getMessage() +"/"+ e1.getStatusCode());
//        } catch (IOException e) {
//            e.printStackTrace();
//
//        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) { //s는 마켓의 버전입니다.
//        if (s != null) {
//            if (!s.equals(APP_VERSION_NAME)) { //APP_VERSION_NAME는 현재 앱의
//                ((AppinformationActivity) mContext).tvNotification.setVisibility(View.GONE);//.tvNotification.setVisibility(View.GONE);
//                ((AppinformationActivity) mContext).btnUpdate.setVisibility(View.VISIBLE);
//                ((AppinformationActivity) mContext).tvNonNewVersion.setVisibility(View.VISIBLE);
//            }else{
//                ((AppinformationActivity) mContext).tvNotification.setVisibility(View.VISIBLE);
//            }
//        }
       super.onPostExecute(s);
    }

    public String getVersionInfo(Context context) {
        String version = null;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            _log.m("PackageManager.NameNotFoundException");
        }
        return version;
    }
}
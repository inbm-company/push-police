package kr.go.police.bgidocsubmit;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        App.context = getApplicationContext();

        // 키보드 보안
        //IxSecureManager.initLicense(this, "B27AC1F26BE1", "POLICE_01"); // 라이선스 설정
    }

    public static Context getStaticContext() {
        return App.context;
    }

    public static void _toast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}

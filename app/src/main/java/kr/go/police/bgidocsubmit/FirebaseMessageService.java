package kr.go.police.bgidocsubmit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessageService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = App.getStaticContext().getResources().getString(R.string.noti_channel_id);

    private static final CharSequence CHANNEL_NAME = "bgidocsubmit_push";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // fcm 토큰 재발급 시에 호출됨
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        SharedPreferences.Editor editor = App.getSharedPreferences().edit();
        editor.putString("token", token);
        editor.apply();
    }

    // 알림을 받을시에 호출
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        _log.e("onMessageReceived"+ remoteMessage.getData().size());
        if(remoteMessage.getData().size() > 0){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            NotificationCompat.Builder builder = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            }else {
                builder = new NotificationCompat.Builder(getApplicationContext());
            }

            Notification notification = popupMessage(builder, remoteMessage);
            notificationManager.notify(1, notification);
        }
    }

    private Notification popupMessage(NotificationCompat.Builder builder, RemoteMessage remoteMessage){
        String title = remoteMessage.getData().get("title"); //remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getData().get("body");// remoteMessage.getNotification().getBody();

        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.putExtra("notiClick",true);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent notifyPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifyPendingIntent = PendingIntent.getActivity(
                    this, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE
            );
        }
        else{
            notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        builder.setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true) //알림 클릭시 자동 제거
                .setContentIntent(notifyPendingIntent);

        Notification notification = builder.build();
        return notification;
    }
}

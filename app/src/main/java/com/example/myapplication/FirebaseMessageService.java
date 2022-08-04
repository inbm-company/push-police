package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;





public class FirebaseMessageService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "00";

    private static final CharSequence CHANNEL_NAME = "testpush";

    private static final String KEY_TEXT_REPLY = "key_text_reply";

    private final String serverUrl = "http://192.168.10.152:3000/token";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("token is" , token);

        try {
            _web.post(serverUrl, token);
        } catch (Exception e) {
            _log.e(e.getMessage());
        }

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        _log.simple("in onmessage");
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
    _log.simple("in onmessage over builder");

        Notification notification = popupMessage(builder, remoteMessage);
        notificationManager.notify(1, notification);

    }

    private Notification popupMessage(NotificationCompat.Builder builder, RemoteMessage remoteMessage){
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        _log.simple("in buildMessage local key"+  remoteMessage.getNotification().getBodyLocalizationKey());
        _log.simple("in buildMessage Priority"+  remoteMessage.getNotification().getNotificationPriority());
        _log.simple("in buildMessage local args"+  remoteMessage.getNotification().getBodyLocalizationArgs());

        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.putExtra("notiClick",true);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

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
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(notifyPendingIntent);

        Notification notification = builder.build();
        return notification;
    }
}

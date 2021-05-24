package yc.ac.smartattendance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService  {
    public MyFirebaseMessagingService() {
    }

    /* 토큰이 새로 만들어질때나 refresh 될때  */
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        //Log.e("NEW_TOKEN", refreshedToken);
        Log.d("새로운 토큰 발급 또는 refresh", s);
        /* DB서버로 새토큰을 업데이트시킬수 있는 부분 */
    }

    /* 메세지를 새롭게 받을때 */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("메세지", "From: " + remoteMessage.getFrom());

        /* 새메세지를 알림기능을 적용하는 부분 */

        if (remoteMessage.getNotification() != null) {
            Log.d("알림", "From: " + remoteMessage.getFrom());
            Log.d("알림", "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String messageBody = remoteMessage.getNotification().getBody();
            String messageTitle = remoteMessage.getNotification().getTitle();

            sendNotification(messageBody, messageTitle);
        }
    }


    private void sendNotification(String messageBody, String messageTitle) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_baseline_favorite_24)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}
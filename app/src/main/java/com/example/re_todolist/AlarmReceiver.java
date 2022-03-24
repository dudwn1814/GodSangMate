package com.example.re_todolist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 111;


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        String activity = bundle.getString("activity");
        //Intent intentService = new Intent(context, AlarmService.class);
        //intentService.putExtra("activity", activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //context.startForegroundService(intentService);
            Log.d("alarmCheck", "alarm");

            int intentID = bundle.getInt("intentID");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent fullScreenIntent = new Intent(context, AlarmPage.class);
            fullScreenIntent.putExtra("activity", activity);
            context.startActivity(fullScreenIntent);

            fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, intentID,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder full_builder = null;
            //OREO API 26 이상에서는 채널 필요
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                context.startForegroundService(fullScreenIntent);

                String channelID = activity;
                String channelName = "매일 알람 채널";
                String description = "매일 정해진 시간에 알람합니다.";
                int importance = NotificationManager.IMPORTANCE_HIGH; //소리와 알림메시지를 같이 보여줌

                NotificationChannel channel = new NotificationChannel(activity, channelName, importance);
                full_builder  = new NotificationCompat.Builder(context, "CHANNEL_ID");
                channel.setDescription(description);

                if (notificationManager != null) {
                    // 노티피케이션 채널을 시스템에 등록
                    notificationManager.createNotificationChannel(channel);
                }
            }

            full_builder
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.loading_icon)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(fullScreenPendingIntent)
                    .setContentTitle("갓생메이트와 약속한 그룹 TODO")
                    .setContentText("<" + activity + ">" + " 해야 할 시간입니다!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(fullScreenPendingIntent, true);

            notificationManager.notify(111, full_builder.build());

        } else {
            //context.startService(intentService);
        }
    }
}
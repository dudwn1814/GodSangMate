package com.example.re_todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

//import com.service.RescheduleAlarmsService;

import java.util.Calendar;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    /*public static final String MONDAY = "MONDAY";
    public static final String TUESDAY = "TUESDAY";
    public static final String WEDNESDAY = "WEDNESDAY";
    public static final String THURSDAY = "THURSDAY";
    public static final String FRIDAY = "FRIDAY";
    public static final String SATURDAY = "SATURDAY";
    public static final String SUNDAY = "SUNDAY";
    public static final String RECURRING = "RECURRING";
    public static final String TITLE = "TITLE";

     */

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        boolean repeat = bundle.getBoolean("repeat");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            String toastText = String.format("Alarm Reboot");
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        } else {
            String toastText = String.format("Alarm Received");
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
            if (repeat) {
                startAlarmService(context, intent);
            }
        }
    }

    private void startAlarmService(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String activity = bundle.getString("activity");
        Intent intentService = new Intent(context, AlarmService.class);
        intentService.putExtra("activity", activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
    }

    /*private void startRescheduleAlarmsService(Context context) {
        Intent intentService = new Intent(context, RescheduleAlarmsService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
    }

     */
}

package com.example.re_todolist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class DateChangeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ((Activity) context).finish();
        ((Activity)context).overridePendingTransition(0, 0);
        ((Activity) context).startActivity(new Intent(context, MainActivity.class));
        ((Activity)context).overridePendingTransition(0, 0);
    }
}
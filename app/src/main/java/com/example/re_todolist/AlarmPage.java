package com.example.re_todolist;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Random;

public class AlarmPage extends AppCompatActivity {

   /* public void onReceive(Context context, Intent intent) {
        setContentView(R.layout.alarm_page);
        Bundle bundle = intent.getExtras();
        String activity = bundle.getString("activity");
        boolean repeat = bundle.getBoolean("repeat");
    }
*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_page);
        Button dismiss = findViewById(R.id.dismiss);
        TextView textView = findViewById(R.id.textView);

        Log.d("AlarmPageCHeck", "AlarmPage 들어옴");

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
                getApplicationContext().stopService(intentService);
                Intent mainIntent = new Intent(AlarmPage.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

        animateClock();
    }

    private void animateClock() {
        ImageView clock = findViewById(R.id.clock);
        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(clock, "rotation", 0f, 20f, 0f, -20f, 0f);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setDuration(800);
        rotateAnimation.start();
    }
}

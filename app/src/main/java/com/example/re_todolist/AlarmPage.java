package com.example.re_todolist;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AlarmPage extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
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

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String activity = intent.getExtras().getString("activity");
        boolean repeat = intent.getExtras().getBoolean("repeat");
        String tdid = intent.getExtras().getString("tdid");
        textView.setText(activity+ " ToDo를 실행할 시간입니다!");
/*
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.setLooping(true);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);


        mediaPlayer.start();
        long[] pattern = { 0, 100, 1000 };
        vibrator.vibrate(pattern, 0);
 */
        Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
        startService(intentService);

        Log.d("AlarmPageCHeck", "AlarmPage 들어옴");

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intentService);
                if (repeat) {
                    String uid = mAuth.getUid();
                    mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");
                    mDbRef.child("UserAccount").child(uid).child("g_code").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot snapshot = task.getResult();
                                if (snapshot != null) {
                                    String groupCode = snapshot.getValue().toString();
                                    mDbRef.child("Alarm").child(groupCode).child(tdid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DataSnapshot snapshot = task.getResult();
                                                if(snapshot != null){
                                                    String time = snapshot.child("alarm_time").getValue().toString();
                                                    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
                                                    Date date;
                                                    //String str = "2020-03-01 12:00:01";

                                                    try {
                                                        if (time != null) {
                                                            date = sd.parse(time);

                                                            Log.d("알람 파싱", "날짜" + date);
                                                            Calendar calendar = Calendar.getInstance();
                                                            if (date != null) {
                                                                calendar.setTime(date);
                                                                calendar.add(Calendar.DATE,1);
                                                                String next = sd.format(calendar.getTime());
                                                                mDbRef.child("Alarm").child(groupCode).child(tdid).child("alarm_time").setValue(next);
                                                            }

                                                        }
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                    Intent mainIntent = new Intent(AlarmPage.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                    //mediaPlayer.stop();
                    //vibrator.cancel();
                }
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

package com.example.re_todolist;

import static com.example.re_todolist.GroupName.groupcode;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class TodoWriteActivity extends AppCompatActivity {

    private boolean alarm = false;
    private boolean repeat = false;
    private String group;
    private String todo = "";
    private String day;


    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    String groupCode, groupName, uid;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();


        //메인 페이지로 이동
        Button cancelBtn = findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(view -> {
            Intent mainIntent = new Intent(TodoWriteActivity.this, MainActivity.class);
            TodoWriteActivity.this.startActivity(mainIntent);
        });


        //spinner 연결
        String[] items = {"개인", "그룹"};

        Spinner spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                group = items[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                group = "개인";
            }
        });


        //알람 체크
        CheckBox alarmChk = findViewById(R.id.alarmChk);
        alarmChk.setOnClickListener(view -> alarm = !alarm);


        //반복 체크
        CheckBox repeatChk = findViewById(R.id.repeatChk);
        repeatChk.setOnClickListener(view -> repeat = !repeat);


        //요일 설정
        Button sunBtn = findViewById(R.id.sunBtn);
        Button monBtn = findViewById(R.id.monBtn);
        Button tueBtn = findViewById(R.id.tueBtn);
        Button wedBtn = findViewById(R.id.wedBtn);
        Button thuBtn = findViewById(R.id.thuBtn);
        Button friBtn = findViewById(R.id.friBtn);
        Button satBtn = findViewById(R.id.satBtn);

        sunBtn.setOnClickListener(view -> day = "sun");
        monBtn.setOnClickListener(view -> day = "mon");
        tueBtn.setOnClickListener(view -> day = "tue");
        wedBtn.setOnClickListener(view -> day = "wed");
        thuBtn.setOnClickListener(view -> day = "thu");
        friBtn.setOnClickListener(view -> day = "fri");
        satBtn.setOnClickListener(view -> day = "sat");


        //Todo 입력
        EditText todoText = findViewById(R.id.textInput);

//알람 시간 입력
        final TimePicker picker = (TimePicker) findViewById(R.id.timePicker);
        picker.setIs24HourView(true);


        // 앞서 설정한 값으로 보여주기
        // 없으면 디폴트 값은 현재시간
        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

        Calendar nextNotifyTime = new GregorianCalendar();
        nextNotifyTime.setTimeInMillis(millis);

        Date nextDate = nextNotifyTime.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDate);
        Toast.makeText(getApplicationContext(), "[처음 실행시] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();


        // 이전 설정값으로 TimePicker 초기화
        Date currentTime = nextNotifyTime.getTime();
        SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
        SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
        int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));


        if (Build.VERSION.SDK_INT >= 23) {
            picker.setHour(pre_hour);
            picker.setMinute(pre_minute);
        } else {
            picker.setCurrentHour(pre_hour);
            picker.setCurrentMinute(pre_minute);
        }

        //투두 저장
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            todo = (todoText.getText().toString().isEmpty() ? "" : todoText.getText().toString());
            int hour, hour_24, minute;
            String am_pm;
            if (Build.VERSION.SDK_INT >= 23) {
                hour_24 = picker.getHour();
                minute = picker.getMinute();
            } else {
                hour_24 = picker.getCurrentHour();
                minute = picker.getCurrentMinute();
            }
            if (hour_24 > 12) {
                am_pm = "PM";
                hour = hour_24 - 12;
            } else {
                hour = hour_24;
                am_pm = "AM";
            }

            // 현재 지정된 시간으로 알람 시간 설정
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour_24);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            Date currentDateTime = calendar.getTime();
            String datetext = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
            Toast.makeText(getApplicationContext(), datetext + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

            //  Preference에 설정한 값 저장
            SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
            editor.putLong("nextNotifyTime", (long) calendar.getTimeInMillis());
            editor.apply();

            diaryNotification(calendar);

            String time = calendar.getTime().toString();

            // TODO: 2022-02-23 not empty 조건 넣기
            ToDo todoObj = new ToDo(todo, group, repeat, day, alarm, time + am_pm.toUpperCase(), "tmp");

            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            uid = firebaseUser.getUid();

            mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserAccount user = dataSnapshot.getValue(UserAccount.class);
                    groupCode = user.getG_code();

                    mDbRef.child("TodoList").child(uid).child(groupCode).push().setValue(todoObj);

                    mDbRef.child("TodoList").child(uid).child(groupCode).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                String key = postSnapshot.getKey();
                                String todoString = postSnapshot.child("todoId").getValue().toString();

                                if (todoString != null && todoString.equals("tmp")) {
                                    mDbRef.child("TodoList").child(uid).child(groupCode).child(key).child("todoId").setValue(key);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.v("TAG", "loadPost:onCancelled", error.toException());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "그룹코드 가져오기 오류",
                            Toast.LENGTH_LONG).show();
                }
            });

            Toast.makeText(getApplicationContext(), "등록완료", Toast.LENGTH_LONG).show();
            finish();
        });
    }


    void diaryNotification(Calendar calendar) {
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean dailyNotify = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DAILY_NOTIFICATION, true);
        Boolean dailyNotify = true; // 무조건 알람을 사용

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        // 사용자가 매일 알람을 허용했다면
        if (dailyNotify) {

            if (alarmManager != null) {

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        }
//        else { //Disable Daily Notifications
//            if (PendingIntent.getBroadcast(this, 0, alarmIntent, 0) != null && alarmManager != null) {
//                alarmManager.cancel(pendingIntent);
//                //Toast.makeText(this,"Notifications were disabled",Toast.LENGTH_SHORT).show();
//            }
//            pm.setComponentEnabledSetting(receiver,
//                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                    PackageManager.DONT_KILL_APP);
//        }
    }
}
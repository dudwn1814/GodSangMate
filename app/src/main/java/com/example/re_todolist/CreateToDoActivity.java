package com.example.re_todolist;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class CreateToDoActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    String groupCode, uid, activity, TDId;
    boolean group, repeat, alarm;
    String time;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");
        //FirebaseUser firebaseUser = mAuth.getCurrentUser();
        //uid = firebaseUser.getUid();

        uid = "user1";
        groupCode = "ABC123";

        //메인 페이지로 이동
        Button cancelBtn = findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(view -> {
            Intent mainIntent = new Intent(CreateToDoActivity.this, MainActivity.class);
            startActivity(mainIntent);
        });

        //spinner 연결(그룹/개인)
        String[] items = {"개인", "그룹"};
        Spinner spinner = findViewById(R.id.spinner);

        LinearLayout timeLayout = findViewById(R.id.timeLinearLayout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 1) {
                    group = true;
                    timeLayout.setVisibility(View.VISIBLE);
                } else {
                    group = false;
                    timeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                group = false;
            }
        });

        //반복 체크
        repeat = false;
        CheckBox repeatChk = findViewById(R.id.repeatChk);
        LinearLayout repeatLayer = findViewById(R.id.linearLayout);
        repeatChk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                repeat = true;
                repeatLayer.setVisibility(View.VISIBLE);
            } else {
                repeat = false;
                repeatLayer.setVisibility(View.GONE);
            }
        });


        //알람 체크
        //alarm = Boolean.FALSE;
        CheckBox alarmChk = findViewById(R.id.alarmChk);
        TimePicker timePicker = findViewById(R.id.timePicker);
        alarmChk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                alarm = true;
                timePicker.setVisibility(View.VISIBLE);
            } else {
                alarm = false;
                timePicker.setVisibility(View.GONE);
            }
        });

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
            activity = (todoText.getText().toString().isEmpty() ? "" : todoText.getText().toString());
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
            time = calendar.getTime().toString();

            //투두 객체 생성
            ToDoPrac todoObj = new ToDoPrac();
            todoObj.setActivity(activity);
            todoObj.setRepeat(repeat);
            todoObj.setUid(uid);

            //DB에 입력한 날짜 저장
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String writeDate = format.format(date);

            if (group) {
                todoObj.setAlarm(alarm);
                if (alarm) todoObj.setTime(time + am_pm.toUpperCase());
                TDId = mDbRef.child("ToDoList").child(groupCode).child(writeDate).child("Group").push().getKey();
                todoObj.setTdid(TDId);
                mDbRef.child("ToDoList").child(groupCode).child(writeDate).child("Group").child(TDId).setValue(todoObj);
            } else {
                TDId = mDbRef.child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).push().getKey();
                todoObj.setTdid(TDId);
                mDbRef.child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).child(TDId).setValue(todoObj);
            }

            Toast.makeText(getApplicationContext(), "등록완료", Toast.LENGTH_LONG).show();
            finish();
        });
    }


    void diaryNotification(Calendar calendar) {
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean dailyNotify = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DAILY_NOTIFICATION, true);
        boolean dailyNotify = true; // 무조건 알람을 사용

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
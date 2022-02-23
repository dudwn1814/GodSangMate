package com.example.re_todolist;

import static com.example.re_todolist.GroupName.groupcode;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class TodoWriteActivity extends AppCompatActivity {

    private boolean alarm = false;
    private boolean repeat = false;
    private String group;
    private String todo = "";
    private String day;
    private String time_hour = "00";
    private String time_minute = "00";
    private String time_ampm = "AM";

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

        uid = "user1";

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


        //시간 입력
        EditText hour = findViewById(R.id.hour);
        EditText minute = findViewById(R.id.minute);
        EditText ampm = findViewById(R.id.ampm);


        //투두 저장
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            todo = (todoText.getText().toString().isEmpty() ? "" : todoText.getText().toString());

            time_hour = (hour.getText().toString().isEmpty() ? "00" : hour.getText().toString());
            time_minute = (minute.getText().toString().isEmpty() ? "00" : minute.getText().toString());
            time_ampm = (ampm.getText().toString().isEmpty() ? "AM" : ampm.getText().toString());

            // TODO: 2022-02-23 not empty 조건 넣기
            ToDo todoObj = new ToDo(todo, group, repeat, day, alarm, time_hour + time_minute + time_ampm.toUpperCase());

            mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserAccount user = dataSnapshot.getValue(UserAccount.class);
                    groupCode = user.getG_code();

                    mDbRef.child("TodoList").child(uid).child(groupCode).push().setValue(todoObj);
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
}
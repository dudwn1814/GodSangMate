package com.example.re_todolist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Console;
import java.util.HashMap;


public class CalendarActivity extends AppCompatActivity {

    private boolean alarm = false;
    private boolean repeat = false;
    private String group;
    private String todo = "";
    private String day;
    private String time_hour = "00";
    private String time_minute = "00";
    private String time_ampm = "AM";


    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        Button cancelBtn = findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(CalendarActivity.this, MainActivity.class);
                CalendarActivity.this.startActivity(mainIntent);
            }
        });


        Spinner spinner = findViewById(R.id.spinner);
        String[] items = {"개인", "그룹"};

        //spinner 연결
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

        CheckBox alarmChk = findViewById(R.id.alarmChk);
        alarmChk.setOnClickListener(view -> {
            alarm = !alarm;
        });

        CheckBox repeatChk = findViewById(R.id.repeatChk);
        repeatChk.setOnClickListener(view -> {
            repeat = !repeat;
        });

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


        TextView todoText = (TextView) findViewById(R.id.todoText);

        EditText hour = (EditText) findViewById(R.id.hour);

        EditText minute = (EditText) findViewById(R.id.minute);

        EditText ampm = (EditText) findViewById(R.id.ampm);


        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            todo = (todoText.getText().toString().isEmpty() ? "" : todoText.getText().toString());
            time_hour = (hour.getText().toString().isEmpty() ? "00" : hour.getText().toString());
            time_minute = (minute.getText().toString().isEmpty() ? "00" : minute.getText().toString());
            time_ampm = (ampm.getText().toString().isEmpty() ? "AM" : ampm.getText().toString());

            //not empty 조건 넣기
            Course todoObj = new Course(todo, group, repeat, day, alarm, time_hour + time_minute + time_ampm.toUpperCase());

           /* mDatabase.child("todos").child(todo).setValue(todoObj)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "저장을 완료했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "저장을 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
            */

            String info = todo + " " + repeat + " " + day + " " + alarm + " " + group + " " + time_hour + time_minute + time_ampm.toUpperCase();
            Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
            //Intent intent = new Intent(this, MainActivity.class);
            //intent.putExtra("todo", info);
            //setResult(Activity.RESULT_OK, intent);
            finish();
        });
    }
}
package com.example.re_todolist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class TodoWriteActivity extends AppCompatActivity {

    private boolean alarm = false;
    private boolean repeat = false;
    private String group;
    private String todo = "";
    private String day;
    private String time_hour = "00";
    private String time_minute = "00";
    private String time_ampm = "AM";

    private String uid;         //firebase uid
    FirebaseAuth mAuth;
    DatabaseReference mDbRef;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");

        Button cancelBtn = findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(view -> {
            Intent mainIntent = new Intent(TodoWriteActivity.this, MainActivity.class);
            TodoWriteActivity.this.startActivity(mainIntent);
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
        alarmChk.setOnClickListener(view -> alarm = !alarm);

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


        EditText todoText = findViewById(R.id.textInput);

        EditText hour = findViewById(R.id.hour);

        EditText minute = findViewById(R.id.minute);

        EditText ampm = findViewById(R.id.ampm);


        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            todo = (todoText.getText().toString().isEmpty() ? "" : todoText.getText().toString());
            time_hour = (hour.getText().toString().isEmpty() ? "00" : hour.getText().toString());
            time_minute = (minute.getText().toString().isEmpty() ? "00" : minute.getText().toString());
            time_ampm = (ampm.getText().toString().isEmpty() ? "AM" : ampm.getText().toString());

            //not empty 조건 넣기
            ToDo todoObj = new ToDo(todo, group, repeat, day, alarm, time_hour + time_minute + time_ampm.toUpperCase());
            //FirebaseUser firebaseUser = mAuth.getCurrentUser();
            //UserAccount account = new UserAccount();
            //account.setUid(firebaseUser.getUid());
            // TODO: 2022-02-15 test uid
            mDbRef.child("todoTest").child("cF6jxtem0SaEtCVSdGEVCj6r31R2").setValue(todoObj);


            Toast.makeText(getApplicationContext(), "등록완료", Toast.LENGTH_LONG).show();
            finish();
        });
    }

}
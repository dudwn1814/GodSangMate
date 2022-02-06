package com.example.re_todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    Button button1, button2;
    TextInputLayout Id_layout, Pw_layout;
    EditText Id, Pw;
    String email, password;
    com.example.re_todolist.myDBhelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Id_layout = findViewById(R.id.ID);
        Id = Id_layout.getEditText();
        Pw_layout = findViewById(R.id.PW);
        Pw = Pw_layout.getEditText();

        button1 = findViewById(R.id.b_login);
        button2 = findViewById(R.id.b_register);

        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage("아이디 또는 비밀번호가 잘못 입력 되었습니다.");
        alert_confirm.setPositiveButton("확인", null);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = Id.getText().toString();
                password = Pw.getText().toString();

                /* if(!myDB.validation(email, password)) {
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                    } */
                //else
                //if 그룹 있으면 메인 화면(to do list)으로 이동
                //else 그룹 선택 화면으로 이동
                Toast.makeText(getApplicationContext(), "로그인 되었습니다.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), com.example.re_todolist.JoinActivity.class);
                startActivity(intent);
            }
        });
    }
}
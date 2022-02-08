package com.example.re_todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    Button button1, button2;
    TextInputLayout Id_layout, Pw_layout;
    EditText Id, Pw;
    String email, password;
    Boolean LIstate;
    AlertDialog.Builder alert_confirm;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Id_layout = findViewById(R.id.ID);
        Pw_layout = findViewById(R.id.PW);
        Id = Id_layout.getEditText();
        Pw = Pw_layout.getEditText();
        button1 = findViewById(R.id.b_login);
        button2 = findViewById(R.id.b_register);

        mAuth = FirebaseAuth.getInstance();

        alert_confirm = new AlertDialog.Builder(this);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = Id.getText().toString();
                password = Pw.getText().toString();

                alert_confirm.setMessage("아이디 또는 비밀번호가 잘못 입력 되었습니다.");
                alert_confirm.setPositiveButton("확인", null);

                validation(email, password);
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

    private void validation(String email, String password){
        if(email.length() != 0 && password.length() != 0){
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //if 그룹 있으면 메인 화면(to do list)으로 이동
                                //else 그룹 선택 화면으로 이동
                                Toast.makeText(getApplicationContext(), "로그인 되었습니다.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                AlertDialog alert = alert_confirm.create();
                                alert.show();
                            }
                        }
                    });
        }
        else{
            AlertDialog alert = alert_confirm.create();
            alert.show();
        };
    }

    private void setState(boolean state){
        LIstate = state;
    }
}
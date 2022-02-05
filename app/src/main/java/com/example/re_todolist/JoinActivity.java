package com.example.re_todolist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity {

    Button button;
    TextInputLayout Id_layout, Pw_layout, rePw_layout;
    EditText Id, Pw, rePw;
    String email, password, re_password;
    boolean IDstate, PWstate;

    myDBhelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Id_layout = findViewById(R.id.ID);
        Id = Id_layout.getEditText();
        Pw_layout = findViewById(R.id.PW);
        Pw = Pw_layout.getEditText();
        rePw_layout = findViewById(R.id.re_PW);
        rePw = rePw_layout.getEditText();
        button = findViewById(R.id.create);
        IDstate = false;      //true일 때 사용 가능한 이메일
        PWstate = false;      //true일 때 비밀번호=재입력 비밀번호

        Id.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력난에 변화가 있을 시 조치
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때 조치
                email = Id.getText().toString();
                checkEmail();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        rePw.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력난에 변화가 있을 시 조치
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때 조치
                password = Pw.getText().toString();
                re_password = rePw.getText().toString();
                checkPW();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage("입력 정보를 다시 확인해주세요.");
        alert_confirm.setPositiveButton("확인", null);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IDstate && PWstate) {
                    //db에 데이터 전달하기
                    //if(myDB.insertData(email, password)) {
                        String msg = "아이디 "+email+" 비밀번호 "+password;
                        Toast.makeText(getApplicationContext(), msg,
                                Toast.LENGTH_LONG).show();
                    //}
                    /*else{
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                    }*/
                }
                else{
                    AlertDialog alert = alert_confirm.create();
                    alert.show();

                }
            }
        });
    }

    private void checkEmail() {
        Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
        if (pattern.matcher(email).matches()) {
            //if (myDB.checkID())
            Id_layout.setError(null);
            Id_layout.setHelperText("사용 가능한 ID 입니다.");
            setIdState(true);
            //else
            //Id_layout.setError("이미 사용중인 아이디입니다."); return false;
        } else {
            Id_layout.setHelperText(null);
            Id_layout.setError("이메일 형식에 맞지 않는 ID입니다.");
            setIdState(false);
        }
    }

    private void checkPW() {
        if (password.equals(re_password)) {
            rePw_layout.setError(null);
            setPwState(true);
        } else {
            rePw_layout.setError("비밀번호가 일치하지 않습니다.");
            setPwState(false);
        }
    }

    void setIdState(Boolean state){
        IDstate=state;
    }
    void setPwState(Boolean state){
        PWstate=state;
    }
}

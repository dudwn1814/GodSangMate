package com.example.re_todolist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class NicknameActivity extends AppCompatActivity {

    Button button;
    TextInputLayout nn_layout;
    EditText nn;
    String nickname;
    boolean nnstate;
    AlertDialog.Builder alert_confirm;

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        nn_layout = findViewById(R.id.nickname);
        nn = nn_layout.getEditText();
        button = findViewById(R.id.b_start);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");

        nn.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력난에 변화가 있을 시 조치
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때 조치
                nickname = nn.getText().toString();
                checkNickname();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage("입력정보를 다시 확인해주세요.");
        alert_confirm.setPositiveButton("확인", null);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nnstate) { 
                    //register(email,password);
                }
                else{
                    AlertDialog alert = alert_confirm.create();
                    alert.show();

                }
            }
        });
    }

    private void checkNickname() {
        if(nickname.length() <= 8) {
        /*mDbRef.child("UserAccount").orderByChild("emailID").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                if(value != null){
                    nn_layout.setHelperText(null);
                    nn_layout.setError("이미 사용중인 닉네임입니다.");
                    setnnState(false);
                }
                else{*/
                    nn_layout.setError(null);
                    nn_layout.setHelperText("사용 가능한 닉네임입니다.");
                    setnnState(true);
                }
        /*
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }*/
        else{
            nn_layout.setHelperText(null);
            nn_layout.setError("8자 이내로 입력해주세요.");
            setnnState(false);
        }
    }

    void setnnState(Boolean state){
        nnstate=state;
    }
/*
    private void register(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    UserAccount account = new UserAccount();
                    account.setUid(firebaseUser.getUid());
                    account.setEmailID(firebaseUser.getEmail());
                    account.setPassword(password);

                    //db에 insert
                    mDbRef.child("UserAcount").child(account.getUid()).setValue(account);

                    String msg = "아이디 "+email+" 비밀번호 "+password;
                    Toast.makeText(getApplicationContext(), "가입되었습니다.",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            }
        });
    }
    */
}

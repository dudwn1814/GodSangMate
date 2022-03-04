package com.example.re_todolist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.LinkedHashMap;

public class LoginActivity extends AppCompatActivity {
    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;
    private AlertDialog.Builder alert_confirm;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference mDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        //firebaseAuth.signOut();

        id = (EditText) findViewById(R.id.loginActivity_edittext_id);
        password = (EditText) findViewById(R.id.loginActivity_edittext_password);
        login = (Button) findViewById(R.id.login);
        signup = (Button) findViewById(R.id.signup);
        alert_confirm = new AlertDialog.Builder(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEvent();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, JoinActivity.class));
            }
        });

        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    moveNextPage(user);
                } else {
                    //로그아웃
                }
            }

            ;
        };
    }

    void loginEvent() {
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {

                            //로그인 실패한부분
                            //Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            alert_confirm.setPositiveButton("확인", null);
                            alert_confirm.setMessage("로그인 정보를 확인해주세요.");
                            AlertDialog alert = alert_confirm.create();
                            alert.show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void moveNextPage(FirebaseUser user) {
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");
        String uid = user.getUid();
        mDbRef.child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //그룹 없으면 그룹선택
                if(dataSnapshot.child("g_code").getValue() == null){
                    Intent intent = new Intent(LoginActivity.this, Groupmenu.class);
                    startActivity(intent);
                    finish();
                }
                //닉네임 없으면 닉네임 입력
                else if(dataSnapshot.child("nickname").getValue() == null){
                    UserAccount userAccount = dataSnapshot.getValue(UserAccount.class);
                    String groupCode = userAccount.getG_code();
                    Intent intent = new Intent(LoginActivity.this, NicknameActivity.class);
                    intent.putExtra("g_code", groupCode);
                    startActivity(intent);
                    finish();
                }
                //메인으로 이동
                else{
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}

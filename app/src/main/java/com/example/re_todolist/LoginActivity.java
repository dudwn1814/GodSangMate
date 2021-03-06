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

        getSupportActionBar().hide();

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
                if (id.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
                    alert_confirm.setMessage("?????? ????????? ?????? ??????????????????.");
                    alert_confirm.setPositiveButton("??????", null);
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                } else loginEvent();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, JoinActivity.class));
            }
        });

        //????????? ??????????????? ?????????
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    moveNextPage(user);
                } else {
                    //????????????
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
                            //????????? ???????????????
                            //Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            alert_confirm.setPositiveButton("??????", null);
                            alert_confirm.setMessage("????????? ????????? ??????????????????.");
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
                if (dataSnapshot.child("emailID").getValue() == null) {
                }
                //?????? ????????? ????????????
                else if (dataSnapshot.child("g_code").getValue() == null) {
                    Intent intent = new Intent(LoginActivity.this, Groupmenu.class);
                    startActivity(intent);
                    finish();
                }
                //????????? ????????? ????????? ??????
                else if (dataSnapshot.child("nickname").getValue() == null) {
                    UserAccount userAccount = dataSnapshot.getValue(UserAccount.class);
                    String groupCode = userAccount.getG_code();
                    Intent intent = new Intent(LoginActivity.this, NicknameActivity.class);
                    intent.putExtra("g_code", groupCode);
                    startActivity(intent);
                    finish();
                }
                //???????????? ??????
                else {
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

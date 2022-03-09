package com.example.re_todolist;

import android.content.Intent;
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

public class JoinActivity extends AppCompatActivity {

    Button button;
    TextInputLayout Id_layout, Pw_layout, rePw_layout;
    EditText Id, Pw, rePw;
    String email, password, re_password;
    boolean IDstate, PWstate, rePWstate;
    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    AlertDialog.Builder alert_confirm;

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
        PWstate = false;      //true일 때 비밀번호 6자리 이상
        rePWstate = false;      //true일 때 비밀번호=재입력 비밀번호

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");

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

        Pw.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력난에 변화가 있을 시 조치
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때 조치
                password = Pw.getText().toString();
                checkPW();
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
                recheckPW();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage("입력 정보를 다시 확인해주세요.");
        alert_confirm.setPositiveButton("확인", null);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IDstate && PWstate && rePWstate) {
                    register(email, password);
                } else {
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            }
        });
    }

    private void checkEmail() {
        Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
        if (pattern.matcher(email).matches()) {
            mDbRef.child("UserAccount").orderByChild("emailID").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserAccount value = snapshot.getValue(UserAccount.class);
                            if (value != null) {
                                Id_layout.setHelperText(null);
                                Id_layout.setError("이미 사용중인 아이디입니다.");
                                setIdState(false);
                            } else {
                                Id_layout.setError(null);
                                Id_layout.setHelperText("사용 가능한 ID 입니다.");
                                setIdState(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            Id_layout.setHelperText(null);
            Id_layout.setError("이메일 형식에 맞지 않는 ID입니다.");
            setIdState(false);
        }
    }

    private void checkPW() {
        if (password.length() >= 6) {
            Pw_layout.setError(null);
            setPwState(true);
        } else {
            Pw_layout.setError("비밀번호는 6자 이상이어야 합니다.");
            setPwState(false);
        }
    }

    private void recheckPW() {
        if (password.equals(re_password)) {
            rePw_layout.setError(null);
            setREPwState(true);
        } else {
            rePw_layout.setError("비밀번호가 일치하지 않습니다.");
            setREPwState(false);
        }
    }

    void setIdState(Boolean state) {
        IDstate = state;
    }

    void setPwState(Boolean state) {
        PWstate = state;
    }

    void setREPwState(Boolean state) {
        rePWstate = state;
    }

    private void register(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    UserAccount account = new UserAccount();
                    account.setUid(firebaseUser.getUid());
                    account.setEmailID(firebaseUser.getEmail());
                    account.setPassword(password);

                    //db에 insert
                    mDbRef.child("UserAccount").child(account.getUid()).setValue(account);

                    Toast.makeText(getApplicationContext(), "가입되었습니다.",
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            }
        });
    }
}

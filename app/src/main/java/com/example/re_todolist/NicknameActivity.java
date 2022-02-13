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

public class NicknameActivity extends AppCompatActivity {

    Button button;
    TextInputLayout nn_layout;
    EditText nn;
    String nickname, group_code, group_name;
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

        Intent intent = getIntent();
        group_name = intent.getExtras().getString("name");
        group_code = intent.getExtras().getString("g_code");

        //group_code = "2KRZOZ";

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
                nickname = nn.getText().toString().trim();
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
                    register(nickname);
                }
                else{
                    AlertDialog alert = alert_confirm.create();
                    alert.show();

                }
            }
        });
    }

    private void checkNickname() {
        if(nickname.length()==0){
            nn_layout.setHelperText(null);
            nn_layout.setError(null);
            setnnState(false);
        }
        /*else if (nickname.contains(" ")){
            nn_layout.setHelperText(null);
            nn_layout.setError("사용할 수 없는 닉네임입니다.");
            setnnState(false);
        }*/
        else {
            if (nickname.length() > 8) {
                nn_layout.setHelperText(null);
                nn_layout.setError("8자 이내로 입력해주세요.");
                setnnState(false);
            }
            else {
                mDbRef.child("GroupMember").child(group_code).orderByChild("nickname").equalTo(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupMember value = snapshot.getValue(GroupMember.class);
                        if (value != null) {
                            nn_layout.setHelperText(null);
                            nn_layout.setError("이미 사용중인 닉네임입니다.");
                            setnnState(false);
                        } else {
                            nn_layout.setError(null);
                            nn_layout.setHelperText("사용 가능한 닉네임입니다.");
                            setnnState(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }
    }

    void setnnState(Boolean state){
        nnstate=state;
    }

    private void register(String nickname){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String uid = firebaseUser.getUid();

        /* 그룹 최종 생성 단계 */
        /*
        GroupInfo group = new GroupInfo();
        group.setG_code(group_code);
        group.setName(group_name);
        mDbRef.child("GroupList").child(group.getG_code()).setValue(group); */

        /* UserAccount에 groupInfo 저장 */
        //mDbRef.child("UserAccount").child(uid).child("g_code").setValue(group_code);
        mDbRef.child("UserAccount").child(uid).child("nickname").setValue(nickname);


        /* 그룹 단위 멤버 uid-닉네임 저장 */
        //GroupMember member = new GroupMember();
        //member.setUid(uid);
        //member.setUid("user1");
        //member.setNickname(nickname);
        mDbRef.child("GroupMember").child(group_code).child(uid).child("nickname").setValue(nickname);
        //mDbRef.child("GroupMember").child(group_code).child("user1").setValue(member);
    }
}

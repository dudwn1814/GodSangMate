package com.example.re_todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class PartGroupActivity extends AppCompatActivity {

    Button button;
    TextInputLayout gCode_layout;
    EditText gCode;
    String groupCode;

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    AlertDialog.Builder alert_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partgroup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gCode_layout = findViewById(R.id.gcode);
        gCode = gCode_layout.getEditText();
        button = findViewById(R.id.b_next);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");

        alert_confirm = new AlertDialog.Builder(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupCode = gCode.getText().toString();
                if(groupCode.length() == 0){
                    alert_confirm.setPositiveButton("확인", null);
                    alert_confirm.setMessage("참가 그룹의 코드를 입력해주세요.");
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
                else{
                    validation(groupCode);
                }
            }
        });
    }

    private void validation( String code ){
        mDbRef.child("GroupList").orderByChild("g_code").equalTo(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GroupInfo value = snapshot.getValue(GroupInfo.class);
                if(value != null){
                    String name = snapshot.child(code).child("name").getValue().toString();
                    alert_confirm.setMessage("\'"+name+"\' 그룹에 참가하시겠습니까?");
                    alert_confirm.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //userAccount에 groupcode 저장
                            Intent intent = new Intent(getApplicationContext(), com.example.re_todolist.NicknameActivity.class);
                            startActivity(intent);
                        }
                    });

                alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
            }
                else{
                    alert_confirm.setPositiveButton("확인", null);
                    alert_confirm.setMessage("해당 그룹을 찾을 수 없습니다.");
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}


package com.example.re_todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class GroupName extends AppCompatActivity {

    EditText nameInput;
    static String groupcode, groupname;
    AlertDialog.Builder alert_confirm;
    FirebaseAuth mAuth;
    DatabaseReference mDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_name);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");

        alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage("그룹 이름을 입력해주세요.");
        alert_confirm.setPositiveButton("확인", null);

        Button codeButton = findViewById(R.id.codeButton);
        codeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                nameInput = findViewById(R.id.nameInput);
                groupname = nameInput.getText().toString();

                if (groupname.trim().length() == 0) {
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                } else {
                    createCode();
                    validation();
                }
            }
        });
    }

    public void createCode() {
        int codeLen = 6;
        final char[] characterTable = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

        Random random = new Random(System.currentTimeMillis());
        int table_len = characterTable.length;
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < codeLen; i++) {
            buf.append(characterTable[random.nextInt(table_len)]);
        }
        groupcode = buf.toString();
    }

    public void validation() {
        //mDbRef.child("GroupList").orderByChild("g_code").equalTo(groupcode).addListenerForSingleValueEvent(new ValueEventListener() {
        mDbRef.child("GroupMember").orderByChild("g_code").equalTo(groupcode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GroupInfo value = snapshot.getValue(GroupInfo.class);
                if (value != null) {
                    createCode();
                    validation();
                } else {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String uid = firebaseUser.getUid();

                    /* 그룹 최종 생성 단계 */
                    GroupInfo group = new GroupInfo();
                    group.setG_code(groupcode);
                    group.setName(groupname);
                    mDbRef.child("GroupList").child(group.getG_code()).setValue(group);

                    /* UserAccount에 groupInfo 저장 */
                    mDbRef.child("UserAccount").child(uid).child("g_code").setValue(groupcode);

                    /* GroupMember에 uid 저장 */
                    mDbRef.child("GroupMember").child(groupcode).child(uid).child("uid").setValue(uid);

                    Intent intent = new Intent(GroupName.this, GroupNum.class);
                    EditText nameInput = (EditText) findViewById(R.id.nameInput);
                    intent.putExtra("group_name", groupname);
                    intent.putExtra("group_code", groupcode);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}

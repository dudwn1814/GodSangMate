package com.example.re_todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class GroupNum extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_num);
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        TextView Group_name = (TextView) findViewById(R.id.group_name);
        TextView Group_code = (TextView) findViewById(R.id.group_code);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");


        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        String groupName = bundle.getString("group_name");
        String groupCode = bundle.getString("group_code");


        Group_name.setText(groupName + "의");
        Group_code.setText(groupCode);


        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        UsersGroup account = new UsersGroup();
        account.setUid(firebaseUser.getUid());
        account.setG_name(groupName);
        account.setG_code(groupCode);

        //db에 insert
        mDbRef.child("UsersGroup").child(firebaseUser.getUid()).setValue(account);


        Button backButton = (Button) findViewById(R.id.button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(GroupNum.this, NicknameActivity.class);
                //intent.putExtra("name", groupName);
                intent.putExtra("g_code", groupCode);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override public void onBackPressed() {
        //super.onBackPressed();
    }
}


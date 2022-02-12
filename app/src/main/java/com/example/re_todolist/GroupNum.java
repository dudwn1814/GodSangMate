package com.example.re_todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class GroupNum extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_num);
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");

        TextView Group_name = (TextView) findViewById(R.id.group_name);
        TextView Group_code = (TextView) findViewById(R.id.group_code);

        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        String groupname = bundle.getString("group_name");
        String groupcode = bundle.getString("group_code");

        Group_name.setText(groupname + "의");
        Group_code.setText(groupcode + "입니다.");

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        UserAccount account = new UserAccount();
        account.setUid(firebaseUser.getUid());
        account.setEmailID(firebaseUser.getEmail());
        account.setG_name(groupname);
        account.setG_code(groupcode);

        //db에 insert
        mDbRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);

        Button backButton = (Button) findViewById(R.id.button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("message", "result message is OK!");

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}

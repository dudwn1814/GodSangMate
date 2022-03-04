package com.example.re_todolist;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadingActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDbRef;
    FirebaseUser user = firebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");
        firebaseAuth = FirebaseAuth.getInstance();
        String uid = user.getUid();
        mDbRef.child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                    finish();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

        Intent intent = getIntent();
        int achieve = intent.getExtras().getInt("achieve");

    }

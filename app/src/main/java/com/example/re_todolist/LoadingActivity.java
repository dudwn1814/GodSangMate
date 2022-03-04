package com.example.re_todolist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user==null)  startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
        else {
            mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");
            firebaseAuth = FirebaseAuth.getInstance();
            String uid = user.getUid();

            mDbRef.child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        UserAccount userA = dataSnapshot.getValue(UserAccount.class);
                        String groupCode = userA.getG_code();
                        mDbRef.child("GroupList").child(groupCode).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                GroupInfo group = snapshot.getValue(GroupInfo.class);
                                String groupName = group.getName();

                                mDbRef.child("GroupMember").child(groupCode).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        long person = snapshot.getChildrenCount();

                                        Log.e("test", groupCode + " " + groupName + " " + person);
                                    /*
                                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                                    intent.putExtra("groupCode", groupCode);
                                    intent.putExtra("groupName", groupName);
                                    intent.putExtra("memberCount", person);
                                    startActivity(intent);
                                    finish();

                                     */
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.v("TAG", "loadPost:onCancelled", error.toException());
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
}

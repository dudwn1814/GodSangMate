package com.example.re_todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class PopupActivity extends Activity {

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    String groupCode, groupName, uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        //uid = "user1";
        //groupCode = "ABC123";

        Date dDate = new Date();
        dDate = new Date(dDate.getTime() + (1000 * 60 * 60 * 24 * -1));
        SimpleDateFormat dSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String yesterday = dSdf.format(dDate);


        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_next);

  /*      //UI 객체생성
        txtText = (TextView)findViewById(R.id.txtText);
*/
        //데이터 가져오기
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        Toast.makeText(getApplicationContext(), data + "가져옴", Toast.LENGTH_SHORT).show();


        ArrayList<String> yesterday_tdid = new ArrayList<>();
        ArrayList<String> notDone_td = new ArrayList<>();

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();

                mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(yesterday).child("Personal").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        yesterday_tdid.clear();
                        notDone_td.clear();

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            ToDoPrac todo = postSnapshot.getValue(ToDoPrac.class);

                            if (todo != null && !todo.isDone()) {
                                //오늘 tdid임
                                yesterday_tdid.add(todo.getTdid());
                                notDone_td.add(todo.getActivity());
                            }
                        }
                        //recyclerview 연결
                        RecyclerView recyclerView = findViewById(R.id.recyclerView_yesterday);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerView.setAdapter(new RecyclerAdapter(yesterday_tdid, notDone_td));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.v("TAG", "loadPost:onCancelled", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "그룹명 가져오기 오류",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //확인 버튼 클릭
    public void mOnClose(View v) {

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String writeDate = format.format(date);

        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "OK BUTTON");
        setResult(RESULT_OK, intent);


        //확인버튼 클릭하면 일단 전날 안한거 전부다 오늘 디비에 집어넣음
        Date dDate = new Date();
        dDate = new Date(dDate.getTime() + (1000 * 60 * 60 * 24 * -1));
        SimpleDateFormat dSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String yesterday = dSdf.format(dDate);

        ArrayList<String> yesterday_tdid = new ArrayList<>();
        ArrayList<String> notDone_td = new ArrayList<>();
        mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(yesterday).child("Personal").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                yesterday_tdid.clear();
                notDone_td.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ToDoPrac todo = postSnapshot.getValue(ToDoPrac.class);

                    if (todo != null && !todo.isDone()) {
                        // TODO: 2022-03-04 반복은 안넘기게, 체크한거만 넘기게
                        yesterday_tdid.add(todo.getTdid());
                        notDone_td.add(todo.getActivity());

                        mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(yesterday).child("Personal").child(uid).child(todo.getTdid()).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.v("CEHCKHCKEH", todo.getTdid() + " " + task.getResult().getValue());
                                mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).child(todo.getTdid()).setValue(task.getResult().getValue());
                            } else {

                            }
                        });
                    }
                }
                //recyclerview 연결
                RecyclerView recyclerView = findViewById(R.id.recyclerView_yesterday);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(new RecyclerAdapter(yesterday_tdid, notDone_td));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.v("TAG", "loadPost:onCancelled", error.toException());
            }
        });


        //액티비티(팝업) 닫기
        finish();
    }

    //취소 버튼 클릭
    public void mOnCancelClose(View v) {
        finish();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}

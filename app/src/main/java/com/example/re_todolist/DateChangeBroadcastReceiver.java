package com.example.re_todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateChangeBroadcastReceiver extends BroadcastReceiver {
    DatabaseReference mDbRef;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("test","receiver");
        Toast.makeText(context, "리시버", Toast.LENGTH_SHORT).show();
        mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        Date dDate = new Date();
        dDate = new Date(dDate.getTime() + (1000 * 60 * 60 * 24 * -1));
        SimpleDateFormat dSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String today = format.format(date);
        String yesterday = dSdf.format(dDate);
        Calendar alcalendar = Calendar.getInstance();
        alcalendar.add(Calendar.DATE, 1);
        SimpleDateFormat alformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nextalarm = alformat.format(alcalendar);


        mDbRef.child("ToDoList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot group : snapshot.getChildren()) {
                    String groupCode = group.getKey();
                    if (groupCode != null) {
                        for (DataSnapshot groupToDo : group.child(yesterday).child("Group").getChildren()) {
                            if (groupToDo != null) {
                                if (groupToDo.child("repeat").getValue(Boolean.class).equals(Boolean.TRUE)) {
                                    ToDoPrac todo = new ToDoPrac();
                                    AlarmPrac alarm = new AlarmPrac();

                                    todo.setTdid(groupToDo.getKey());
                                    todo.setActivity(groupToDo.child("activity").getValue(String.class));
                                    todo.setUid(groupToDo.child("uid").getValue(String.class));
                                    todo.setRepeat(true);
                                    todo.setAlarm(groupToDo.child("alarm").getValue(Boolean.class));

                                    alarm.setTdid(groupToDo.getKey());
                                    alarm.setActivity(groupToDo.child("activity").getValue(String.class));
                                    if (todo.isAlarm()) {
                                        todo.setTime(groupToDo.child("time").getValue(String.class));
                                        alarm.setAlarm_time(nextalarm);
                                    }
                                    mDbRef.child("ToDoList").child(groupCode).child(today).child("Group").child(todo.getTdid()).setValue(todo);
                                }
                            }
                        }
                        for (DataSnapshot uidSnapshot : group.child(yesterday).child("Personal").getChildren()) {
                            String uid = uidSnapshot.getKey();
                            for (DataSnapshot personalToDo : uidSnapshot.getChildren()) {
                                ToDoPrac todo = personalToDo.getValue(ToDoPrac.class);
                                todo.setDone(false);
                                if (todo != null) {
                                    if (todo.isRepeat())
                                        mDbRef.child("ToDoList").child(groupCode).child(today).child("Personal").child(uid).child(todo.getTdid()).setValue(todo);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
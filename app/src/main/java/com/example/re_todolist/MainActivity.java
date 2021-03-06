package com.example.re_todolist;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CircleProgressBar.ProgressFormatter {

    private static final String DEFAULT_PATTERN = "%d%%";
    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    String groupCode, groupName, uid, writeDate;
    AlertDialog.Builder alert_confirm;
    Calendar calendar;
    int achieve_g, achieve_p, achieve_gp, achieve, intentID;
    CircleProgressBar circleProgressBar_group, circleProgressBar_personal;
    TextView groupAchieveInfo, personalAchieveInfo;
    LinearLayout achievementLayer;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();
        alert_confirm = new AlertDialog.Builder(this);


        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        writeDate = format.format(date);

        //setting toolbar
        toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.invitation:
                        String msg = "[???????????????] '" + groupName + "' ????????? ???????????????:) \n?????? ?????? : " + groupCode;
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, msg);
                        startActivity(Intent.createChooser(intent, "Group Invitation"));
                        return true;
                    case R.id.groupExit:
                        alert_confirm.setMessage("'" + groupName + "' ????????? ?????????????????????????");
                        alert_confirm.setPositiveButton("??????", (dialog, which) -> groupExit());

                        alert_confirm.setNegativeButton("??????", (dialog, which) -> {
                        });
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                        return true;

                    case R.id.logOut:
                        alert_confirm.setMessage("???????????? ???????????????????");
                        alert_confirm.setPositiveButton("??????", (dialog, which) -> {
                            mAuth.signOut();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        });
                        alert_confirm.setNegativeButton("??????", (dialog, which) -> {
                        });
                        AlertDialog alertView = alert_confirm.create();
                        alertView.show();
                        return true;
                }
                return false;
            }
        });

        //?????? ??????, ?????? ??? ????????????
        getGroupDatafromDB();
        //?????? ?????? ????????????
        getRepeatTodo();
        //?????? ????????? ????????????(?????? ????????? ??????)
        BroadcastReceiver br = new DateChangeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        this.registerReceiver(br, filter);


        setAlarm();

        //?????? ?????? ??????
        // TODO: 2022-03-07 ??????????????? ?????? 24????????????..
        CountDownTimer countDownTimer = new CountDownTimer(3600000 * 24, 1000) {
            @Override
            public void onTick(long l) {
                SimpleDateFormat countDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
                Date countDate = new Date();

                String nowDate = countDateFormat.format(countDate);
                //Log.d("TimeCheck", nowDate);
                String endDate = "24:00:00";
                //Log.d("TimeCheck", endDate);

                try {
                    Date inputDate = countDateFormat.parse(nowDate);
                    Date currDate = countDateFormat.parse(endDate);

                    long diffSec = (currDate.getTime() - inputDate.getTime()) / 1000;
                    long countHour = diffSec / 3600;
                    long countMin = (diffSec - (3600 * countHour)) / 60;
                    long countSec = (diffSec - (3600 * countHour)) - (60 * countMin);

                    TextView countTime = findViewById(R.id.countTime);
                    countTime.setText("??????????????? " + countHour + ":" + countMin + ":" + countSec + " ???????????????");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();

        //todo ?????? ???????????? ??????
        ImageButton fab = findViewById(R.id.writeBtn);

        fab.setOnClickListener(view ->

        {
            Intent todoWriteIntent = new Intent(MainActivity.this, CreateToDoActivity.class);
            MainActivity.this.startActivity(todoWriteIntent);
        });

        //?????????
        circleProgressBar_group = findViewById(R.id.circle_bar_group);
        circleProgressBar_personal = findViewById(R.id.circle_bar_personal);
        groupAchieveInfo = findViewById(R.id.groupAchieveInfo);
        personalAchieveInfo = findViewById(R.id.personalAchieveInfo);


        computeAchieveG();
        computeAchieveP();
        computeAchieveGP();


        //????????? ????????????
        ImageButton b_share = findViewById(R.id.imageButton);
        achievementLayer = findViewById(R.id.achievementLayer);

        b_share.setOnClickListener(view ->
        {
            computeAchieve();
        });


        //RecyclerView
        List<ExpandableListAdapter.Item> data = new ArrayList<>();
        List<ExpandableListAdapter.Item> todo_personal = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        ArrayList<String> dbKey = new ArrayList<>();
        ArrayList<String> dbGroupKey = new ArrayList<>();
        List<ArrayList> personalKey = new ArrayList<>();


        ExpandableListAdapter.Item group_todo = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "??????");

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();

                LinkedHashMap<String, String> memberInfo = new LinkedHashMap<>();

                memberInfo.put(user.getUid(), user.getNickname());

                mDbRef.child("gsmate").child("GroupMember").child(groupCode).orderByChild("nickname").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot d_snapshot : snapshot.getChildren()) {
                            GroupMember member = d_snapshot.getValue(GroupMember.class);
                            if (member != null) {
                                String m_Uid = member.getUid();
                                String m_nickname = member.getNickname();
                                if (!m_Uid.equals(uid)) memberInfo.put(m_Uid, m_nickname);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dbKey.clear();
                        dbGroupKey.clear();
                        personalKey.clear();
                        todo_personal.clear();
                        data.clear();

                        dbGroupKey.add(0, "GROUP");
                        group_todo.invisibleChildren = new ArrayList<>();

                        for (DataSnapshot postSnapshot : snapshot.child("Group").getChildren()) {
                            String key = postSnapshot.getKey();
                            ToDoPrac todo = postSnapshot.getValue(ToDoPrac.class);

                            if (todo != null) {
                                String activity = todo.getActivity();
                                boolean repeat = todo.isRepeat();
                                boolean alarm = todo.isAlarm();
                                String tdid = todo.getTdid();
                                String uid = todo.getUid();
                                boolean done = todo.isDone();
                                //Map<String, Map<String, String>> member = todo.getMember();

                                LinkedHashMap<String, String> member = new LinkedHashMap<>();

                                for (DataSnapshot memberSnapshot : postSnapshot.child("Member").getChildren()) {
                                    GroupMember groupMember = memberSnapshot.getValue(GroupMember.class);
                                    if (groupMember != null) {
                                        String mUid = groupMember.getUid();
                                        String nickname = groupMember.getNickname();
                                        member.put(mUid, nickname);
                                    }
                                }

                                if (alarm) {
                                    String time = todo.getTime();
                                    if (member.size() != 0) {
                                        group_todo.invisibleChildren
                                                .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, time, done, member));
                                    } else {
                                        group_todo.invisibleChildren
                                                .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, time, done));

                                        dbGroupKey.add(key);
                                    }
                                } else {
                                    if (member.size() != 0) {
                                        group_todo.invisibleChildren
                                                .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, done, member));
                                    } else {
                                        group_todo.invisibleChildren
                                                .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, done));
                                    }
                                    dbGroupKey.add(key);
                                }
                            }
                        }

                        for (Map.Entry<String, String> entry : memberInfo.entrySet()) {
                            ArrayList<String> dbPersonalKey = new ArrayList<>();
                            dbPersonalKey.add(0, entry.getValue());
                            ExpandableListAdapter.Item personal_todo = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, entry.getValue());
                            personal_todo.invisibleChildren = new ArrayList<>();
                            String muid = entry.getKey();

                            for (DataSnapshot postSnapshot : snapshot.child("Personal").child(muid).getChildren()) {
                                String key = postSnapshot.getKey();
                                ToDoPrac todo = postSnapshot.getValue(ToDoPrac.class);

                                if (todo != null) {
                                    String activity = todo.getActivity();
                                    boolean repeat = todo.isRepeat();
                                    String tdid = todo.getTdid();
                                    String uid = todo.getUid();
                                    boolean done = todo.isDone();

                                    personal_todo.invisibleChildren
                                            .add(new ExpandableListAdapter.Item(ExpandableListAdapter.PERSONALCHILD, activity, tdid, uid, repeat, done));
                                    dbPersonalKey.add(key);
                                }
                            }
                            personalKey.add(dbPersonalKey);
                            todo_personal.add(personal_todo);
                        }

                        dbKey.addAll(dbGroupKey);
                        data.add(group_todo);

                        data.addAll(group_todo.invisibleChildren);

                        for (ArrayList<String> dbPersonalKey : personalKey) {
                            dbKey.addAll(dbPersonalKey);
                        }
                        for (ExpandableListAdapter.Item personal_todo : todo_personal) {
                            data.add(personal_todo);
                            data.addAll(personal_todo.invisibleChildren);
                        }

                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerView.setAdapter(new ExpandableListAdapter(data, dbKey));
                        //recyclerView.setAdapter(new ExpandableListAdapter(data));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.v("TAG", "loadPost:onCancelled", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(getApplicationContext(), "???????????? ???????????? ??????", Toast.LENGTH_LONG).show();
            }
        });


        //????????? ???????????? ?????? ?????? ?????????
        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);

                groupCode = user.getG_code();

                mDbRef.child("gsmate").child("GroupMember").child(groupCode).child(uid).child("lastVisit").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!String.valueOf(task.getResult().getValue()).equals(writeDate)) {
                            //???????????? ????????? ???????????? ?????? ??????

                            Date dDate = new Date();
                            dDate = new Date(dDate.getTime() + (1000 * 60 * 60 * 24 * -1));
                            SimpleDateFormat dSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                            String yesterday = dSdf.format(dDate);

                            mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    UserAccount user = dataSnapshot.getValue(UserAccount.class);
                                    groupCode = user.getG_code();

                                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(yesterday).child("Personal").child(uid).addValueEventListener(new ValueEventListener() {
                                        int yesterday_done = 0;

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                                ToDoPrac todo = postSnapshot.getValue(ToDoPrac.class);

                                                if (!todo.isRepeat() && !todo.isDone()) {
                                                    yesterday_done++;
                                                    Log.d("yesterday_Todo", yesterday_done + "in func");
                                                }
                                            }

                                            Log.d("yesterday_Todo", yesterday_done + "");
                                            if (yesterday_done <= 0) {
                                                //?????? ????????? ????????? ????????? ?????? ??????
                                                Log.d("yesterday_Todo", "??? ????????? ???????????????, ????????? ????????? ??????");
                                            } else {
                                                Log.d("yesterday_Todo", "???????????? ????????? ?????? ??????" + String.valueOf(task.getResult().getValue()));
                                                Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                                                intent.putExtra("data", "Test Popup");
                                                startActivityForResult(intent, 1);
                                            }
                                            mDbRef.child("gsmate").child("GroupMember").child(groupCode).child(uid).child("lastVisit").setValue(writeDate);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.v("TAG", "loadPost:onCancelled", error.toException());
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    //Toast.makeText(getApplicationContext(), "????????? ???????????? ??????", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            //???????????? ????????? ??????
                            Log.d("yesterday_Todo", "?????? ?????????" + String.valueOf(task.getResult().getValue()));
                        }
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(getApplicationContext(), "???????????? ???????????? ??????", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //????????? ??????
                String result = data.getStringExtra("result");
                //Toast.makeText(getApplicationContext(), result + "??????", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setAlarm() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
            mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserAccount user = snapshot.getValue(UserAccount.class);
                    if (user != null) {
                        groupCode = user.getG_code();

                        if (groupCode != null) {
                            mDbRef.child("gsmate").child("Alarm").child(groupCode).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    groupCode = user.getG_code();
                                    for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                                        AlarmPrac alarm = datasnapshot.getValue(AlarmPrac.class);
                                        if (alarm != null) {
                                            String tdid = alarm.getTdid();
                                            String alarmTimes = alarm.getAlarm_time();
                                            String activity = alarm.getActivity();
                                            boolean repeat = alarm.isRepeat();
                                            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
                                            Date date;
                                            //String str = "2020-03-01 12:00:01";

                                            try {
                                                if (alarmTimes != null) {
                                                    date = sd.parse(alarmTimes);

                                                    Log.d("?????? ??????", "??????" + date);
                                                    Calendar calendar = Calendar.getInstance();
                                                    if (date != null) {
                                                        calendar.setTime(date);
                                                        moveAlarm(tdid, calendar, activity, repeat);
                                                    }

                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void moveAlarm(String tdid, Calendar calendar, String activity, Boolean repeat) {
        PackageManager pm = this.getPackageManager();
        if(System.currentTimeMillis()<=calendar.getTimeInMillis()) {
            Log.d("??????", activity);
            ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            alarmIntent.putExtra("activity", activity);
            alarmIntent.putExtra("tdid", tdid);
            // ???????????? ?????? ????????? ???????????????
            if (repeat) {
                alarmIntent.putExtra("repeat", true);
                /*
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }

                 */
            }
            else alarmIntent.putExtra("repeat", false);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, intentID++, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            // ?????? ??? ???????????? ????????? ?????????????????? ??????
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

    }


    private void getGroupDatafromDB() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();

                mDbRef.child("gsmate").child("GroupList").child(groupCode).child("name").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupName = dataSnapshot.getValue(String.class);
                        //TextView title = findViewById(R.id.ToDoTitle);
                        //getSupportActionBar().setTitle(groupName);
                        toolbar.setTitle(groupName);
                        //title.setText(groupName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "????????? ???????????? ??????",Toast.LENGTH_LONG).show();
                    }
                });


                //?????? ??? ????????????
                //TextView groupPerson = findViewById(R.id.GroupPerson);

                mDbRef.child("gsmate").child("GroupMember").child(groupCode).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long person = snapshot.getChildrenCount();
                        toolbar.setSubtitle(person + "?????? ????????? ???????????? ????????????");
                        //getSupportActionBar().setSubtitle(person + "?????? ????????? ???????????? ????????????");
                        //groupPerson.setText("(" + person + "?????? ????????? ???????????? ????????????)");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.v("TAG", "loadPost:onCancelled", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(getApplicationContext(), "???????????? ???????????? ??????", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void groupExit() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String writeDate = format.format(date);

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupCode = dataSnapshot.child("g_code").getValue(String.class);
                if (groupCode != null) {
                    /* user??? ????????? to_do ???????????? */
                    //?????? ?????? ??????
                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).setValue(null);
                    //?????? ?????? ??????
                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").orderByChild("uid").equalTo(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                                        ToDoPrac todo = datasnapshot.getValue(ToDoPrac.class);
                                        if (todo != null) {
                                            String tdid = todo.getTdid();
                                            mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").child(tdid).setValue(null);
                                            mDbRef.child("gsmate").child("Alarm").child(groupCode).child(tdid).setValue(null);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                                ToDoPrac todo = datasnapshot.getValue(ToDoPrac.class);
                                if (todo != null) {
                                    String tdid = todo.getTdid();
                                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").child(tdid).child("Member").child(uid).setValue(null);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });


                    /* groupMember ???????????? groupList????????? ????????????*/
                    mDbRef.child("gsmate").child("GroupMember").child(groupCode).child(uid).setValue(null);
                    mDbRef.child("gsmate").child("GroupMember").child(groupCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            GroupMember value = snapshot.getValue(GroupMember.class);
                            if (value == null) {
                                mDbRef.child("gsmate").child("GroupList").child(groupCode).setValue(null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else Log.e("test", "???????????? ???????????? ??????");

                /* user??? ????????????, ?????? ??? user ?????? ?????? */
                mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").setValue(null);
                mDbRef.child("gsmate").child("UserAccount").child(uid).child("nickname").setValue(null);
                mDbRef.child("gsmate").child("UsersGroup").child(uid).setValue(null);


                Intent intent = new Intent(getApplicationContext(), Groupmenu.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }

    //?????? ?????? ????????? ??????
    public void computeAchieveG() {
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupCode = snapshot.getValue(String.class);
                if (groupCode != null) {
                    mDbRef.child("gsmate").child("GroupMember").child(groupCode).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            float memberCount = (float) snapshot.getChildrenCount();

                            mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    float max;        //?????? ?????? ??????
                                    max = (float) snapshot.getChildrenCount();

                                    if (max == 0) achieve_g = 0;
                                    else {
                                        float fullCount = max * memberCount;
                                        float progress = 0;    //????????? ?????????
                                        for (DataSnapshot todoSnapshot : snapshot.getChildren()) {
                                            progress += (int) todoSnapshot.child("Member").getChildrenCount();
                                        }
                                        achieve_g = (int) (progress / fullCount * 100);
                                    }
                                    groupAchieveInfo.setText("?????? ????????? : " + achieve_g + "%");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else Log.e("test", "???????????? ???????????? ??????");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //????????? ?????? ????????? ??????
    public void computeAchieveGP() {
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupCode = snapshot.getValue(String.class);
                if (groupCode != null) {
                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            float max;        //?????? ?????? ??????
                            max = (float) snapshot.getChildrenCount();
                            if (max == 0) achieve_gp = 0;
                            else {
                                float progress = 0;    //????????? ?????????
                                for (DataSnapshot todoSnapshot : snapshot.getChildren()) {
                                    for (DataSnapshot memberSnapshot : todoSnapshot.child("Member").getChildren()) {
                                        GroupMember member = memberSnapshot.getValue(GroupMember.class);
                                        if (uid.equals(member.getUid()))
                                            progress = progress + 1;
                                    }
                                }
                                achieve_gp = (int) (progress / max * 100);
                            }
                            circleProgressBar_group.setProgress(achieve_gp);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else Log.e("test", "???????????? ???????????? ??????");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //?????? ????????? ??????
    public void computeAchieveP() {
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupCode = snapshot.getValue(String.class);
                if (groupCode != null) {
                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            float max = 0;        //????????? ?????? ?????? ??????
                            float progress = 0;   //????????? ????????? ?????? ??????
                            max = (float) snapshot.getChildrenCount();
                            if (max == 0) achieve_p = 0;
                            else {
                                for (DataSnapshot todoSnapshot : snapshot.getChildren()) {
                                    ToDoPrac todo = todoSnapshot.getValue(ToDoPrac.class);
                                    if (todo != null) {
                                        boolean done = todo.isDone();
                                        if (done) progress++;
                                    }
                                }
                                achieve_p = (int) (progress / max * 100);
                            }
                            personalAchieveInfo.setText((int) progress + "/" + (int) max);
                            circleProgressBar_personal.setProgress(achieve_p);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else Log.e("test", "???????????? ???????????? ??????");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //????????? ?????? ????????? ??????
    public void computeAchieve() {
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    String groupCode;

                    groupCode = snapshot.getValue(String.class);
                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot snapshot = task.getResult();
                                float group = (float) snapshot.child("Group").getChildrenCount();
                                float personal = (float) snapshot.child("Personal").child(uid).getChildrenCount();
                                float whole = group + personal;

                                if (whole == 0) achieve = 0;
                                else {
                                    float progress_g = 0;    //????????? ?????? ?????? ???
                                    for (DataSnapshot todoSnapshot : snapshot.child("Group").getChildren()) {
                                        for (DataSnapshot memberSnapshot : todoSnapshot.child("Member").getChildren()) {
                                            GroupMember member = memberSnapshot.getValue(GroupMember.class);
                                            if (uid.equals(member.getUid()))
                                                progress_g = progress_g + 1;
                                        }
                                    }
                                    float progress_p = 0;    //????????? ?????? ?????? ???
                                    for (DataSnapshot todoSnapshot : snapshot.child("Personal").child(uid).getChildren()) {
                                        ToDoPrac todo = todoSnapshot.getValue(ToDoPrac.class);
                                        if (todo.isDone()) progress_p = progress_p + 1;
                                    }
                                    float progress = progress_g + progress_p;
                                    achieve = (int) (progress / whole * 100);
                                }
                                Intent intent = new Intent(MainActivity.this, ShareActivity.class);
                                intent.putExtra("achieve", achieve);
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, achievementLayer, "transition");
                                startActivity(intent, options.toBundle());
                            }
                        }
                    });
                }

            }

        });
    }
/*
    public static void resetAlarm(Context context) {
        AlarmManager resetAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(context, DateChangeBroadcastReceiver.class);
        PendingIntent resetSender = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), resetIntent, 0);
        Toast.makeText(context, System.currentTimeMillis() + "", Toast.LENGTH_SHORT).show();

        // ?????? ??????
        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, 0);
        resetCal.set(Calendar.MINUTE, 0);
        resetCal.set(Calendar.SECOND, 0);

        //????????? 0?????? ????????? ?????? 24????????? ????????? ????????? AlarmManager.INTERVAL_DAY??? ?????????.
        //resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender);
        resetAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender);
    }
*/
    public void getRepeatTodo() {
        mDbRef.child("gsmate").child("LastVisit").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        String lastVisit = snapshot.getValue(String.class);
                        if (!lastVisit.equals(writeDate)) {
                            /*
                            Calendar alcalendar = Calendar.getInstance();
                            alcalendar.add(Calendar.DATE, 1);
                            SimpleDateFormat alformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String nextalarm = alformat.format(alcalendar);
                             */

                            mDbRef.child("gsmate").child("ToDoList").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DataSnapshot snapshot = task.getResult();
                                        for (DataSnapshot group : snapshot.getChildren()) {
                                            String groupCode = group.getKey();
                                            if (groupCode != null) {
                                                if (group.child(lastVisit).getValue() != null) {
                                                    for (DataSnapshot groupToDo : group.child(lastVisit).child("Group").getChildren()) {
                                                        if (groupToDo != null) {
                                                            Log.e("test", groupToDo.toString());
                                                            if (groupToDo.child("repeat").getValue(Boolean.class).equals(Boolean.TRUE)) {
                                                                ToDoPrac todo = new ToDoPrac();
                                                                todo.setTdid(groupToDo.getKey());
                                                                todo.setActivity(groupToDo.child("activity").getValue(String.class));
                                                                todo.setUid(groupToDo.child("uid").getValue(String.class));
                                                                todo.setRepeat(true);
                                                                todo.setAlarm(groupToDo.child("alarm").getValue(Boolean.class));

                                                                if (todo.isAlarm()) {
                                                                    todo.setTime(groupToDo.child("time").getValue(String.class));

                                                                    AlarmPrac alarm = new AlarmPrac();
                                                                    alarm.setTdid(groupToDo.getKey());
                                                                    alarm.setActivity(groupToDo.child("activity").getValue(String.class));
                                                                    //alarm.setAlarm_time(nextalarm);
                                                                    //alarm ????????
                                                                }
                                                                mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").child(todo.getTdid()).setValue(todo);
                                                            }
                                                        }
                                                    }
                                                    for (DataSnapshot uidSnapshot : group.child(lastVisit).child("Personal").getChildren()) {
                                                        String uid = uidSnapshot.getKey();
                                                        for (DataSnapshot personalToDo : uidSnapshot.getChildren()) {
                                                            ToDoPrac todo = personalToDo.getValue(ToDoPrac.class);
                                                            todo.setDone(false);
                                                            if (todo != null) {
                                                                if (todo.isRepeat())
                                                                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).child(todo.getTdid()).setValue(todo);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            });

                            mDbRef.child("gsmate").child("LastVisit").setValue(writeDate);
                        }
                    } else mDbRef.child("gsmate").child("LastVisit").setValue(writeDate);
                }
            }
        });
    }
}


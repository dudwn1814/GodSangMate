package com.example.re_todolist;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    int achieve_g, achieve_p, achieve_gp;
    CircleProgressBar circleProgressBar_group, circleProgressBar_personal;
    TextView groupAchieveInfo, personalAchieveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();

        alert_confirm = new AlertDialog.Builder(this);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        writeDate = format.format(date);

        //그룹 이름, 인원 수 가져오기
        getGroupDatafromDB();

        //알람 울리기 허용
        ImageButton alrm = findViewById(R.id.alarmButton);
        alrm.setOnClickListener(view -> {
            setAlarm();
        });


        //todo 작성 페이지로 이동
        ImageButton fab = findViewById(R.id.writeBtn);

        fab.setOnClickListener(view -> {
            Intent todoWriteIntent = new Intent(MainActivity.this, CreateToDoActivity.class);
            MainActivity.this.startActivity(todoWriteIntent);
        });

        //달성률
        //achieve_g = 50;

        circleProgressBar_group = findViewById(R.id.circlebar_group);
        circleProgressBar_personal = findViewById(R.id.circlebar_personal);
        groupAchieveInfo = findViewById(R.id.groupAchieveInfo);
        personalAchieveInfo = findViewById(R.id.personalAchieveInfo);


        computeAchieveG();
        computeAchieveP();
        computeAchieveGP();

        //circleProgressBar_group.setProgress(achieve_g);
        //circleProgressBar_personal.setProgress(achieve_p);


        //달성률 공유하기
        ImageButton b_share = findViewById(R.id.imageButton);

        b_share.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ShareActivity.class);
            intent.putExtra("achieve", achieve_p);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, circleProgressBar_personal, "transition");
            startActivity(intent, options.toBundle());
        });


        //RecyclerView
        List<ExpandableListAdapter.Item> data = new ArrayList<>();
        List<ExpandableListAdapter.Item> todo_personal = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        ArrayList<String> dbKey = new ArrayList<>();
        ArrayList<String> dbGroupKey = new ArrayList<>();
        List<ArrayList> personalKey = new ArrayList<>();


        ExpandableListAdapter.Item group_todo = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "그룹");

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();

                LinkedHashMap<String, String> memberInfo = new LinkedHashMap<>();
                mDbRef.child("gsmate").child("GroupMember").child(groupCode).orderByChild("nickname").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot d_snapshot : snapshot.getChildren()) {
                            GroupMember member = d_snapshot.getValue(GroupMember.class);
                            if (member != null) {
                                String m_Uid = member.getUid();
                                String m_nickname = member.getNickname();
                                memberInfo.put(m_Uid, m_nickname);
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
                Toast.makeText(getApplicationContext(), "그룹코드 가져오기 오류",
                        Toast.LENGTH_LONG).show();
            }
        });


        //다음날 넘어갈때 못한 투두 넘기기
        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);

                groupCode = user.getG_code();

                mDbRef.child("gsmate").child("GroupMember").child(groupCode).child(uid).child("lastVisit").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!String.valueOf(task.getResult().getValue()).equals(writeDate)) {
                            //방문일이 오늘이 아니거나 없는 경우

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
                                                //전날 미완인 투두가 하나도 없을 경우
                                                Log.d("yesterday_Todo", "다 완료한 투두이거나, 반복인 투두만 있음");
                                            } else {
                                                Log.d("yesterday_Todo", "방문일이 오늘이 아닌 경우" + String.valueOf(task.getResult().getValue()));
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
                                    Toast.makeText(getApplicationContext(), "그룹명 가져오기 오류",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            //방문일이 오늘인 경우
                            Log.d("yesterday_Todo", "오늘 방문함" + String.valueOf(task.getResult().getValue()));
                        }
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "그룹코드 가져오기 오류",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                String result = data.getStringExtra("result");
                Toast.makeText(getApplicationContext(), result + "받음", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setAlarm() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserAccount user = snapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();

                mDbRef.child("gsmate").child("Alarm").child(groupCode).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupCode = user.getG_code();
                        for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                            AlarmPrac alarm = datasnapshot.getValue(AlarmPrac.class);
                            if (alarm != null) {
                                String todo = alarm.getTdid();
                                mDbRef.child("gsmate").child("Alarm").child(groupCode).child(todo).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        AlarmPrac alarm = datasnapshot.getValue(AlarmPrac.class);
                                        String alarmTimes = alarm.getAlarm_time();
                                        String activity = alarm.getActivity();
                                        boolean repeat = alarm.isRepeat();
                                        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
                                        //String str = "2020-03-01 12:00:01";
                                        try {
                                            Date date = sd.parse(alarmTimes);
                                            calendar.setTime(date);
                                            moveAlarm(calendar, activity, repeat);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getApplicationContext(), "알람시간 가져오기 오류",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "그룹코드 내부 리스트 가져오기 오류",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "그룹코드 가져오기 오류",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void moveAlarm(Calendar calendar, String activity, Boolean repeat) {
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean dailyNotify = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DAILY_NOTIFICATION, true);

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmBroadcastReceiver.class);
        alarmIntent.putExtra("activity", activity);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        // 사용자가 매일 알람을 허용했다면
        if (repeat) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Toast.makeText(getApplicationContext(), "반복 알림이 설정됐습니다", Toast.LENGTH_SHORT).show();
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Toast.makeText(getApplicationContext(), "알림이 한번만 울립니다.", Toast.LENGTH_SHORT).show();
        }
        // 부팅 후 실행되는 리시버 사용가능하게 설정
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
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
                        TextView title = findViewById(R.id.ToDoTitle);
                        title.setText(groupName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "그룹명 가져오기 오류",
                                Toast.LENGTH_LONG).show();
                    }
                });


                //인원 수 가져오기
                TextView groupPerson = findViewById(R.id.GroupPerson);

                mDbRef.child("gsmate").child("GroupMember").child(groupCode).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long person = snapshot.getChildrenCount();
                        groupPerson.setText("(" + person + "명의 인원이 참가하고 있습니다)");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.v("TAG", "loadPost:onCancelled", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "그룹코드 가져오기 오류",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invitation:
                String msg = "[갓생메이트] '" + groupName + "' 그룹에 초대합니다:) \n그룹 코드 : " + groupCode;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(Intent.createChooser(intent, "Group Invitation"));
                return true;
            case R.id.groupExit:
                alert_confirm.setMessage("'" + groupName + "' 그룹을 탈퇴하시겠습니까?");
                alert_confirm.setPositiveButton("확인", (dialog, which) -> groupExit());

                alert_confirm.setNegativeButton("취소", (dialog, which) -> {
                });
                AlertDialog alert = alert_confirm.create();
                alert.show();
                return true;

            case R.id.logOut:
                alert_confirm.setMessage("로그아웃 하시겠습니까?");
                alert_confirm.setPositiveButton("확인", (dialog, which) -> {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
                alert_confirm.setNegativeButton("취소", (dialog, which) -> {
                });
                AlertDialog alertView = alert_confirm.create();
                alertView.show();
                return true;
        }
        return false;
    }

    public void groupExit() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String writeDate = format.format(date);

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();
                /* user가 작성한 to_do 삭제하기 */
                //개인 투두 삭제
                mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).setValue(null);
                //그룹 투두 삭제
                mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").orderByChild("uid").equalTo(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                                    ToDoPrac todo = datasnapshot.getValue(ToDoPrac.class);
                                    if (todo != null) {
                                        String tdid = todo.getTdid();
                                        mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").child(tdid).setValue(null);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                /*user의 그룹정보, 그룹 내 user 정보 제거*/
                mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").setValue(null);
                mDbRef.child("gsmate").child("UserAccount").child(uid).child("nickname").setValue(null);
                mDbRef.child("gsmate").child("UsersGroup").child(uid).setValue(null);
                mDbRef.child("gsmate").child("GroupMember").child(groupCode).child(uid).setValue(null);

        /* groupMember 삭제되면 groupList에서도 삭제하기
           =>그룹 멤버 0명일 때, 그룹 정보는 유지하다가 이후 같은 코드를 갖는 새 그룹이 생긴다면 덮어쓰기 */
        /*
        mDbRef.child("gsmate").child("GroupMember").orderByValue().equalTo(groupCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
         */


                Intent intent = new Intent(getApplicationContext(), Groupmenu.class);
                startActivity(intent);
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

    //그룹 달성률 계산
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
                                    float max;        //그룹 투두 전체
                                    max = (float) snapshot.getChildrenCount();

                                    if (max == 0) achieve_g = 0;
                                    else {
                                        float fullCount = max * memberCount;
                                        float progress = 0;    //실행한 인원수
                                        for (DataSnapshot todoSnapshot : snapshot.getChildren()) {
                                            progress += (int) todoSnapshot.child("Member").getChildrenCount();
                                        }
                                        achieve_g = (int) (progress / fullCount * 100);
                                        circleProgressBar_group.setProgress(achieve_g);
                                    }
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
                } else Log.e("test", "그룹코드 받아오기 실패");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //개인의 그룹 달성률 계산
    public void computeAchieveGP() {
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupCode = snapshot.getValue(String.class);
                if (groupCode != null) {
                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Group").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            float max;        //그룹 투두 전체
                            max = (float) snapshot.getChildrenCount();
                            if (max == 0) achieve_gp = 0;
                            else {
                                float progress = 0;    //실행한 인원수
                                for (DataSnapshot todoSnapshot : snapshot.getChildren()) {
                                    for (DataSnapshot memberSnapshot : todoSnapshot.child("Member").getChildren()) {
                                        GroupMember member = memberSnapshot.getValue(GroupMember.class);
                                        if (uid.equals(member.getUid())) progress = progress + 1;
                                    }
                                }
                                achieve_g = (int) (progress / max * 100);
                            }
                            groupAchieveInfo.setText("나의 달성률 : " + achieve_g + "%");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else Log.e("test", "그룹코드 받아오기 실패");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //개인 달성률 계산
    public void computeAchieveP() {
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupCode = snapshot.getValue(String.class);
                if (groupCode != null) {
                    mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            float max = 0;        //유저의 개인 투두 전체
                            float progress = 0;   //유저가 체크한 개인 투두
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
                } else Log.e("test", "그룹코드 받아오기 실패");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
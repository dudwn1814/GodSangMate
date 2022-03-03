package com.example.re_todolist;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.security.acl.Group;
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
    String groupCode, groupName, uid;
    AlertDialog.Builder alert_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();

        alert_confirm = new AlertDialog.Builder(this);
        uid = "user1";
        groupCode = "ABC123";

        //그룹 이름, 인원 수 가져오기
        getGroupDatafromDB();


        //todo 작성 페이지로 이동
        ImageButton fab = findViewById(R.id.writeBtn);

        fab.setOnClickListener(view -> {
            Intent todoWriteIntent = new Intent(MainActivity.this, CreateToDoActivity.class);
            MainActivity.this.startActivity(todoWriteIntent);
        });

        //달성률
        int achieve_g = 50;
        int achieve_p = 70;
        CircleProgressBar circleProgressBar_group = findViewById(R.id.circlebar_group);
        CircleProgressBar circleProgressBar_personal = findViewById(R.id.circlebar_personal);

        circleProgressBar_group.setProgress(achieve_g);
        circleProgressBar_personal.setProgress(achieve_p);


        //달성률 공유하기
        ImageButton b_share = findViewById(R.id.imageButton);

        b_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShareActivity.class);
                intent.putExtra("achieve", achieve_p);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, circleProgressBar_personal, "transition");
                startActivity(intent, options.toBundle());
            }
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
                //UserAccount user = dataSnapshot.getValue(UserAccount.class);
                //groupCode = user.getG_code();

                LinkedHashMap<String, String> memberInfo = new LinkedHashMap<>();
                //mDbRef.child("gsmate").child("GroupMember").child(groupCode).orderByChild("nickname").addValueEventListener(new ValueEventListener() {
                mDbRef.child("gsmate").child("GroupMember").child(groupCode).orderByChild("nickname").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot d_snapshot : snapshot.getChildren()) {
                            GroupMember member = d_snapshot.getValue(GroupMember.class);
                            String m_Uid = member.getUid();
                            String m_nickname = member.getNickname();
                            memberInfo.put(m_Uid, m_nickname);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String writeDate = format.format(currentTime);

                // mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(날짜).addValueEventListener(new ValueEventListener() {
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
                            String activity = todo.getActivity();
                            boolean repeat = todo.isRepeat();
                            boolean alarm = todo.isAlarm();
                            String tdid = todo.getTdid();
                            String uid = todo.getUid();
                            boolean done = todo.isDone();
                            //Map<String, Map<String, String>> member = todo.getMember();

                            LinkedHashMap<String, String> member = new LinkedHashMap<>();

                            for(DataSnapshot memberSnapshot : postSnapshot.child("Member").getChildren()){
                                GroupMember groupMember = memberSnapshot.getValue(GroupMember.class);
                                String mUid = groupMember.getUid();
                                String nickname = groupMember.getNickname();
                                member.put(mUid, nickname);
                            }

                            if (alarm) {
                                String time = todo.getTime();
                                if (member.size() != 0) {
                                    group_todo.invisibleChildren
                                            .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, time, done, member));
                                    dbGroupKey.add(key);
                                } else {
                                    group_todo.invisibleChildren
                                            .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, time, done));
                                    dbGroupKey.add(key);

                                }
                            } else {
                                if (member.size() != 0) {
                                    group_todo.invisibleChildren
                                            .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, done, member));
                                    dbGroupKey.add(key);

                                } else {
                                    group_todo.invisibleChildren
                                            .add(new ExpandableListAdapter.Item(ExpandableListAdapter.GROUPCHILD, activity, tdid, uid, repeat, alarm, done));
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
                                String activity = todo.getActivity();
                                boolean repeat = todo.isRepeat();
                                String tdid = todo.getTdid();
                                String uid = todo.getUid();
                                boolean done = todo.isDone();

                                personal_todo.invisibleChildren
                                        .add(new ExpandableListAdapter.Item(ExpandableListAdapter.PERSONALCHILD, activity, tdid, uid, repeat, done));
                                dbPersonalKey.add(key);
                            }
                            personalKey.add(dbPersonalKey);
                            todo_personal.add(personal_todo);
                        }

                        for (String group_key : dbGroupKey) {
                            dbKey.add(group_key);
                        }
                        data.add(group_todo);

                        for(ExpandableListAdapter.Item i : group_todo.invisibleChildren){
                            data.add(i);
                        }

                        for (ArrayList<String> dbPersonalKey : personalKey) {
                            for (String personal_key : dbPersonalKey) {
                                dbKey.add(personal_key);
                            }
                        }
                        for (ExpandableListAdapter.Item personal_todo : todo_personal) {
                            data.add(personal_todo);
                            for(ExpandableListAdapter.Item i : personal_todo.invisibleChildren){
                                data.add(i);
                            }
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void getGroupDatafromDB() {
        //FirebaseUser firebaseUser = mAuth.getCurrentUser();
        //uid = firebaseUser.getUid();

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                //groupCode = user.getG_code();

                mDbRef.child("gsmate").child("GroupList").child(groupCode).child("name").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);
                        groupName = value;
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
                alert_confirm.setMessage("\'" + groupName + "\' 그룹을 탈퇴하시겠습니까?");
                alert_confirm.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        groupExit();
                    }
                });

                alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alert = alert_confirm.create();
                alert.show();
                return true;
        }
        return false;
    }

    public void groupExit() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String writeDate = format.format(currentTime);

        /* user가 작성한 to_do 삭제하기 */
        //개인 투두 삭제
        mDbRef.child("gsmate").child("ToDoList").child(groupCode).child(writeDate).child("Personal").child(uid).setValue(null);
        //그룹 투두 삭제

        /*user의 그룹정보, 그룹 내 user 정보 제거*/
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").setValue(null);
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("nickname").setValue(null);
        mDbRef.child("gsmate").child("UsersGroup").child(uid).setValue(null);
        mDbRef.child("gsmate").child("GroupMember").child(groupCode).child(uid).setValue(null);

        /* groupMember 삭제되면 groupList에서도 삭제하기  
           =>그룹 멤버 0명일 때, 그룹 정보는 유지하다가 이후 같은 코드를 갖는 새 그룹이 생긴다면 덮어쓰기 */
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


        Intent intent = new Intent(getApplicationContext(), Groupmenu.class);
        startActivity(intent);
    }

    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }
}
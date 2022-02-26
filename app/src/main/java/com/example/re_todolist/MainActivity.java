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

import java.util.ArrayList;
import java.util.List;

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

        //그룹 이름, 인원 수 가져오기
        getGroupDatafromDB();


        //todo 작성 페이지로 이동
        ImageButton fab = findViewById(R.id.writeBtn);

        fab.setOnClickListener(view -> {
            Intent todoWriteIntent = new Intent(MainActivity.this, TodoWriteActivity.class);
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
        //ArrayList<String> arrayList = new ArrayList<>();
        List<ExpandableListAdapter.Item> data = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        ArrayList<String> dbKey = new ArrayList<>();

        ExpandableListAdapter.Item group_todo = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "GROUP");
        ExpandableListAdapter.Item personal_todo = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "PERSONAL");

        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();

                mDbRef.child("TodoList").child(uid).child(groupCode).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //arrayList.clear();
                        dbKey.clear();
                        data.clear();

                        group_todo.invisibleChildren = new ArrayList<>();
                        personal_todo.invisibleChildren = new ArrayList<>();

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            String key = postSnapshot.getKey();
                            dbKey.add(key);
                            String todoString = postSnapshot.child("todo").getValue().toString();

                            if (todoString != null) {
                                if (postSnapshot.child("personal").getValue().toString().equals("그룹")) {
                                    group_todo.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, todoString));
                                } else {
                                    personal_todo.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, todoString));
                                }
                                //arrayList.add(todoString);
                            }
                        }

                        //String todoString = snapshot.getValue(String.class);
                        //Toast.makeText(getApplicationContext(), snapshot.getValue()+"",
                        //        Toast.LENGTH_LONG).show();
                        //if (todoString != null) {
                        //    arrayList.add(todoString);
                        //data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, todoString));
                        //}
                        data.add(group_todo);
                        data.add(personal_todo);

                        //arrayList.add("To Do");
                        //RecyclerAdapter adapter = new RecyclerAdapter(getApplicationContext(), arrayList, recyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerView.setAdapter(new ExpandableListAdapter(data));
                        //recyclerView.setAdapter(adapter);
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


//SWIPE---------------------------------------------------------------------------------------------------------------------
        //recyclerview swipe
        /*ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean isItemViewSwipeEnabled() {
                return super.isItemViewSwipeEnabled();
            }


            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Toast.makeText(getApplicationContext(), "on Move",
                        Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                Log.v("groupCheck", position+"");
                //db 삭제

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                } else {
                    uid = "test";
                    Toast.makeText(getApplicationContext(), "uid를 못불러옴.",
                            Toast.LENGTH_LONG).show();
                }

                //mDbRef.child("TodoList").child(uid).child("group-code").child(dbKey.get(position)).removeValue();
            }
        };*/

        //ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        //itemTouchHelper.attachToRecyclerView(recyclerView);
    }
//----------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void getGroupDatafromDB() {
        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                groupCode = user.getG_code();

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
                alert_confirm.setMessage("\'"+groupName+"\' 그룹을 탈퇴하시겠습니까?");
                alert_confirm.setPositiveButton("확인", new DialogInterface.OnClickListener(){
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

    public void groupExit(){
        //user가 작성한 to_do 삭제하기
        
        /*user의 그룹정보, 그룹 내 user 정보 제거*/
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("g_code").setValue(null);
        mDbRef.child("gsmate").child("UserAccount").child(uid).child("nickname").setValue(null);
        mDbRef.child("gsmate").child("UsersGroup").child(uid).setValue(null);
        mDbRef.child("gsmate").child("GroupMember").child(groupCode).child(uid).setValue(null);

        /* groupMember 삭제되면 groupList에서도 삭제하기 */
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
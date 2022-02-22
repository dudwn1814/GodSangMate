package com.example.re_todolist;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CircleProgressBar.ProgressFormatter {

    private static final String DEFAULT_PATTERN = "%d%%";
    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    String groupCode, groupName, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();

        uid = "user1";

        getDatafromDB();

        TextView groupPerson = findViewById(R.id.GroupPerson);

        mDbRef.child("gsmate").child("GroupMember")
                .child("YJSIVV")
                .addValueEventListener(new ValueEventListener() {
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


        ImageButton fab = findViewById(R.id.writeBtn);
        fab.setOnClickListener(view -> {
            Intent calendarIntent = new Intent(MainActivity.this, TodoWriteActivity.class);
            MainActivity.this.startActivity(calendarIntent);
        });

/*        Button optionBtn = findViewById(R.id.optionBtn);


        optionBtn.setOnClickListener(view -> {
            PopupMenu p = new PopupMenu(getApplicationContext(), view);
            getMenuInflater().inflate(R.menu.menu_main, p.getMenu());

            mDbRef.child("UserAccount").child("cF6jxtem0SaEtCVSdGEVCj6r31R2").child("g_code").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    groupCode = String.valueOf(task.getResult().getValue());
                    p.getMenu().getItem(0).setTitle(groupCode);
                }
            });

            p.setOnMenuItemClickListener(item -> {
                Toast.makeText(getApplicationContext(),
                        "팝업메뉴 이벤트 처리 - "
                                + item.getTitle(),
                        Toast.LENGTH_SHORT).show();
                return false;
            });
            p.show();
        });
 */

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
        ArrayList<String> arrayList = new ArrayList<>();
        List<ExpandableListAdapter.Item> data = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        //FirebaseUser firebaseUser = mAuth.getCurrentUser();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        } else {
            uid = "test";
            Toast.makeText(getApplicationContext(), "uid를 못불러옴.",
                    Toast.LENGTH_LONG).show();
        }

        ArrayList<String> dbKey = new ArrayList<>();
        mDbRef.child("TodoList")
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayList.clear();
                        dbKey.clear();

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            String key = postSnapshot.getKey();
                            dbKey.add(key);
                            String todoString = postSnapshot.child("todo").getValue().toString();
                            Log.v("groupCheck", todoString + "");

                            if (todoString != null) {
                                arrayList.add(todoString);
                                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, todoString));
                            }
                        }

                        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Group"));

                        //String todoString = snapshot.getValue(String.class);
                        //Toast.makeText(getApplicationContext(), snapshot.getValue()+"",
                        //        Toast.LENGTH_LONG).show();
                        //if (todoString != null) {
                        //    arrayList.add(todoString);
                        //data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, todoString));
                        //}

                        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Cars"));
                        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Audi"));
                        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Aston Martin"));
                        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "BMW"));

                        arrayList.add("To Do");
                        RecyclerAdapter adapter = new RecyclerAdapter(getApplicationContext(), arrayList, recyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        //recyclerView.setAdapter(new ExpandableListAdapter(data));
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.v("TAG", "loadPost:onCancelled", error.toException());
                    }
                });

        //recyclerview swipe
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Toast.makeText(getApplicationContext(), "on Move",
                        Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                Log.v("groupCheck", dbKey + "" + position + "");
                //db 삭제

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                } else {
                    uid = "test";
                    Toast.makeText(getApplicationContext(), "uid를 못불러옴.",
                            Toast.LENGTH_LONG).show();
                }

                mDbRef.child("TodoList").child(uid).child(dbKey.get(position)).removeValue();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void getDatafromDB() {
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
                return true;
        }
        return false;
    }


    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }


}
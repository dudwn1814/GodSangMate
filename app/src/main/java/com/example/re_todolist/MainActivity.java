package com.example.re_todolist;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CircleProgressBar.ProgressFormatter {

    private static final String DEFAULT_PATTERN = "%d%%";
    FirebaseAuth mAuth;
    DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference("gsmate");
    String groupCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView groupPerson = findViewById(R.id.GroupPerson);

        mDbRef.child("GroupMember")
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

        Button optionBtn = findViewById(R.id.optionBtn);


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
                startActivity(intent);
            }
        });


        //RecyclerView
        ArrayList<String> arrayList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        //FirebaseUser firebaseUser = mAuth.getCurrentUser();

        mDbRef.child("todoTest")
                .child("cF6jxtem0SaEtCVSdGEVCj6r31R2")
                .child("todo")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayList.clear();

                        String todoString = snapshot.getValue(String.class);
                        if (todoString != null) {
                            arrayList.add(todoString);
                        }

                        RecyclerAdapter adapter = new RecyclerAdapter(getApplicationContext(), arrayList, recyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
                //db 삭제
                mDbRef.child("todoTest").child("cF6jxtem0SaEtCVSdGEVCj6r31R2").removeValue();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 1) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }


}
package com.example.re_todolist;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HEADER = 0;
    public static final int GROUPCHILD = 1;
    public static final int PERSONALCHILD = 2;

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    String groupCode, uid;

    private final List<Item> data;
    private final ArrayList<String> dbkey;

    public ExpandableListAdapter(List<Item> data, ArrayList<String> dbkey) {
        this.data = data;
        this.dbkey = dbkey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view;
        View child_view;
        switch (type) {
            case HEADER:
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_header, parent, false);
                return new ListHeaderViewHolder(view);
            case GROUPCHILD:
            case PERSONALCHILD:
                LayoutInflater child_inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                child_view = child_inflater.inflate(R.layout.todo_prac, parent, false);
                return new ListHeaderViewHolder(child_view);
        }
        return null;
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();

        final Item item = data.get(position);
        final String dbItem = dbkey.get(position);

        switch (item.type) {
            case HEADER:
                final ListHeaderViewHolder itemController = (ListHeaderViewHolder) holder;
                itemController.refferalItem = item;
                itemController.header_title.setText(item.title);
                if (item.invisibleChildren == null) {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.ic_expand_less_black_24dp);
                } else {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.ic_expand_more_black_24dp);
                }
                itemController.btn_expand_toggle.setOnClickListener(v -> {
                    if (item.invisibleChildren == null) {
                        item.invisibleChildren = new ArrayList<>();
                        int count = 0;
                        int pos = data.indexOf(itemController.refferalItem);
                        while (data.size() > pos + 1 && (data.get(pos + 1).type == GROUPCHILD || data.get(pos + 1).type == PERSONALCHILD)) {
                            item.invisibleChildren.add(data.remove(pos + 1));
                            count++;
                        }
                        notifyItemRangeRemoved(pos + 1, count);
                        itemController.btn_expand_toggle.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    } else {
                        int pos = data.indexOf(itemController.refferalItem);
                        int index = pos + 1;
                        for (Item i : item.invisibleChildren) {
                            data.add(index, i);
                            index++;
                        }
                        notifyItemRangeInserted(pos + 1, index - pos - 1);
                        itemController.btn_expand_toggle.setImageResource(R.drawable.ic_expand_less_black_24dp);
                        item.invisibleChildren = null;
                    }
                });
                break;
            case GROUPCHILD:
                final ListHeaderViewHolder childItemController_g = (ListHeaderViewHolder) holder;
                childItemController_g.checkBox.setText(item.activity);

                if(item.repeat) childItemController_g.repeatDay.setVisibility(View.VISIBLE);
                else    childItemController_g.repeatDay.setVisibility(View.GONE);

                if(item.alarm){
                    childItemController_g.alarmIcon.setVisibility(View.VISIBLE);
                    //childItemController.alarmTime.setText(item.time);
                }
                else{
                    childItemController_g.alarmIcon.setVisibility(View.INVISIBLE);
                }

                if(item.member != null){
                    //childItemController.todoPerson.setText(인원수 카운트);
                    childItemController_g.todoPerson.setVisibility(View.VISIBLE);
                }
                else{
                    childItemController_g.todoPerson.setVisibility(View.VISIBLE);
                }

                childItemController_g.checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new MaterialAlertDialogBuilder(view.getContext())
                                .setTitle(item.activity)
                                .setMessage(dbItem)
                                .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(view.getContext(), "취소",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(view.getContext(), "삭제",
                                                Toast.LENGTH_SHORT).show();

                                        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                                                groupCode = user.getG_code();

                                                mDbRef.child("TodoList").child(uid).child(groupCode).child(dbItem).removeValue();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(view.getContext(), "그룹코드 가져오기 오류",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                })
                                .show();
                        return false;
                    }
                });
                break;
            case PERSONALCHILD:
                final ListHeaderViewHolder childItemController_p = (ListHeaderViewHolder) holder;

                childItemController_p.checkBox.setText(item.activity);
                childItemController_p.alarmIcon.setVisibility(View.GONE);
                childItemController_p.alarmTime.setVisibility(View.GONE);
                childItemController_p.todoPerson.setVisibility(View.GONE);
                if(item.repeat) childItemController_p.repeatDay.setVisibility(View.VISIBLE);
                else    childItemController_p.repeatDay.setVisibility(View.GONE);

                childItemController_p.checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new MaterialAlertDialogBuilder(view.getContext())
                                .setTitle(item.activity)
                                .setMessage(dbItem)
                                .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(view.getContext(), "취소",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(view.getContext(), "삭제",
                                                Toast.LENGTH_SHORT).show();

                                        mDbRef.child("gsmate").child("UserAccount").child(uid).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                UserAccount user = dataSnapshot.getValue(UserAccount.class);
                                                groupCode = user.getG_code();

                                                mDbRef.child("TodoList").child(uid).child(groupCode).child(dbItem).removeValue();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(view.getContext(), "그룹코드 가져오기 오류",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                })
                                .show();

                        return false;
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static class ListHeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView header_title, todoPerson, repeatDay, alarmTime;
        public ImageView btn_expand_toggle, alarmIcon;
        public Item refferalItem;
        public CheckBox checkBox;

        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
            checkBox = (CheckBox) itemView.findViewById(R.id.activity);
            alarmIcon = (ImageView) itemView.findViewById(R.id.alarmIcon);
            todoPerson = (TextView) itemView.findViewById(R.id.todoPerson);
            repeatDay = (TextView) itemView.findViewById(R.id.repeatDay);
            alarmTime = (TextView) itemView.findViewById(R.id.alarmTime);
        }
    }

    public static class Item {
        public int type;
        public String title, activity, tdid, uid, time;
        public boolean repeat, alarm, done;
        public List<Item> invisibleChildren;
        Object member;

        //header 생성
        public Item(int type, String title) {
            this.type = type;
            this.title = title;
        }

        //그룹 투두 받아올 때(no alarm, 누구도 수행 X)
        public Item (int type, String activity, String tdid, String uid, boolean repeat, boolean alarm, boolean done){
            this.type = type;
            this.type = type;
            this.activity = activity;
            this.tdid = tdid;
            this.uid = uid;
            this.repeat = repeat;
            this.alarm = alarm;
            this.done = done;
        }

        //그룹 투두 받아올 때(no alarm, 누군가 투두 수행)
        public Item(int type, String activity, String tdid, String uid, boolean repeat, boolean alarm, boolean done, Object member){
            this.type = type;
            this.activity = activity;
            this.tdid = tdid;
            this.uid = uid;
            this.repeat = repeat;
            this.alarm = alarm;
            this.done = done;
            this.member = member;
        }

        //그룹 투두 받아올 때(alarm, 누구도 수행 X)
        public Item(int type, String activity, String tdid, String uid, boolean repeat, boolean alarm, String time, boolean done){
            this.type = type;
            this.activity = activity;
            this.tdid = tdid;
            this.uid = uid;
            this.repeat = repeat;
            this.alarm = alarm;
            this.time = time;
            this.done = done;
        }

        //그룹 투두 받아올 때(alarm, 누군가 투두 수행)
        public Item(int type, String activity, String tdid, String uid, boolean repeat, boolean alarm, String time, boolean done, Object member){
            this.type = type;
            this.activity = activity;
            this.tdid = tdid;
            this.uid = uid;
            this.repeat = repeat;
            this.alarm = alarm;
            this.time = time;
            this.done = done;
            this.member = member;
        }

        //개인 투두 받아올때
        public Item (int type, String activity, String tdid, String uid, boolean repeat, boolean done){
            this.type = type;
            this.type = type;
            this.activity = activity;
            this.tdid = tdid;
            this.uid = uid;
            this.repeat = repeat;
            this.done = done;
        }
    }
}

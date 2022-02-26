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
    public static final int CHILD = 1;

    FirebaseAuth mAuth;
    DatabaseReference mDbRef;
    String groupCode, groupName, uid;

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
            case CHILD:
                LayoutInflater child_inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                child_view = child_inflater.inflate(R.layout.todo, parent, false);
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
                itemController.header_title.setText(item.text);
                if (item.invisibleChildren == null) {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.circle_minus);
                } else {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.circle_plus);
                }
                itemController.btn_expand_toggle.setOnClickListener(v -> {
                    if (item.invisibleChildren == null) {
                        item.invisibleChildren = new ArrayList<>();
                        int count = 0;
                        int pos = data.indexOf(itemController.refferalItem);
                        while (data.size() > pos + 1 && data.get(pos + 1).type == CHILD) {
                            item.invisibleChildren.add(data.remove(pos + 1));
                            count++;
                        }
                        notifyItemRangeRemoved(pos + 1, count);
                        itemController.btn_expand_toggle.setImageResource(R.drawable.circle_plus);
                    } else {
                        int pos = data.indexOf(itemController.refferalItem);
                        int index = pos + 1;
                        for (Item i : item.invisibleChildren) {
                            data.add(index, i);
                            index++;
                        }
                        notifyItemRangeInserted(pos + 1, index - pos - 1);
                        itemController.btn_expand_toggle.setImageResource(R.drawable.circle_minus);
                        item.invisibleChildren = null;
                    }
                });
                break;
            case CHILD:
                final ListHeaderViewHolder childItemController = (ListHeaderViewHolder) holder;
                childItemController.checkBox.setText(item.text);
                childItemController.checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(view.getContext(), "long click",
                                Toast.LENGTH_SHORT).show();

                        new MaterialAlertDialogBuilder(view.getContext())
                                .setTitle(item.text)
                                .setMessage(dbItem)
                                .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(view.getContext(), "취소",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("수정", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(view.getContext(), "수정",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(view.getContext(), "삭제",
                                                Toast.LENGTH_SHORT).show();

                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        uid = firebaseUser.getUid();

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
        public TextView header_title;
        public ImageView btn_expand_toggle;
        public Item refferalItem;
        public CheckBox checkBox;

        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        }
    }

    public static class Item {
        public int type;
        public String text;
        public List<Item> invisibleChildren;

        public Item(int type, String text) {
            this.type = type;
            this.text = text;
        }
    }
}

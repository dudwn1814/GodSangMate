package com.example.re_todolist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<String> itemList = new ArrayList<>();
    private final List<String> yesterdayList;
    public static List<String> yesterdayPutList = new ArrayList<>();

    public RecyclerAdapter(ArrayList<String> yesterday_tdid, ArrayList<String> notDone_td) {
        this.yesterdayList = yesterday_tdid;
        itemList.clear();
        yesterdayPutList.clear();

        for (int i = 0; i < notDone_td.size(); i++) {
            itemList.add(notDone_td.get(i));
            Log.d("notDone", notDone_td.get(i));
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox item;

        public ViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.recycler_item);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        ViewHolder holder = (ViewHolder) viewHolder;

        holder.item.setText(itemList.get(i));

        holder.item.setOnClickListener(view -> {
            if (holder.item.isChecked()) {
                Log.v("CHECKBOX_TEST", "checkbox test" + holder.item.getText());
                yesterdayPutList.add(yesterdayList.get(i));
            }
        });
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

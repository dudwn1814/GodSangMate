package com.example.re_todolist;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView textView_todo;
    public ImageView alarm_icon;
    public CheckBox checkBox;

    public ViewHolder(Context context, View itemView) {
        super(itemView);

        textView_todo = itemView.findViewById(R.id.todoPerson);
        alarm_icon = itemView.findViewById(R.id.todoIcon);
        checkBox = itemView.findViewById(R.id.checkBox);
    }
}

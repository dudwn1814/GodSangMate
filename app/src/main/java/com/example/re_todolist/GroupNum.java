package com.example.re_todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GroupNum extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_num);

        TextView Group_name = (TextView) findViewById(R.id.group_name);
        TextView Group_code = (TextView) findViewById(R.id.group_code);

        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        String groupname = bundle.getString("group_name");
        String groupcode = bundle.getString("group_code");

        Group_name.setText(groupname + "의");
        Group_code.setText(groupcode + "입니다.");


        Button backButton = (Button) findViewById(R.id.button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("message", "result message is OK!");

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}

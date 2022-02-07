package com.example.re_todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;

public class GroupName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_name);

        Button codeButton = findViewById(R.id.codeButton);
        codeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int codeLen = 6;
                final char[] characterTable = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                        'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                        'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

                Random random = new Random(System.currentTimeMillis());
                int table_len = characterTable.length;
                StringBuffer buf = new StringBuffer();

                for (int i = 0; i < codeLen; i++) {
                    buf.append(characterTable[random.nextInt(table_len)]);
                }
                String s = buf.toString();

                Intent intent = new Intent(GroupName.this, GroupNum.class);
                EditText nameInput = (EditText) findViewById(R.id.nameInput);
                intent.putExtra("group_name", nameInput.getText().toString());
                intent.putExtra("group_code", s);

                startActivity(intent);
            }
        });
    }
}
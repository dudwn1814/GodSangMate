package com.example.re_todolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;

public class GroupName extends AppCompatActivity {

    EditText nameInput;
    String groupname;
    AlertDialog.Builder alert_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_name);

        alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage("그룹 이름을 입력해주세요.");
        alert_confirm.setPositiveButton("확인", null);

        Button codeButton = findViewById(R.id.codeButton);
        codeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                nameInput = findViewById(R.id.nameInput);
                groupname = nameInput.getText().toString();

                if(groupname.length() == 0){
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }

                else{
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
                    intent.putExtra("group_name", groupname);
                    intent.putExtra("group_code", s);

                    startActivity(intent);
                }

/*                int codeLen = 6;
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

                startActivity(intent);          */
            }
        });
    }
}
package com.example.re_todolist;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
import com.example.re_todolist.R;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private int index = 0;
    int nod = 400;
    int days[] = new int[nod];
    int months[] = new int[nod];
    int years[] = new int[nod];

    CalendarView calendarView;
    Button saveButton;
    EditText textInput;
    List<String> calendarString = new ArrayList<>();



    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityCreated(Bundle b){
        super.onActivityCreated(b);

        saveButton = (Button) getView().findViewById(R.id.saveButton);
        textInput = (EditText)getView().findViewById(R.id.textInput);

        readInfo();



        // 저장 버튼 이벤트 처리 기능 --> 임시로 기능 확인만을 위해서 작성함
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // calendarString에 입력한 textInput저장
                calendarString.add(index, textInput.getText().toString());
                index++;
                // 일정 저장 시 입력하는 곳 초기화
                textInput.setText("");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                AlertDialog dialog = builder.setMessage("저장되었습니다.")
                        .setPositiveButton("확인", null)
                        .create();
                dialog.show();
            }
        });

    }

    @Override
    public void onPause(){
        super.onPause();
        saveInfo();
    }

    public void saveInfo(){
        File file = new File(getContext().getFilesDir(), "saved");
        File dFile = new File(getContext().getFilesDir(), "days");
        File mFile = new File(getContext().getFilesDir(), "month");
        File yFile = new File(getContext().getFilesDir(), "years");

        try {
            FileOutputStream fOut = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fOut));

            FileOutputStream fOutDays = new FileOutputStream(dFile);
            BufferedWriter bwDays = new BufferedWriter(new OutputStreamWriter(fOutDays));

            FileOutputStream fOutMonths = new FileOutputStream(mFile);
            BufferedWriter bwMonths = new BufferedWriter(new OutputStreamWriter(fOutMonths));

            FileOutputStream fOutYears = new FileOutputStream(yFile);
            BufferedWriter bwYears = new BufferedWriter(new OutputStreamWriter(fOutYears));

            for(int i=0; i<index;i++){
                bw.write(calendarString.get(i));
                bw.newLine();
                bwDays.write(days[i]);
                bwMonths.write(months[i]);
                bwYears.write(years[i]);
            }

            bw.close();
            fOut.close();
            bwDays.close();
            fOutDays.close();
            bwMonths.close();
            fOutMonths.close();
            bwYears.close();
            fOutYears.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readInfo(){
        File file = new File(getContext().getFilesDir(), "saved");
        File dFile = new File(getContext().getFilesDir(), "days");
        File mFile = new File(getContext().getFilesDir(), "month");
        File yFile = new File(getContext().getFilesDir(), "years");

        if(!file.exists()){
            return;
        }
        try{
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            FileInputStream isDays = new FileInputStream(dFile);
            BufferedReader rDays = new BufferedReader(new InputStreamReader(isDays));
            FileInputStream isMonths = new FileInputStream(mFile);
            BufferedReader rMonths = new BufferedReader(new InputStreamReader(isMonths));
            FileInputStream isYears = new FileInputStream(yFile);
            BufferedReader rYears = new BufferedReader(new InputStreamReader(isYears));

            int i = 0;
            String line = reader.readLine();

            while(line!=null){
                calendarString.add(line);
                line = reader.readLine();
                days[i] = rDays.read();
                months[i] = rMonths.read();
                years[i] = rYears.read();
                i++;
            }
            index = i;
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_todo, container, false);
    }
}

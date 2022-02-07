package com.example.re_todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class myDBhelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "sampleDB";
    public static final String TABLE_NAME =  "userinfo";

    public static final String COL_1 = "no";
    public static final String COL_2 = "ID";
    public static final String COL_3 = "PW";

    public myDBhelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE "+ TABLE_NAME + "( 'no' INTEGER PRIMARY KEY AUTOINCREMENT, 'ID' TEXT NOT NULL UNIQUE, 'PW' TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String Id, String Pw){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, Id);
        contentValues.put(COL_3, Pw);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1) return false;
        else return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME, null);
        return res;
    }

    public boolean checkID(String Id){
        //if select count (case when ID='email' then 1 end) from TABLE_NAME
        return true;
    }
}

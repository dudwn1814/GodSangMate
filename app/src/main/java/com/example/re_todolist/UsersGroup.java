package com.example.re_todolist;

/* 사용자 계정 정보 모델  클래스 */
public class UsersGroup {
    private String uid;         //firebase uid
    private String emailID;
    private String password;
    private String g_code;
    private String g_name;

    public UsersGroup() { }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) { this.uid = uid; }

    public String getG_code() { return g_code; }
    public void setG_code(String g_code) {
        this.g_code = g_code;
    }


    public String getG_name() { return g_name; }
    public void setG_name(String g_name) {
        this.g_name = g_name;
    }



}

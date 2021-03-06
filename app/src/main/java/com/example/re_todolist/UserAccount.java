package com.example.re_todolist;

/* 사용자 계정 정보 모델  클래스 */
public class UserAccount {
    private String uid;         //firebase uid
    private String emailID;
    private String password;
    private String nickname;
    private String g_code;

    public UserAccount() {
    }

    public UserAccount(String uid, String emailID, String password){
        this.uid = uid;
        this.emailID = emailID;
        this.password = password;
    }

    public UserAccount(String uid, String emailID, String password, String g_code){
        this.uid = uid;
        this.emailID = emailID;
        this.password = password;
        this.g_code = g_code;
    }

    public UserAccount(String uid, String emailID, String password, String g_code, String nickname){
        this.uid = uid;
        this.emailID = emailID;
        this.password = password;
        this.g_code = g_code;
        this.nickname = nickname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getG_code() { return g_code; }

    public void setG_code(String g_code) { this.g_code = g_code; }
}

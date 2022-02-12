package com.example.re_todolist;

/* 사용자 계정 정보 모델  클래스 */
public class UserAccount {
    private String uid;         //firebase uid
    private String emailID;
    private String password;

    public UserAccount() { }

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

}

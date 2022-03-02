package com.example.re_todolist;

public class GroupMember {
    String uid;
    String nickname;

    public GroupMember() { }

    public GroupMember(String uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
    }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }
}

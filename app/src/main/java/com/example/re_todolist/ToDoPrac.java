package com.example.re_todolist;

public class ToDoPrac {
    //투두, 반복-요일, 알람-시간, TodoID, uid, 완료여부
    String activity;
    boolean repeat;
    boolean alarm;
    String time;
    String tdid;
    String uid;
    boolean done;
    Object member;

    public ToDoPrac(){
        this.done = false;
    };

    //그룹 투두 data 받아올 때(no alarm, 누구도 수행 X), 개인 투두 받아올때
    public ToDoPrac(String activity, String tdid, String uid, boolean repeat, boolean alarm, boolean done){
        this.activity = activity;
        this.tdid = tdid;
        this.uid = uid;
        this.repeat = repeat;
        this.alarm = alarm;
        this.done = done;
    }

    //그룹 투두 data 받아올 때(no alarm, 누군가 투두 수행)
    public ToDoPrac(String activity, String tdid, String uid, boolean repeat, boolean alarm, boolean done, Object member){
        this.activity = activity;
        this.tdid = tdid;
        this.uid = uid;
        this.repeat = repeat;
        this.alarm = alarm;
        this.done = done;
        this.member = member;
    }

    //그룹 투두 data 받아올 때(alarm, 누구도 수행 X)
    public ToDoPrac(String activity, String tdid, String uid, boolean repeat, boolean alarm, String time, boolean done){
        this.activity = activity;
        this.tdid = tdid;
        this.uid = uid;
        this.repeat = repeat;
        this.alarm = alarm;
        this.time = time;
        this.done = done;
    }

    //그룹 투두 data 받아올 때(alarm, 누군가 투두 수행)
    public ToDoPrac(String activity, String tdid, String uid, boolean repeat, boolean alarm, String time, boolean done, Object member){
        this.activity = activity;
        this.tdid = tdid;
        this.uid = uid;
        this.repeat = repeat;
        this.alarm = alarm;
        this.time = time;
        this.done = done;
        this.member = member;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }

    public Object getMember() {
        return member;
    }

    public void setMember(Object member) {
        this.member = member;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTdid() {
        return tdid;
    }

    public void setTdid(String tdid) {
        this.tdid = tdid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}


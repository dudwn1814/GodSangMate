package com.example.re_todolist;

public class ToDoPrac {
    //투두, 반복-요일, 알람-시간, TodoID, uid, 완료여부
    String Activity;
    boolean repeat;
    Week week;
    boolean alarm;
    String time;
    String TDId;
    String uid;
    boolean done;

    public ToDoPrac(){
        this.done = false;
    };

    public String getActivity() {
        return Activity;
    }

    public void setActivity(String activity) {
        Activity = activity;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public Week getWeek() {
        return week;
    }

    public void setWeek(Week week) {
        this.week = week;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTDId() {
        return TDId;
    }

    public void setTDId(String TDId) {
        this.TDId = TDId;
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

class Week{
    boolean Sun, Mon, Tue, Wed, Thu, Fri, Sat;

    public Week(){
        this.Sun=false;
        this.Mon=false;
        this.Tue=false;
        this.Wed=false;
        this.Thu=false;
        this.Fri=false;
        this.Sat=false;
    }

    public boolean isSun() {
        return Sun;
    }

    public void setSun(boolean sun) {
        Sun = sun;
    }

    public boolean isMon() {
        return Mon;
    }

    public void setMon(boolean mon) {
        Mon = mon;
    }

    public boolean isTue() {
        return Tue;
    }

    public void setTue(boolean tue) {
        Tue = tue;
    }

    public boolean isWed() {
        return Wed;
    }

    public void setWed(boolean wed) {
        Wed = wed;
    }

    public boolean isThu() {
        return Thu;
    }

    public void setThu(boolean thu) {
        Thu = thu;
    }

    public boolean isFri() {
        return Fri;
    }

    public void setFri(boolean fri) {
        Fri = fri;
    }

    public boolean isSat() {
        return Sat;
    }

    public void setSat(boolean sat) {
        Sat = sat;
    }
}

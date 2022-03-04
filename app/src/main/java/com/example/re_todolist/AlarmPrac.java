package com.example.re_todolist;

public class AlarmPrac {
    //투두, 반복-요일, 알람-시간, TodoID, uid, 완료여부
    String activity;
    boolean repeat;
    String tdid;
    String alarm_time;

    //그룹 투두 data 받아올 때(alarm, 누구도 수행 X)
    public AlarmPrac() {
        this.activity = activity;
        this.tdid = tdid;
        this.repeat = repeat;
        this.alarm_time = alarm_time;
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

    public String getTdid() {
        return tdid;
    }

    public void setTdid(String tdid) {
        this.tdid = tdid;
    }

    public String getAlarm_time() {
        return alarm_time;
    }

    public void setAlarm_time(String alarm_time) {
        this.alarm_time = alarm_time;
    }
}


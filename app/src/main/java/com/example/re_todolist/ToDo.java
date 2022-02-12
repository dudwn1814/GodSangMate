package com.example.re_todolist;

public class ToDo {

    //알람 반복 시간 요일, 그룹/개인, 투두
    String todo;
    String personal;
    boolean repeat;
    String day;
    boolean alarm;
    String time;

    public ToDo(String todo, String personal, boolean repeat, String day, boolean alarm, String time) {
        this.todo = todo;
        this.personal = personal;
        this.repeat = repeat;
        this.day = day;
        this.alarm = alarm;
        this.time = time;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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
}

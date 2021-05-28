package com.example.campusl2k2.Data;

import java.io.Serializable;

public class ScheduleItem implements Serializable { //스케줄의 리스트를 저장할 객체
    private String title;
    private String time;
    private boolean isChecked;
    public String getTitle(){ return title; }
    public String getTime(){ return time; }
    public boolean getChecked(){return isChecked;}
    public void setTitle(String title){
        this.title=title;
    }
    public void setTime(String time){
        this.time=time;
    }
    public void setChecked(boolean state){this.isChecked=state;}
}

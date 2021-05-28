package com.example.campusl2k2.Structure;

public class Pair {
    private String sid;
    private String disName;
    public Pair(String sid, String disname){
        this.sid=sid;
        this.disName=disname;
    }
    public void setSid(String sid){
        this.sid=sid;
    }
    public void setDisName(String disName){
        this.disName=disName;
    }
    public String getSid(){return this.sid;}
    public String getDisName(){return this.disName;}
}

package com.example.campusl2k2.Data;

public class Member {
    private String sipSessionID;
    private String sipSessionPW;
    private String disPlayName;
    public Member(String sid, String spw, String dis){
        sipSessionID=sid;
        sipSessionPW=spw;
        disPlayName=dis;
    }
    public void setDisPlayName(String dis){
        this.disPlayName=dis;
    }
    public String getSipSessionID(){
        return sipSessionID;
    }
    public String getSipSessionPW(){
        return sipSessionPW;
    }
    public String getDisPlayName(){
        return disPlayName;
    }
}

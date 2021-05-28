package com.example.campusl2k2.Data;

import com.example.campusl2k2.Structure.Pair;

import java.util.ArrayList;

public class DatabaseRoomDetails { //데이터베이스에 실제로 저장될 상세 데이터
    private String roomName;
    private ArrayList<Pair> participants;
    private String maker;
    private String teller;

    public DatabaseRoomDetails(String roomName, ArrayList<Pair> participants,String maker) {
        this.roomName = roomName;
        this.participants = participants;
        this.maker=maker;
        this.teller="";
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public ArrayList<Pair> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Pair> participants) {
        this.participants = participants;
    }

    public String getMaker(){return maker;}

    public void setMaker(String maker){this.maker=maker;}

    public String getTeller(){return teller;}

    public void setTeller(String teller){this.teller=teller;}
}

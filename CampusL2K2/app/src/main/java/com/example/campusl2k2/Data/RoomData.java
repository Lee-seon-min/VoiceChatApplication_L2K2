package com.example.campusl2k2.Data;

public class RoomData { //리사클러뷰의 하나의 뷰에 담을 정보
    private String roomName;
    private String roomId; //방의 고유 아이디
    private String makerSipId;

    public RoomData(String roomName, String roomId,String maker) {
        this.roomName = roomName;
        this.roomId = roomId;
        this.makerSipId=maker;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMakerSipId(){return makerSipId;}
}

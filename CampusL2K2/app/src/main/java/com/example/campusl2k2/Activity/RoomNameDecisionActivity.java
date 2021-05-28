package com.example.campusl2k2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.example.campusl2k2.R;

public class RoomNameDecisionActivity extends Activity {
    private Button make;
    private EditText roomName;
    public static int MAKEROOM_RESCODE=200;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_name_decision);

        setVal();
        setEvents();
    }
    private void setVal(){
        make=findViewById(R.id.createRoom);
        roomName=findViewById(R.id.targetRoomName);
    }
    private void setEvents(){
        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("targetRoomName",roomName.getText().toString());
                setResult(MAKEROOM_RESCODE,intent);
                finish();
            }
        });
    }
}

package com.example.campusl2k2.Activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusl2k2.Data.ScheduleItem;
import com.example.campusl2k2.DataBase.DataHandler;
import com.example.campusl2k2.R;
import com.example.campusl2k2.Util.UIdisplay;

import java.util.ArrayList;

public class WriteScheduleActivity extends AppCompatActivity { //작성 액티비티
    private String thisdate;
    private EditText title_Contents,daily_Contents;
    private Button saveBtn;
    private AlertDialog alertDialog;
    private ArrayList<ScheduleItem> list;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_schedule);


        setVal();
        setEvents();

        Intent intent=getIntent();
        thisdate=intent.getStringExtra(CalendarActivity.PICKDATE); //선택한 날짜
        list=(ArrayList<ScheduleItem>)intent.getSerializableExtra("scheduleList");
    }
    public void setVal(){
        title_Contents=findViewById(R.id.title_contents);
        daily_Contents=findViewById(R.id.daily_contents);
        saveBtn =findViewById(R.id.write_savebtn);
        alertDialog=getAlertDialog();
    }
    public AlertDialog getAlertDialog(){ //저장하기 버튼 누를시, 안내문
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("일정 저장 알림");
        builder.setMessage("해당 내용을 저장하시겠습니까?");
        builder.setIcon(getResources().getDrawable(R.drawable.ic_baseline_campaign_24));
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return; //그냥 종료
            }
        });
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() { //데이터베이스에 저장
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues values=new ContentValues();
                values.put("title",title_Contents.getText().toString());
                values.put("wtime",thisdate);
                values.put("todo",daily_Contents.getText().toString());
                values.put("ischecked","false");
                Uri uri=getContentResolver().insert(DataHandler.CONTENT_URI_SCHDULE,values);

                Intent intent=new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        return builder.create();
    }
    public void setEvents(){
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               saveSchedule();
            }
        });
    }
    public void saveSchedule(){
        String nowTitle=title_Contents.getText().toString();
        if(nowTitle.length()<1){
            UIdisplay.showMessage(WriteScheduleActivity.this,"제목은 필수 입력 사항입니다.");
            return;
        }
        for(int i=0;i<list.size();i++){
            if(list.get(i).getTitle().equals(nowTitle)){
                UIdisplay.showMessage(WriteScheduleActivity.this,"이미 동일한 제목의 일정이 존재합니다.");
                return;
            }
        }
        alertDialog.show();
    }
}

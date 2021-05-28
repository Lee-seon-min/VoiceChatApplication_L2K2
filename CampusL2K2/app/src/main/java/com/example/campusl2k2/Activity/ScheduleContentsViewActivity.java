package com.example.campusl2k2.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusl2k2.Data.ScheduleItem;
import com.example.campusl2k2.DataBase.DataHandler;
import com.example.campusl2k2.R;
import com.example.campusl2k2.Util.UIdisplay;

import java.util.ArrayList;

public class ScheduleContentsViewActivity extends AppCompatActivity { //저장된 일정을 불러오는 곳
    public class NoDataSQLException extends Exception{
        public NoDataSQLException(String message){
            super(message);
        }
    }
    private EditText title, contents;
    private Button fixbtn;
    private String thisTime,thisTitle;
    private ArrayList<ScheduleItem> list;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_contents_view);
        setVal();
        setEvents();

        Intent intent=getIntent();
        thisTitle=intent.getStringExtra(ScheduleActivity.TITLE); //시간
        thisTime=intent.getStringExtra(ScheduleActivity.TIME); //제목
        list=(ArrayList<ScheduleItem>)intent.getSerializableExtra("scheduleList");
        try{ //데이터 불러오기
            Cursor cursor=getContentResolver().query(DataHandler.CONTENT_URI_SCHDULE,new String[]{"title, todo"},"title = ? and wtime = ?",new String[]{thisTitle,thisTime},null); //제목과 시간으로 해당 콘텐츠를 불러옴
            if(cursor==null){ //가져올 데이터가 없다면,
                throw new NoDataSQLException("데이터 무결성 오류");
            }
            while(cursor.moveToNext()){
                title.setText(cursor.getString(0));
                contents.setText(cursor.getString(1));
            }
        }
        catch(NoDataSQLException e){
            Log.d("NoDataSQLException...",e.getMessage());
        }

    }
    public void setVal(){
        title=findViewById(R.id.fix_title);
        contents=findViewById(R.id.fix_daily_contents);
        fixbtn=findViewById(R.id.fix_contentsbtn);
    }
    public void setEvents(){
        fixbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //수정버튼
                updateSchedule();
            }
        });
    }
    public void updateSchedule(){
        String nowTitle=title.getText().toString();
        if(nowTitle.length()<1){
            UIdisplay.showMessage(ScheduleContentsViewActivity.this,"제목은 필수 입력 사항입니다.");
            return;
        }
        for(int i=0;i<list.size();i++){
            if(!list.get(i).getTitle().equals(thisTitle)&&list.get(i).getTitle().equals(nowTitle)){
                UIdisplay.showMessage(ScheduleContentsViewActivity.this,"이미 동일한 제목의 일정이 존재합니다.");
                return;
            }
        }
        ContentValues values=new ContentValues();
        values.put("title",title.getText().toString());
        values.put("todo",contents.getText().toString());
        int cnt = getContentResolver().update(DataHandler.CONTENT_URI_SCHDULE,values,"title = ? and wtime = ?",new String[]{thisTitle,thisTime});

        if(cnt!=0) {
            Toast.makeText(ScheduleContentsViewActivity.this, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent();
            setResult(RESULT_OK,intent);
            finish();
        }
        else
            Toast.makeText(ScheduleContentsViewActivity.this,"일시적인 오류가 발생하였습니다.",Toast.LENGTH_SHORT).show();

    }
}

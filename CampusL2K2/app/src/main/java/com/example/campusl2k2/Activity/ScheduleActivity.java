package com.example.campusl2k2.Activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusl2k2.Adapter.ScheduleRecyclerViewAdaptor;
import com.example.campusl2k2.Data.ScheduleItem;
import com.example.campusl2k2.DataBase.DataHandler;
import com.example.campusl2k2.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity implements ScheduleRecyclerViewAdaptor.IShowContents {
    private FloatingActionButton writebtn;
    private RecyclerView recyclerView;
    public ArrayList<ScheduleItem> list=new ArrayList<>(); //데이터베이스에서 가져온 데이터로 리스트화
    private ScheduleRecyclerViewAdaptor adaptor=new ScheduleRecyclerViewAdaptor();
    private String thisdate;
    private int REQUEST_WRITE=101;
    private int REQUEST_UPDATE=102;
    public static String TITLE="title";
    public static String TIME="time";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        setVal();
        setEvents();

        Intent intent=getIntent();
        thisdate=intent.getStringExtra(CalendarActivity.PICKDATE); //캘린더에서 고른 날짜 데이터

        //데이터베이스에서 데이터 가져와서 리스트에 담기
        readData();

        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this); //리사이클러뷰의 스타일은 Linear로
        recyclerView.setLayoutManager(layoutManager); //레이아웃세팅
        adaptor.setList(list); //리스트 세팅
        adaptor.setListener(this); //인터페이스객체로 설정
        recyclerView.setAdapter(adaptor); //어뎁터 세팅
        adaptor.notifyDataSetChanged();
    }
    public void setVal() {
        writebtn = findViewById(R.id.addSchedule);
        recyclerView = findViewById(R.id.scheduleList);
    }
    public void setEvents() {
        writebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //일정추가버튼
                Intent intent=new Intent(ScheduleActivity.this,WriteScheduleActivity.class);
                intent.putExtra(CalendarActivity.PICKDATE,thisdate);
                intent.putExtra("scheduleList",list);
                startActivityForResult(intent,REQUEST_WRITE); //작성을 요청
            }
        });
    }
    public void readData(){
        Cursor cursor=getContentResolver().query(DataHandler.CONTENT_URI_SCHDULE,new String[]{"title","wtime","ischecked"},"wtime = ?",new String[]{thisdate},null); //가져온 날짜 데이터를 활용하여, 해당 URI와 매칭하며 해당하는 테이블에서 조작한다.
        if(cursor!=null)
            while(cursor.moveToNext()){ //한 행씩 받아옴
                String title=cursor.getString(0); //0번째 컬럼
                String time=cursor.getString(1); //1번째 컬럼
                String state=cursor.getString(2);//2번째 컬럼
                ScheduleItem item=new ScheduleItem(); //객체생성
                item.setTime(time);
                item.setTitle(title);
                if(state.equals("false"))
                    item.setChecked(false);
                else
                    item.setChecked(true);
                list.add(item); //리스트에 담기
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode==REQUEST_WRITE || requestCode==REQUEST_UPDATE) &&
                resultCode==RESULT_OK){ //작성이 되었으므로, 다시 리스트를 갱신해야한다.

            list.clear();

            readData();

            adaptor.setList(list);
            adaptor.notifyDataSetChanged(); //갱신
        }
    }

    @Override
    public void intentPage(String title, String wtime,ArrayList<ScheduleItem> scheduleItems) { //일정 불러오기(리사이클러뷰 어뎁터의 메소드 오버라이딩)
        Intent intent=new Intent(ScheduleActivity.this,ScheduleContentsViewActivity.class);
        intent.putExtra(TITLE,title);
        intent.putExtra(TIME,wtime);
        intent.putExtra("scheduleList",scheduleItems);
        startActivityForResult(intent,REQUEST_UPDATE);
    }

    @Override
    public void stateChange(String title, String wtime, boolean state) {
        String ch_state=null;
        if(state)
            ch_state="true";
        else
            ch_state="false";
        ContentValues values=new ContentValues();
        values.put("ischecked",ch_state);
        int cnt = getContentResolver().update(DataHandler.CONTENT_URI_SCHDULE,values,"title = ? and wtime = ?",new String[]{title,wtime});
    }

    @Override
    public void deleteSchedule(String title,String wtime){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("일정 삭제 알림");
        builder.setMessage("해당 내용을 삭제하겠습니까?\n\n"+title);
        builder.setIcon(getResources().getDrawable(R.drawable.ic_baseline_campaign_24));
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return; //그냥 종료
            }
        });
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() { //데이터베이스에서 삭제
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int cnt = getContentResolver().delete(DataHandler.CONTENT_URI_SCHDULE,"title = ? and wtime = ?",new String[]{title,wtime});

                list.clear();

                readData();

                adaptor.setList(list);
                adaptor.notifyDataSetChanged(); //갱신
            }
        });
        builder.create().show();
    }
}

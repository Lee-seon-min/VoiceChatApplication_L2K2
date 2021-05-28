package com.example.campusl2k2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campusl2k2.DataBase.DataHandler;
import com.example.campusl2k2.R;

import java.util.Calendar;

public class CalendarActivity extends Activity { //일정
    private CalendarView calendarView;
    private Button chkScheduleBtn;
    private String thisdate;
    private LinearLayout container;
    private boolean isinSchedule=false;
    public static String PICKDATE="pickdate";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setVal();
        setEvents();

        Calendar curDate=Calendar.getInstance();
        curDate.setTimeInMillis(calendarView.getDate()); //캘린더 뷰에 픽되어있는 날짜를 가져온다.
        thisdate=curDate.get(Calendar.YEAR)+"-"+(curDate.get(Calendar.MONTH)+1)+"-"+curDate.get(Calendar.DATE); //가져온 날짜를 알맞은 포맷으로 변경한다.(2020-9-2)
    }
    public void readData(){
        isinSchedule=false;
        Cursor cursor=getContentResolver().query(DataHandler.CONTENT_URI_SCHDULE,new String[]{"title","ischecked"},"wtime = ?",new String[]{thisdate},null); //가져온 날짜 데이터를 활용하여, 해당 URI와 매칭하며 해당하는 테이블에서 조작한다.
        if(cursor!=null)
            while(cursor.moveToNext()){ //한 행씩 받아옴
                isinSchedule=true;
                String title=cursor.getString(0); //0번째 컬럼
                String state=cursor.getString(1); //1번째 컬럼
                TextView view1 = new TextView(this);
                String todoText="[일정] "+title;
                view1.setText(todoText);
                view1.setTextSize(12);
                if(state.equals("false"))
                    view1.setTextColor(Color.BLACK);
                else
                    view1.setTextColor(Color.RED);

                //layout_width, layout_height, gravity 설정
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view1.setGravity(Gravity.CENTER);
                view1.setLayoutParams(lp);

                //부모 뷰에 추가
                container.addView(view1);
            }
        if(!isinSchedule){
            TextView view1 = new TextView(this);
            String todoText="일정이 없습니다.";
            view1.setText(todoText);
            view1.setTextSize(13);
            view1.setTextColor(Color.GRAY);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view1.setGravity(Gravity.CENTER);
            view1.setLayoutParams(lp);

            //부모 뷰에 추가
            container.addView(view1);
        }
    }
    public void setVal(){
        calendarView=findViewById(R.id.mycalendar);
        chkScheduleBtn=findViewById(R.id.editbutton);
        container=findViewById(R.id.summaryScheduleContainer);
    }
    public void setEvents() {
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                thisdate = year + "-" + (month+1) + "-" + dayOfMonth; //선택한 날짜로 날짜 저장
                container.removeAllViews();
                readData();
            }
        });
        chkScheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CalendarActivity.this, ScheduleActivity.class);
                intent.putExtra(PICKDATE,thisdate);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        container.removeAllViews();
        readData();
    }
}

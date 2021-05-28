package com.example.campusl2k2.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusl2k2.Data.ScheduleItem;
import com.example.campusl2k2.R;

import java.util.ArrayList;

public class ScheduleRecyclerViewAdaptor extends RecyclerView.Adapter<ScheduleRecyclerViewAdaptor.ItemHolder>{ //스케줄 리스트
    private ArrayList<ScheduleItem> list;
    private IShowContents iShowContents;
    public interface IShowContents{
        void intentPage(String title, String wtime, ArrayList<ScheduleItem> scheduleItems);
        void deleteSchedule(String title,String wtime);
        void stateChange(String title,String wtime,boolean state);
    }
    public void setList(ArrayList<ScheduleItem> list){this.list=list;}
    public void setListener(IShowContents iShowContents){
        this.iShowContents=iShowContents;
    }
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //ItemHolder객체를 생성후 onBindViewHolder 함수에서 바인딩
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_items,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) { //이벤트 및 기타 UI상세 바인딩
        final int pos=position;
        String title=list.get(position).getTitle();
        String wTime=list.get(position).getTime();
        holder.scheduleTitle.setText(title);
        holder.writeTime.setText(wTime);
        if(list.get(position).getChecked()) {
            holder.checkBox.setChecked(true);
            holder.scheduleTitle.setTextColor(Color.RED);
        }
        else {
            holder.checkBox.setChecked(false);
            holder.scheduleTitle.setTextColor(Color.BLACK);
        }
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iShowContents.deleteSchedule(title,wTime);
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    holder.scheduleTitle.setTextColor(Color.RED);
                else
                    holder.scheduleTitle.setTextColor(Color.BLACK);
                iShowContents.stateChange(title,wTime,isChecked);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //해당 뷰를 누르면, 액티비티가 대신 받아서 처리
                iShowContents.intentPage(title,wTime,list);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    } //아이템 갯수

    public class ItemHolder extends RecyclerView.ViewHolder{
        protected TextView scheduleTitle;
        protected TextView writeTime;
        protected ImageButton deleteButton;
        protected CheckBox checkBox;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            scheduleTitle=itemView.findViewById(R.id.scheduleTitle);
            writeTime=itemView.findViewById(R.id.write_time);
            deleteButton=itemView.findViewById(R.id.deleteSchedule_Button);
            checkBox=itemView.findViewById(R.id.checkScheduleState);
        }
    }
}

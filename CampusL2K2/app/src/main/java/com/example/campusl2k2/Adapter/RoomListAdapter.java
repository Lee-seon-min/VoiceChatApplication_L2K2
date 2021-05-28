package com.example.campusl2k2.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusl2k2.Data.RoomData;
import com.example.campusl2k2.R;

import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ItemHolder>{
    private List<RoomData> roomDataList;
    private IThrowable listener;
    public RoomListAdapter(IThrowable activity, List<RoomData> list){
        listener=activity;
        roomDataList=list;
    }
    public interface IThrowable{
        void callBack(String roomID,String roomName,String maker);
    }
    public void updateList(List<RoomData> list){
        roomDataList=list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.room_items,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, final int position) {
        holder.roomName.setText(roomDataList.get(position).getRoomName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.callBack(roomDataList.get(position).getRoomId(),holder.roomName.getText().toString(),
                        roomDataList.get(position).getMakerSipId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomDataList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder{
        private TextView roomName;
        public ItemHolder(@NonNull View itemView){
            super(itemView);
            roomName=itemView.findViewById(R.id.roomName);
        }
    }
}

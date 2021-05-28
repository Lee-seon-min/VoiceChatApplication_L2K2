package com.example.campusl2k2.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusl2k2.R;

import java.util.ArrayList;

public class ParticipantsListAdapter extends RecyclerView.Adapter<ParticipantsListAdapter.ItemHolder>{
    private ArrayList<String> nameList;
    public ParticipantsListAdapter(ArrayList<String> list){
        nameList=list;
    }
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.participants_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.displayName.setText(nameList.get(position));
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder{
        private TextView displayName;
        public ItemHolder(@NonNull View itemView){
            super(itemView);
            displayName=itemView.findViewById(R.id.participantName);
        }
    }
}

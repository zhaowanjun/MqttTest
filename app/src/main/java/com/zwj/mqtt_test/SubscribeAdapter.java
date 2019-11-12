package com.zwj.mqtt_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.ViewHolder> {
    private  Context context;
    private List<String> datas;

    public SubscribeAdapter(Context context, List<String> datas) {
        this.context = context;
        this.datas = datas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTime.setText(Utils.getDateTime());
        holder.tvMsg.setText(datas.get(position));
    }

    @Override
    public int getItemCount() {
        if(datas != null) {
            return datas.size();
        }
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTime;
        TextView tvMsg;

        public ViewHolder(View view) {
            super(view);
            this.tvTime = view.findViewById(R.id.tv_time);
            this.tvMsg = view.findViewById(R.id.tv_msg);
        }
    }

    public void addData(String massage) {
        datas.add(massage);
        notifyDataSetChanged();
    }

    public void clearData() {
        datas.clear();
        notifyDataSetChanged();
    }
}

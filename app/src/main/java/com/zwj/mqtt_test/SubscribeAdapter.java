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
    private List<MessageBean> datas;

    public SubscribeAdapter(Context context, List<MessageBean> datas) {
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
        MessageBean messageBean = datas.get(position);
        holder.tvTime.setText(messageBean.getDateTime());
        holder.tvMsg.setText(messageBean.getContent());
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

    public void addData(MessageBean messageBean) {
        datas.add(messageBean);
        notifyDataSetChanged();
    }

    public void clearData() {
        datas.clear();
        notifyDataSetChanged();
    }
}

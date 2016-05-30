package com.example.vinay.sms.Adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vinay.sms.R;
import com.example.vinay.sms.Messaging.SMS;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private String TAG = SearchAdapter.class.getSimpleName();
    private List<SMS> searchList;

    public SearchAdapter(List<SMS> searchList) {
        this.searchList = searchList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.search_rows, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String sender = searchList.get(position).getSenderAddress();
        String message = searchList.get(position).getMessage();
        Log.d(TAG, "onBindViewHolder: " + sender);
        Log.d(TAG, "onBindViewHolder: " + message);
        holder.sender.setText(sender);
        holder.message.setText(message);
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView sender, message;

        public MyViewHolder(View view) {
            super(view);
            sender = (TextView) view.findViewById(R.id.senderAddress);
            message = (TextView) view.findViewById(R.id.messageBody);
        }

    }
}

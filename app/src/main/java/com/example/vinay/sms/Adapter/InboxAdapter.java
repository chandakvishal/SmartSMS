package com.example.vinay.sms.Adapter;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.MyViewHolder> {

    private String TAG = InboxAdapter.class.getSimpleName();
    private List<SMS> smsList;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<SMS> SMSListToDelete = new ArrayList<>();

    public InboxAdapter(List<SMS> smsList) {
        this.smsList = smsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemViewReceived = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.inbox_row_received, parent, false);
        View itemViewSent = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.inbox_row_sent, parent, false);
        switch (viewType) {
            case 0:
                return new MyViewHolder(itemViewReceived);
            case 1:
                return new MyViewHolder(itemViewSent);
        }
        return new MyViewHolder(itemViewReceived);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd");
        holder.time.setText(ft.format(Long.parseLong(smsList.get(position).date)));
        String message = String.valueOf(smsList.get(position).message);
        holder.message.setText(message);
        if ("true".equals(smsList.get(position).isSentStatus())) {
            holder.itemView.setBackgroundColor(Color.CYAN);
        }
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if ("true".equals(smsList.get(position).isSentStatus())) {
            return 1;
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message, time;

        public MyViewHolder(View view) {
            super(view);
            time = (TextView) view.findViewById(R.id.timeStamp);
            message = (TextView) view.findViewById(R.id.messageBody);
        }
    }

    public void onItemRemove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView) {
        final int adapterPosition = viewHolder.getAdapterPosition();
        Log.d(TAG, "onItemRemove: " + adapterPosition);
        final SMS SMS = smsList.get(adapterPosition);
        Snackbar snackbar = Snackbar
                .make(recyclerView, "Question Removed", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        smsList.add(adapterPosition, SMS);
                        notifyItemInserted(adapterPosition);
                        recyclerView.scrollToPosition(adapterPosition);
                        SMSListToDelete.remove(SMS);
                    }
                });
        snackbar.show();
        smsList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        SMSListToDelete.add(SMS);
    }
}
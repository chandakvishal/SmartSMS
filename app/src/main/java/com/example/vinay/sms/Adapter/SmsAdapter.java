package com.example.vinay.sms.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.MyViewHolder> {

    private String TAG = SmsAdapter.class.getSimpleName();
    private List<SMS> questionList;
    private Context ctx;

    private List<SMS> smsListToDelete = new ArrayList<>();

    public SmsAdapter(Activity activity, List<SMS> questionList) {
        this.questionList = questionList;
        ctx = activity.getApplicationContext();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.sms_display_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String email = String.valueOf(questionList.get(position).senderAddress);
        holder.email.setText(email);

        final TypedArray imgs = ctx.getResources().obtainTypedArray(R.array.userArray);
        final Random rand = new Random();
        final int rndInt = rand.nextInt(imgs.length());
        final int resID = imgs.getResourceId(rndInt, 0);

        holder.image.setImageResource(resID);

    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView email;
        public ImageView image;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.userImageAnswer);
            email = (TextView) view.findViewById(R.id.emailAnswer);

        }
    }

    public void onItemRemove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView) {
        final int adapterPosition = viewHolder.getAdapterPosition();
        Log.d(TAG, "onItemRemove: " + adapterPosition);
        final SMS question = questionList.get(adapterPosition);
        Snackbar snackbar = Snackbar
                .make(recyclerView, "Question Removed", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        questionList.add(adapterPosition, question);
                        notifyItemInserted(adapterPosition);
                        recyclerView.scrollToPosition(adapterPosition);
                        smsListToDelete.remove(question);
                    }
                });
        snackbar.show();
        questionList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        smsListToDelete.add(question);
    }
}
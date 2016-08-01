package com.example.vinay.sms.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vinay.sms.MainActivity;
import com.example.vinay.sms.Messaging.Display.SmsDisplayFragment;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.MyViewHolder> implements MessagingAdapter {

    private String TAG = SmsAdapter.class.getSimpleName();
    private List<SMS> smsList;
    private Context ctx;

    private List<SMS> smsListToDelete = new ArrayList<>();

    public SmsAdapter(Activity activity, List<SMS> smsList) {
        this.smsList = smsList;
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

        String address = String.valueOf(smsList.get(position).senderAddress);
        holder.address.setText(address);

        final TypedArray imgs = ctx.getResources().obtainTypedArray(R.array.userArray);
        final Random rand = new Random();
        final int rndInt = rand.nextInt(imgs.length());
        final int resID = imgs.getResourceId(rndInt, 0);

        holder.image.setImageResource(resID);

        int count = SmsDisplayFragment.getCountOfUnreadMessages(address);
        Log.d(TAG, "onBindViewHolder: " + address + ":" + count);
        if (count > 0) {
            holder.unreadCount.setText(String.valueOf(count));
        } else {
            holder.unreadCount.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    @Override
    public void onItemRemove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView) {
        final int adapterPosition = viewHolder.getAdapterPosition();
        Log.d(TAG, "onItemRemove: " + adapterPosition);
        final SMS sms = smsList.get(adapterPosition);
        CoordinatorLayout coordinatorLayout = MainActivity.getCoordinateLayout();
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Message Deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        smsList.add(adapterPosition, sms);
                        notifyItemInserted(adapterPosition);
                        recyclerView.scrollToPosition(adapterPosition);
                        smsListToDelete.remove(sms);
                    }
                });
        snackbar.show();
        List<SMS> list = new ArrayList<>();
        list.add(sms);
        smsList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        smsListToDelete.add(sms);
    }

    public void deleteSMS(Context context, String message, String number) {
        try {
            Log.d(TAG, "deleteSMS: Deleting SMS from inbox");
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[]{"_id", "thread_id", "address",
                            "person", "date", "body"}, null, null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);

                    if (message.equals(body) && address.equals(number)) {
                        Log.d(TAG, "deleteSMS: Deleting SMS with id: " + threadId);
                        context.getContentResolver().delete(
                                Uri.parse("content://sms/" + id), null, null);
                    }
                } while (c.moveToNext());
            }
            assert c != null;
            c.close();
        } catch (Exception e) {
            Log.d(TAG, "deleteSMS: Could not delete SMS from inbox: " + e.getMessage());
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView address, unreadCount;
        public ImageView image;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.userImageAnswer);
            address = (TextView) view.findViewById(R.id.emailAnswer);
            unreadCount = (TextView) view.findViewById(R.id.unreadCount);

        }
    }
}
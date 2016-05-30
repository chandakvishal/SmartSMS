package com.example.vinay.sms.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vinay.sms.R;
import com.example.vinay.sms.Messaging.SMS;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.MyViewHolder> {

    private String TAG = InboxAdapter.class.getSimpleName();
    private List<SMS> smsList;
    private Activity activity;
    private Context ctx;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<SMS> SMSListToDelete = new ArrayList<>();

    public InboxAdapter(Activity activity, List<SMS> smsList) {
        this.smsList = smsList;
        ctx = activity.getApplicationContext();
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.inbox_rows, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd");
        holder.time.setText(ft.format(Long.parseLong(smsList.get(position).date)));
        String message = String.valueOf(smsList.get(position).message);
        holder.message.setText(message);
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView message, time;
        private FrameLayout frameLayout;

        public MyViewHolder(View view) {
            super(view);
            time = (TextView) view.findViewById(R.id.timeStamp);
            message = (TextView) view.findViewById(R.id.messageBody);
            frameLayout = (FrameLayout) activity.findViewById(R.id.main_fragment);

            view.setOnLongClickListener(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean onLongClick(final View view) {

            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.menu_question);

            // Force icons to show
            Object menuHelper;
            Class[] argTypes;
            try {
                Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                fMenuHelper.setAccessible(true);
                menuHelper = fMenuHelper.get(popupMenu);
                argTypes = new Class[]{boolean.class};
                menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
            } catch (Exception e) {
                Log.w("TAG", "error forcing menu icons to show", e);
                popupMenu.show();
                return false;
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.album_overflow_delete:
                            Toast.makeText(view.getContext(), "Position is " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                            return true;

                        case R.id.album_overflow_share:
                            Toast.makeText(view.getContext(), "Position is " + getAdapterPosition(), Toast.LENGTH_SHORT).show();

                            return true;

                        case R.id.album_overflow_fav:
                            Toast.makeText(view.getContext(), "Position is " + getAdapterPosition(), Toast.LENGTH_SHORT).show();

                            return true;

                        default:
                            return true;
                    }
                }
            });

            popupMenu.show();

            // Try to force some horizontal offset
            try {
                Field fListPopup = menuHelper.getClass().getDeclaredField("mPopup");
                fListPopup.setAccessible(true);
                Object listPopup = fListPopup.get(menuHelper);
                argTypes = new Class[]{int.class};
                Class listPopupClass = listPopup.getClass();

                //Blur background
                frameLayout.getForeground().setAlpha(100);

                // Invoke setHorizontalOffset() with the negative width to move left by that distance
                listPopupClass.getDeclaredMethod("setHorizontalOffset", argTypes).invoke(listPopup, 17);

                // Invoke show() to update the window's position
                listPopupClass.getDeclaredMethod("show").invoke(listPopup);
            } catch (Exception e) {
                // Again, an exception here indicates a programming error rather than an exceptional condition
                // at runtime
                Log.w(TAG, "Unable to force offset", e);
            }

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu popupMenu) {
                    frameLayout.getForeground().setAlpha(0);
                }
            });

            return true;
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
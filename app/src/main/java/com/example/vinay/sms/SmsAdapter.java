package com.example.vinay.sms;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.MyViewHolder> {

    private String TAG = SmsAdapter.class.getSimpleName();
    private List<SMS> questionList;
    private Activity activity;
    private Context ctx;

    private List<SMS> smsListToDelete = new ArrayList<>();

    public SmsAdapter(Activity activity, List<SMS> questionList) {
        this.questionList = questionList;
        ctx = activity.getApplicationContext();
        this.activity = activity;
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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView email;
        public ImageView image;
        private FrameLayout frameLayout;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.userImageAnswer);
            email = (TextView) view.findViewById(R.id.emailAnswer);
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
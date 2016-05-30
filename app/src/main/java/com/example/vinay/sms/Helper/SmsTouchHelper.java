package com.example.vinay.sms.Helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.vinay.sms.Adapter.SmsAdapter;

public class SmsTouchHelper extends ItemTouchHelper.SimpleCallback {
    private SmsAdapter smsAdapter;
    private RecyclerView recyclerView;

    public SmsTouchHelper(SmsAdapter smsAdapter, RecyclerView recyclerView) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.smsAdapter = smsAdapter;
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //TODO: Not implemented here
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //Remove item
        smsAdapter.onItemRemove(viewHolder, recyclerView);
    }
}
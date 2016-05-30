package com.example.vinay.sms.Helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.vinay.sms.Adapter.InboxAdapter;

public class InboxTouchHelper extends ItemTouchHelper.SimpleCallback {
    private InboxAdapter inboxAdapterAdapter;
    private RecyclerView recyclerView;

    public InboxTouchHelper(InboxAdapter messageAdapter, RecyclerView recyclerView) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.inboxAdapterAdapter = messageAdapter;
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
        inboxAdapterAdapter.onItemRemove(viewHolder, recyclerView);
    }
}
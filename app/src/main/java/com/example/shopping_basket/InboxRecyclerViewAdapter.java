package com.example.shopping_basket;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shopping_basket.databinding.FragmentInboxItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class InboxRecyclerViewAdapter extends RecyclerView.Adapter<InboxRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Notif> inbox;

    public InboxRecyclerViewAdapter(ArrayList<Notif> inbox) {
        this.inbox = inbox;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentInboxItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return inbox.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;

        public ViewHolder(FragmentInboxItemBinding binding) {
            super(binding.getRoot());
            mIdView = binding.inboxItemMessage;  // maps to @+id/inbox_item_message
            mContentView = binding.inboxItemTime; // maps to @+id/inbox_item_time
        }


        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}

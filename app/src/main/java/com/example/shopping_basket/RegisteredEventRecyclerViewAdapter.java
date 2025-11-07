package com.example.shopping_basket;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shopping_basket.databinding.RegisteredEventItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class RegisteredEventRecyclerViewAdapter extends RecyclerView.Adapter<RegisteredEventRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Event> registeredEvents;

    public RegisteredEventRecyclerViewAdapter(ArrayList<Event> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(RegisteredEventItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return registeredEvents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;

        public ViewHolder(RegisteredEventItemBinding binding) {
            super(binding.getRoot());
            mIdView = binding.registeredEventName;
            mContentView = binding.registeredEventTime;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
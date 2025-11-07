package com.example.shopping_basket;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shopping_basket.databinding.EntrantItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class EntrantListRecyclerViewAdapter extends RecyclerView.Adapter<EntrantListRecyclerViewAdapter.ViewHolder> {

    private List<Profile> entrants;

    public EntrantListRecyclerViewAdapter(ArrayList<Profile> entrants) {
        this.entrants = entrants;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(EntrantItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String entrantName = holder.entrantName.getText().toString();
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView entrantName;

        public ViewHolder(EntrantItemBinding binding) {
            super(binding.getRoot());
            entrantName = binding.entrantName;
        }

        @Override
        public String toString() {
            return super.toString() + "'";
        }
    }
}

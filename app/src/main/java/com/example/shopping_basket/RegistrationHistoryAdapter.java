package com.example.shopping_basket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegistrationHistoryAdapter
        extends RecyclerView.Adapter<RegistrationHistoryAdapter.ViewHolder> {

    private final List<RegistrationHistoryItem> items;

    public RegistrationHistoryAdapter(List<RegistrationHistoryItem> items) {
        this.items = items != null ? items : new ArrayList<RegistrationHistoryItem>();
    }

    public void setItems(List<RegistrationHistoryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registration_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {
        RegistrationHistoryItem item = items.get(position);

        holder.titleText.setText(
                item.getEventTitle() != null ? item.getEventTitle() : "Unknown Event"
        );

        String status = item.getStatus();
        if (status == null) status = "unknown";
        holder.statusText.setText(status);

        Timestamp ts = item.getTimestamp();
        if (ts != null) {
            Date date = ts.toDate();
            String formatted =
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(date);
            holder.timestampText.setText(formatted);
        } else {
            holder.timestampText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView titleText;
        final TextView statusText;
        final TextView timestampText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_event_title);
            statusText = itemView.findViewById(R.id.text_registration_status);
            timestampText = itemView.findViewById(R.id.text_registration_timestamp);
        }
    }
}

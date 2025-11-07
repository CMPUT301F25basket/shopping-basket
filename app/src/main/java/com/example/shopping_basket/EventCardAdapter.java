package com.example.shopping_basket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.ViewHolder> {
    private ArrayList<Event> events;

    public EventCardAdapter(ArrayList<Event> events) {
        this.events = events;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventCardPoster;
        private TextView eventCardName;
        private TextView eventCardDate;
        private TextView eventCardDuration;
        private TextView eventCardStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventCardPoster = itemView.findViewById(R.id.event_card_poster);
            eventCardName = itemView.findViewById(R.id.event_card_name);
            eventCardDate = itemView.findViewById(R.id.event_card_registration_period);
            eventCardDuration = itemView.findViewById(R.id.event_card_time);
            eventCardStatus = itemView.findViewById(R.id.event_card_status);

            // TODO IMPLEMENTATION:
            itemView.setOnClickListener(v -> {
                // Navigate to event detail screen
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Event selectedEvent = ((EventCardAdapter)
                            ((RecyclerView) itemView.getParent()).getAdapter()).events.get(position);
                    android.widget.Toast.makeText(
                            itemView.getContext(),
                            "Clicked: " + selectedEvent.getName(),
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_overview_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event eventItem = events.get(position);

        holder.eventCardName.setText(eventItem.getName());

        // TODO IMPLEMENTATION:
        if (eventItem.getStartDate() != null && eventItem.getEndDate() != null) {
            java.text.SimpleDateFormat dateFormat =
                    new java.text.SimpleDateFormat("MMM/dd/yyyy", java.util.Locale.getDefault());
            java.text.SimpleDateFormat timeFormat =
                    new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());

            String dateText = dateFormat.format(eventItem.getStartDate().getTime()) + " - " +
                    dateFormat.format(eventItem.getEndDate().getTime());
            holder.eventCardDate.setText(dateText);

            String durationText = timeFormat.format(eventItem.getStartDate().getTime()) + " - " +
                    timeFormat.format(eventItem.getEndDate().getTime());
            holder.eventCardDuration.setText(durationText);


            long diffMillis = eventItem.getEndDate().getTime() - System.currentTimeMillis();
            long daysLeft = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMillis);

            daysLeft = Math.max(0, diffMillis / (24 * 60 * 60 * 1000));
            holder.eventCardStatus.setText("Closes in " + daysLeft + " days");
        } else {
            holder.eventCardDate.setText("Date TBD");
            holder.eventCardDuration.setText("");
            holder.eventCardStatus.setText("Status unknown");
        }
    }


    @Override
    public int getItemCount() {
        return events.size();
    }
}

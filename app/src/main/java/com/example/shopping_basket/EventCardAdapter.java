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
        private Button viewButton;
        private Button eventActionButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventCardPoster = itemView.findViewById(R.id.eventCardPoster);
            eventCardName = itemView.findViewById(R.id.eventCardName);
            eventCardDate = itemView.findViewById(R.id.eventCardDate);
            eventCardDuration = itemView.findViewById(R.id.eventCardDuration);
            eventCardStatus = itemView.findViewById(R.id.eventCardStatus);
            viewButton = itemView.findViewById(R.id.buttonView);
            eventActionButton = itemView.findViewById(R.id.buttonEventAction);

            // TODO: Set onClickListener here to navigate to the event detail page (not necessarily have to click "View" button.
            //       Might also remove "Join" button, since it's not necessary.
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
        // TODO: Implement the rest in accord to Event fields

    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}

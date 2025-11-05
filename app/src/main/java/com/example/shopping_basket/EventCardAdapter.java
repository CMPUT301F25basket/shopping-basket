package com.example.shopping_basket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
        Date eventDate = eventItem.getStartDate().getTime();
        String eventDateString = new SimpleDateFormat("MM/dd/yyyy").format(eventDate);
        String startDateTime = new SimpleDateFormat("HH:mm").format(eventDate); // NOTE: Figure out how to use AM/PM => Use private method
        Date endDate = eventItem.getEndDate().getTime();
        String endDateTime = new SimpleDateFormat("HH:mm").format(endDate);
        String eventDuration = "";
        holder.eventCardDate.setText(eventDateString);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}

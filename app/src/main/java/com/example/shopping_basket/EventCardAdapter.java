package com.example.shopping_basket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * RecyclerView.Adapter for displaying a list of Event objects in cards on the
 * Home screen.
 *
 * This version also supports showing an uploaded poster image on the card.
 * The poster image is stored as a Base64 string on the event document in
 * Firestore and provided to the adapter via the eventPosters map.
 */
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.EventViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final ArrayList<Event> events;
    private final Map<String, String> eventPosters;
    private OnItemClickListener listener;

    public EventCardAdapter(ArrayList<Event> events, Map<String, String> eventPosters) {
        this.events = events;
        this.eventPosters = eventPosters;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_overview_card, parent, false);
        return new EventViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventName.setText(event.getName() != null ? event.getName() : "Unnamed event");

        // Date
        if (event.getStartDate() != null) {
            holder.eventDate.setText(
                    CalendarUtils.dateFormatter(event.getStartDate(), "MM/dd/yyyy")
            );
        } else {
            holder.eventDate.setText("");
        }

        // Time
        if (event.getEventTime() != null) {
            holder.eventTime.setText(
                    CalendarUtils.dateFormatter(event.getEventTime(), "hh:mm a")
            );
        } else {
            holder.eventTime.setText("");
        }

        // Status: "Closes in X days" based on endDate
        if (event.getEndDate() != null) {
            Date end = event.getEndDate();
            Date now = new Date();
            long diff = end.getTime() - now.getTime();
            long days = (long) Math.ceil(diff / (1000.0 * 60 * 60 * 24));
            if (days > 0) {
                holder.eventStatus.setText("Closes in " + days + (days == 1 ? " day" : " days"));
            } else {
                holder.eventStatus.setText("Closed");
            }
        } else {
            holder.eventStatus.setText("");
        }

        // Poster image
        String posterBase64 = null;
        if (eventPosters != null && event.getEventId() != null) {
            posterBase64 = eventPosters.get(event.getEventId());
        }

        if (posterBase64 != null && !posterBase64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(posterBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    holder.eventPoster.setImageBitmap(bitmap);
                } else {
                    holder.eventPoster.setImageResource(R.drawable.image_placeholder);
                }
            } catch (IllegalArgumentException e) {
                // bad base64 → show placeholder
                holder.eventPoster.setImageResource(R.drawable.image_placeholder);
            }
        } else {
            // No poster for this event yet → placeholder
            holder.eventPoster.setImageResource(R.drawable.image_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView eventPoster;
        TextView eventName;
        TextView eventDate;
        TextView eventTime;
        TextView eventStatus;

        EventViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            eventPoster = itemView.findViewById(R.id.event_card_poster);
            eventName   = itemView.findViewById(R.id.event_card_name);
            eventDate   = itemView.findViewById(R.id.event_card_date);
            eventTime   = itemView.findViewById(R.id.event_card_time);
            eventStatus = itemView.findViewById(R.id.event_card_status);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}

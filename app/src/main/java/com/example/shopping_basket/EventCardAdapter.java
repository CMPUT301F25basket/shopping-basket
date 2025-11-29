package com.example.shopping_basket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * A {@link RecyclerView.Adapter} for displaying a list of Event objects in a card format.
 * <p>
 * This adapter is responsible for taking a list of events and populating a {@link RecyclerView}.
 * Each item is displayed as a card showing the event's name, registration period, and time,
 * and remaining registration time.
 * <p>
 * This adapter also allows navigation to EventDetailFragment, where details of an event can
 * be viewed.
 */
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.ViewHolder> {
    private ArrayList<Event> events;
    private OnItemClickListener onItemClickListener;

    /**
     * Constructs a new EventCardAdapter.
     *
     * @param events An ArrayList}of Event objects to be displayed.
     */
    public EventCardAdapter(ArrayList<Event> events) {
        this.events = events;
    }

    /**
     * Interface for receiving click events on items in the RecyclerView.
     */
    public interface OnItemClickListener {
        /**
         * Called when an item in the RecyclerView has been clicked.
         *
         * @param position The position of the clicked item in the adapter.
         */
        void onItemClick(int position);
    }

    /**
     * Registers a callback to be invoked when an item in this adapter has been clicked.
     *
     * @param listener The callback that will be executed.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * A {@link RecyclerView.ViewHolder} that describes an item view and data about its place
     * within the RecyclerView. It holds the UI components for a single event card.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventCardPoster;
        private TextView eventCardName;
        private TextView eventCardDate;
        private TextView eventCardTime;
        private TextView eventCardStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventCardPoster = itemView.findViewById(R.id.event_card_poster);
            eventCardName = itemView.findViewById(R.id.event_card_name);
            eventCardDate = itemView.findViewById(R.id.event_card_registration_period);
            eventCardTime = itemView.findViewById(R.id.event_card_time);
            eventCardStatus = itemView.findViewById(R.id.event_card_status);
        }
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_overview_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the ViewHolder to reflect the item at the
     * given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event eventItem = events.get(position);

        holder.eventCardName.setText(eventItem.getName());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });

        if (eventItem.getStartDate() != null && eventItem.getEndDate() != null) {
            java.text.SimpleDateFormat dateFormat =
                    new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault());
            java.text.SimpleDateFormat timeFormat =
                    new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());

            String dateText = dateFormat.format(eventItem.getStartDate().getTime()) + " - " +
                    dateFormat.format(eventItem.getEndDate().getTime());
            holder.eventCardDate.setText(dateText);
            String eventTimeText = CalendarUtils.dateFormatter(eventItem.getEventTime(), "MM/dd/yyyy hh:mm a");
            holder.eventCardTime.setText(eventTimeText);

            long diffMillis = eventItem.getEndDate().getTime() - System.currentTimeMillis();
            long daysLeft = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMillis);

            // Ensure daysLeft is not negative
            daysLeft = Math.max(0, daysLeft);
            holder.eventCardStatus.setText("Closes in " + daysLeft + " days");
        } else {
            holder.eventCardDate.setText("Date TBD");
            holder.eventCardTime.setText("");
            holder.eventCardStatus.setText("Status unknown");
        }
    }


    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }
}

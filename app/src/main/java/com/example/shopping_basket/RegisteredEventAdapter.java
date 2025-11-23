package com.example.shopping_basket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
/**
 * An {@link ArrayAdapter} for displaying a list of {@link Event} objects that a user has registered for.
 * <p>
 * This adapter is responsible for taking a list of events and populating a {@link android.widget.ListView},
 * where each item displays the event's name and its scheduled time.
 */
public class RegisteredEventAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> registeredEvents;
    private Context context;


    /**
     * Constructs a new {@code RegisteredEventAdapter}.
     * @param context          The current context, used to inflate the layout file.
     * @param registeredEvents A list of {@link Event} objects to be displayed.
     */
    public RegisteredEventAdapter(@NonNull Context context, @NonNull ArrayList<Event> registeredEvents) {
        super(context, 0, registeredEvents);
        this.context = context;
        this.registeredEvents = registeredEvents;
    }

    /**
     * Gets a {@link View} that displays the data at the specified position in the data set.
     * <p>
     * This method inflates the layout, populates the event name and time {@link TextView}
     * with data from the {@link Event} object, and returns the configured view for the list item.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible, for better performance.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.registered_event_item, parent, false);
        }
        Event event = getItem(position);
        TextView eventName = view.findViewById(R.id.registered_event_name);
        TextView eventTime = view.findViewById(R.id.registered_event_time);

        eventName.setText(event.getName());
        eventTime.setText(event.getEventTime());

        return view;
    }
}
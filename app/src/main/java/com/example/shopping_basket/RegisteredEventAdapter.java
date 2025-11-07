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
public class RegisteredEventAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> registeredEvents;
    private Context context;


    public RegisteredEventAdapter(@NonNull Context context, @NonNull ArrayList<Event> registeredEvents) {
        super(context, 0, registeredEvents);
        this.context = context;
        this.registeredEvents = registeredEvents;
    }

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
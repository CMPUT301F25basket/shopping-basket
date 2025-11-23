package com.example.shopping_basket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * An {@link ArrayAdapter} for displaying a list of {@link Notif} objects.
 * <p>
 * This adapter is responsible for taking a list of notifications and populating a {@link android.widget.ListView},
 * where each item displays the notification message and its timestamp.
 */
public class InboxAdapter extends ArrayAdapter<Notif> {
    private ArrayList<Notif> notifications;
    private Context context;

    /**
     * Constructs a new {@code InboxAdapter}.
     *
     * @param context The current context, used to inflate the layout file.
     * @param notifications A list of {@link Notif} objects to be displayed.
     */
    public InboxAdapter(@NonNull Context context, ArrayList<Notif> notifications) {
        super(context, 0, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    /**
     * Gets a {@link View} that displays the data at the specified position in the data set.
     * <p>
     * This method inflates the layout, populates the message and time {@link TextView} components
     * with data from the {@link Notif} object, and returns the configured view.
     *
     * @param position The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_inbox_item, parent, false);
        }
        Notif notif = getItem(position);
        TextView notificationMessage = view.findViewById(R.id.inbox_item_message);
        TextView notificationTime = view.findViewById(R.id.inbox_item_time);

        notificationMessage.setText(notif.getMessage());
        if (notif.getTime() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            String formattedDate = formatter.format(notif.getTime());
            notificationTime.setText(formattedDate);
        } else {
            notificationTime.setText("");
        }

        return view;
    }
}

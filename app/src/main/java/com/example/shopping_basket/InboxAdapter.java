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
import java.util.concurrent.TimeUnit;

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

        notificationMessage.setText(getFormattedTimestamp(notif));

        return view;
    }

    /**
     * Formats the timestamp of a {@link Notif} based on the current user mode (Admin or User).
     * <p>
     * In regular user mode, it displays a relative "time ago" format (e.g., "Sent 5 minutes ago", "Sent just now").
     * In admin mode, it displays an absolute date and time (e.g., "Sent on 08-28-2025 15:00").
     * If the notification time is null, it returns a fallback message.
     *
     * @param notif The {@link Notif} object containing the timestamp to format.
     * @return A formatted string representing the notification's sent time.
     */
    private String getFormattedTimestamp(Notif notif) {
        if (notif.getTime() == null) {
            return "Timestamp not available";
        }

        if (!ProfileManager.getInstance().isAdminMode()) {
            // Regular user mode: Display relative "time ago"
            long diffMillis = System.currentTimeMillis() - notif.getTime().getTime();
            long daysAgo = TimeUnit.MILLISECONDS.toDays(diffMillis);

            if (daysAgo >= 7) {
                return "Sent 7+ days ago";
            } else if (daysAgo >= 1) {
                return String.format(Locale.US, "Sent %d days ago", daysAgo);
            }

            long hoursAgo = TimeUnit.MILLISECONDS.toHours(diffMillis);
            if (hoursAgo >= 1) {
                return String.format(Locale.US, "Sent %d hours ago", hoursAgo);
            }

            long minutesAgo = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
            if (minutesAgo >= 1) {
                return String.format(Locale.US, "Sent %d minutes ago", minutesAgo);
            } else {
                return "Sent just now";
            }
        } else {
            // User Mode: Display absolute date and time
            String formattedDate = CalendarUtils.dateFormatter(notif.getTime(), "MM-dd-yyyy HH:mm");
            return String.format(Locale.US, "Sent on %s", formattedDate);
        }
    }
}

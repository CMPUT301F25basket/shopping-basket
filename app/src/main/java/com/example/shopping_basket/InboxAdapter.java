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

public class InboxAdapter extends ArrayAdapter<Notif> {
    private ArrayList<Notif> notifications;
    private Context context;

    public InboxAdapter(@NonNull Context context, ArrayList<Notif> notifications) {
        super(context, 0, notifications);
        this.context = context;
        this.notifications = notifications;
    }

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

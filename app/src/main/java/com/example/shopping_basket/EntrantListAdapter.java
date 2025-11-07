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
public class EntrantListAdapter extends ArrayAdapter<Profile> {

    private ArrayList<Profile> entrants;
    private Context context;

    public EntrantListAdapter(@NonNull Context context, ArrayList<Profile> entrants) {
        super(context, 0, entrants);
        this.context = context;
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.entrant_item, parent, false);
        }
        Profile profile = getItem(position);
        TextView entrantName = view.findViewById(R.id.entrant_name);

        entrantName.setText(profile.getName());

        return view;
    }
}

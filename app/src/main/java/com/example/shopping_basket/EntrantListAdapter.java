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
 * An {@link ArrayAdapter} for displaying a list of Profile objects, representing event entrants.
 * <p>
 * This class is responsible for taking a list of user profiles and populating a ListView,
 * where each item in the list displays the entrant's name.
 */
public class EntrantListAdapter extends ArrayAdapter<Profile> {

    private ArrayList<Profile> entrants;
    private Context context;


    /**
     * Constructs a new EntrantListAdapter.
     *
     * @param context The current context, used to inflate the layout file.
     * @param entrants A list of Profile objects to be displayed.
     */
    public EntrantListAdapter(@NonNull Context context, ArrayList<Profile> entrants) {
        super(context, 0, entrants);
        this.context = context;
        this.entrants = entrants;
    }

    /**
     * Gets a {@link View} that displays the data at the specified position in the data set.
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
            view = LayoutInflater.from(context).inflate(R.layout.entrant_item, parent, false);
        }
        Profile profile = getItem(position);
        TextView entrantName = view.findViewById(R.id.entrant_name);

        entrantName.setText(profile.getName());

        return view;
    }
}

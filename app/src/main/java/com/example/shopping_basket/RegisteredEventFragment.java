package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

// We no longer need FirebaseAuth here
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import kotlinx.serialization.Required;


/**
 * A {@link Fragment} that displays a list of events a user has registered for.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Retrieving the current logged-in user's {@link Profile} from the {@link ProfileManager}.</li>
 *     <li>Querying the Firestore "events" collection to find all events where the user's ID is in the waiting list.</li>
 *     <li>Displaying these events in a {@link ListView} using the {@link RegisteredEventAdapter}.</li>
 * </ul>
 */
public class RegisteredEventFragment extends Fragment {
    private static final String TAG = "RegisteredEventFragment";

    private ListView registeredEventsListView;
    private RegisteredEventAdapter adapter;
    private ArrayList<Event> registeredEventsList;
    private FirebaseFirestore db;
    private Profile currentUser;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public RegisteredEventFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        // Get the current user from the ProfileManager singleton
        currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        registeredEventsList = new ArrayList<>();
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registered_events, container, false);

        registeredEventsListView = view.findViewById(R.id.registered_events_list);

        adapter = new RegisteredEventAdapter(requireContext(), registeredEventsList);
        registeredEventsListView.setAdapter(adapter);

        return view;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * This method triggers the loading of the user's registered events.
     *
     * @param view The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadRegisteredEvents();
    }

    /**
     * Fetches events from Firestore where the current user's ID is present in the {@code waitingList} array.
     * Populates the list and notifies the adapter to refresh the UI.
     * Handles the case where no user is logged in.
     */
    private void loadRegisteredEvents() {
        // TODO: Implement
    }
}

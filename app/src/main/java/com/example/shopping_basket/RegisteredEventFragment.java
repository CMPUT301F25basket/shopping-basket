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


/**
 * A fragment representing a list of Items.
 */
public class RegisteredEventFragment extends Fragment {
    private static final String TAG = "RegisteredEventFragment";

    private ListView registeredEventsListView;
    private RegisteredEventAdapter adapter;
    private ArrayList<Event> registeredEventsList;
    private FirebaseFirestore db;
    private Profile currentUser; // Use the Profile object from the singleton

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadRegisteredEvents();
    }

    private void loadRegisteredEvents() {
        // Reserved for final product
    }
}

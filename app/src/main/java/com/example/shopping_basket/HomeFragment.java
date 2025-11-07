package com.example.shopping_basket;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EventCardAdapter eventAdapter;
    private ArrayList<Event> events = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventType Type of event to display.
     * @param userId Current user’s ID.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO IMPLEMENTATION: Rename and change types and number of parameters
    public static HomeFragment newInstance(String eventType, String userId) {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentHomeBinding.bind(view);

        eventAdapter = new EventCardAdapter(events);
        binding.eventCardList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.eventCardList.setAdapter(eventAdapter);
        loadEvents();

        eventAdapter.setOnItemClickListener(position -> {
            Event selectedEvent = events.get(position);
            navigateToEventDetail(selectedEvent);
        });
    }

    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    events.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        events.add(event);
                    }
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading events: " + e.getMessage());
                });
    }

    private void navigateToEventDetail(Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        findNavController(requireView()).navigate(R.id.action_homeFragment_to_eventDetailFragment, bundle);
    }
}

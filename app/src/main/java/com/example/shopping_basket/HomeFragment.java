package com.example.shopping_basket;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * A {@link Fragment} that serves as the main screen of the application, displaying a list of events.
 * <p>
 * Key responsibilities:
 * <ul>
 *     <li>Fetching a list of all {@link Event} objects from the "events" collection in Firestore.</li>
 *     <li>Displaying the events in a {@link androidx.recyclerview.widget.RecyclerView} using the {@link EventCardAdapter}.</li>
 *     <li>Handling clicks on individual event cards to navigate to the {@link EventDetailFragment} for that event.</li>
 * </ul>
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EventCardAdapter eventAdapter;
    private ArrayList<Event> events = new ArrayList<>();
    private Profile currentUser;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
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
    public static HomeFragment newInstance(String eventType, String userId) {
        return new HomeFragment();
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * This method initializes toolbar option, view binding, sets up the RecyclerView and its adapter,
     * loads event data, and configures the item click listener.
     *
     * @param view The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentHomeBinding.bind(view);
        this.currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        eventAdapter = new EventCardAdapter(events);
        binding.eventCardList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.eventCardList.setAdapter(eventAdapter);
        loadEvents();

        eventAdapter.setOnItemClickListener(position -> {
            Event selectedEvent = events.get(position);
            navigateToEventDetail(selectedEvent);
        });

        // Access the hosting activity's action bar and set the title
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Events");
        }

        // Add a menu provider to the fragment, making it responsible for its own toolbar items
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Make the admin button visible only if the current user is an admin
                if (currentUser.isAdmin())
                    menu.findItem(R.id.action_admin).setVisible(true);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_admin) {
                    findNavController(requireView()).navigate(R.id.action_homeFragment_to_adminMenuFragment);
                    return true;
                }
                return false;
            }
            // The menu's lifecycle is tied to the fragment's view, ensuring it's cleaned up automatically.
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Fetches the list of all events from the Firestore "events" collection.
     * On success, it clears the local event list, populates it with the new data,
     * and notifies the {@link EventCardAdapter} to refresh the UI.
     */
    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    events.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        // Only shows event where registration period is still in effect
                        if (event.getEndDate() != null && event.getEndDate().after(new Date()))
                            events.add(event);
                    }
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading events: " + e.getMessage());
                });
    }

    /**
     * Navigates to the appropriate detail screen based on event ownership.
     * If the current user is the event owner, it navigates to an editable screen.
     * Otherwise, it navigates to a read-only detail screen.
     *
     * @param event The {@link Event} object that was clicked by the user.
     */
    private void navigateToEventDetail(Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        if (event.getOwner() != null) {
            // Navigate to MyEventFragment if the current user is the event's owner
            if (Objects.equals(event.getOwner().getGuid(), currentUser.getGuid())) {
                findNavController(requireView()).navigate(R.id.action_homeFragment_to_myEventFragment, bundle);
                // Navigate to AdminEventDetailFragment if the current user is in admin mode (must be an admin first)
            } else if (ProfileManager.getInstance().isAdminMode()) {
                findNavController(requireView()).navigate(R.id.action_homeFragment_to_adminEventDetailFragment, bundle);
                // Else, navigate to event detail for registering
            } else {
                findNavController(requireView()).navigate(R.id.action_homeFragment_to_eventDetailFragment, bundle);
            }
        } else {
            Toast.makeText(getContext(), "Error loading event detail: no owner found", Toast.LENGTH_SHORT).show();
        }
    }
}

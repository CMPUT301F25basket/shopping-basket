package com.example.shopping_basket;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;

// We no longer need FirebaseAuth here
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * A {@link DialogFragment} that displays a list of events a user has registered for.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Retrieving the current logged-in user's {@link Profile} from the {@link ProfileManager}.</li>
 *     <li>Querying the Firestore "events" collection to find all events where the user's ID is in the waiting list.</li>
 *     <li>Displaying these events in a {@link ListView} using the {@link RegisteredEventAdapter}.</li>
 * </ul>
 */
public class RegisteredEventFragment extends DialogFragment {
    private static final String TAG = "RegisteredEventFragment";

    private ListView registeredEventsListView;
    private RegisteredEventAdapter adapter;
    private ArrayList<Event> registeredEventsList;
    private FirebaseFirestore db;
    private Profile currentUser;
    private LinearLayout emptyLayout;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /**
     * Called when the fragment's dialog is started.
     * Configures the dialog to be dismissable on an outside touch and sets its layout dimensions.
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true); // Dismiss when tapped outside
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bottom_rounded_bg);
        }
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

        emptyLayout = view.findViewById(R.id.empty_view);

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
     * Fetches events from Firestore where the user is registered, sorting them by most recent.
     * It queries the {@code enrollList}, {@code waitingList}, {@code inviteList}, and {@code cancelList} arrays.
     * Populates the list and notifies the adapter to refresh the UI.
     * This query requires a composite index in Firestore for each field being queried.
     */
    private void loadRegisteredEvents() {
        if (currentUser == null || currentUser.getGuid() == null || currentUser.getGuid().isEmpty()) {
            Log.w(TAG, "Cannot load registered events: current user or user ID is null.");
            updateUI(new ArrayList<>());
            return;
        }

        String userGuid = currentUser.getGuid();

        ArrayList<Event> foundEvents = new ArrayList<>();

        // List of fields to check for the user's GUID
        String[] registrationFields = {"waitingList", "inviteList", "enrollList", "cancelList"};
        int queryCount = registrationFields.length;
        int[] queriesFinished = {0};

        for (String field: registrationFields) {
            db.collection("events")
                    .whereArrayContains(field + ".guid", userGuid)
                    .orderBy("eventTime", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        queriesFinished[0]++;

                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Event event = document.toObject(Event.class);
                                // Prevent adding the same event multiple times if user is in multiple lists
                                if (!foundEvents.contains(event)) {
                                    foundEvents.add(event);
                                }
                            }
                        } else {
                            // Log the error if the task failed
                            Log.e(TAG, "Error getting events from " + field, task.getException());
                        }

                        // Check if this is the last query to finish
                        if (queriesFinished[0] == queryCount) {
                            // All queries are done, now sort the final combined list and update UI
                            foundEvents.sort((e1, e2) -> e2.getEventTime().compareTo(e1.getEventTime()));
                            updateUI(foundEvents);
                        }
                    });
        }
    }

    /**
     * Updates the UI by clearing the adapter and adding the new list of events.
     * It checks if the fragment is still attached to an activity before performing UI operations.
     * @param events The new list of events to display.
     */
    private void updateUI(ArrayList<Event> events) {
        // This check is crucial to prevent crashes if the user navigates away
        if (getActivity() == null) {
            return;
        }

        registeredEventsList.clear();
        registeredEventsList.addAll(events);
        adapter.notifyDataSetChanged();

        if (events.isEmpty()) {
            registeredEventsListView.setVisibility(GONE);
            emptyLayout.setVisibility(VISIBLE);
        } else {
            registeredEventsListView.setVisibility(VISIBLE);
            emptyLayout.setVisibility(GONE);
        }
    }
}

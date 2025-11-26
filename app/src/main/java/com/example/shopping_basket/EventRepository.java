package com.example.shopping_basket;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class EventRepository {
    private static final String TAG = "EventRepository";
    private static final String PROFILES_COLLECTION = "profiles";
    private static final String EVENTS_COLLECTION = "events";

    // Callback for a list of events
    public interface EventsCallback {
        void onCallback(ArrayList<Event> eventList);
    }

    // New callback for a single event, allowing for null if not found
    public interface SingleEventCallback {
        void onCallback(@Nullable Event event);
    }

    /**
     * Asynchronously fetches the single most recent event created by a specific user.
     * Orders events by their creation timestamp in descending order and returns the first one.
     *
     * @param userId   The ID of the owner of the event.
     * @param callback The callback that will be invoked with the latest event, or null if not found.
     */
    public static void getLatestEventByOwner(String userId, SingleEventCallback callback) {

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty. Cannot fetch latest event.");
            callback.onCallback(null);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(EVENTS_COLLECTION)
                .whereEqualTo("owner.guid", userId)
                .orderBy("creationTimestamp", Query.Direction.DESCENDING) // Newest first
                .limit(1) // Only get the latest event
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Get the first (and only) document
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        Event latestEvent = document.toObject(Event.class);
                        callback.onCallback(latestEvent);
                        Log.d(TAG, "Successfully loaded latest event: " + latestEvent.getName());
                    } else {
                        // Query succeeded but no events were found for this user
                        Log.d(TAG, "No events found for user: " + userId);
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching latest event for user: " + userId, e);
                    callback.onCallback(null);
                });
    }


    public static void getMyEvents(EventsCallback callback) {
        Profile currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser == null || currentUser.getGuid() == null || currentUser.getGuid().isEmpty()) {
            Log.e("Firestore", "User ID is null or empty. Cannot perform query.");
            // Return an OptionalInt.empty list via the callback to prevent null pointer exceptions
            callback.onCallback(new ArrayList<>());
            return;
        }

        db.collection(EVENTS_COLLECTION)
                .whereEqualTo("owner.guid", currentUser.getGuid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<Event> eventList = querySnapshot.toObjects(Event.class);
                            callback.onCallback(new ArrayList<>(eventList));
                        } else {
                            // Query was successful but returned no documents
                            Log.d(TAG, "No events found for user: " + currentUser.getGuid());
                            callback.onCallback(new ArrayList<>());
                        }
                    } else {
                        // The query failed
                        Log.e(TAG, "Error fetching events: ", task.getException());
                        callback.onCallback(new ArrayList<>());
                    }
                });


    }
}

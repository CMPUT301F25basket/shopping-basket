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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * A fragment representing a list of notifications.
 */
public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";
    private ListView notificationListView;
    private InboxAdapter inboxAdapter;
    private ArrayList<Notif> notifications;
    private FirebaseFirestore db;
    private Profile currentUser;

    public InboxFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        // Initialize Firebase services
        db = FirebaseFirestore.getInstance();
        // Initialize the data list
        notifications = new ArrayList<>();
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        notificationListView = view.findViewById(R.id.inbox_fragment);
        inboxAdapter = new InboxAdapter(requireContext(), notifications);
        notificationListView.setAdapter(inboxAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadNotifications();
    }

    // IMPORTANT: This method is reserved to be implemented in final product
    private void loadNotifications() {
//        if (currentUser == null) {
//            Log.w(TAG, "No user logged in. Cannot load notifications.");
//            Toast.makeText(getContext(), "Please log in to see your notifications.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String userId = currentUser.getGuid();
//
//        db.collection("notifications")
//                .whereEqualTo("target", userId)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Clear the old list before adding new data
//                        notifications.clear();
//
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            Notif notification = document.toObject(Notif.class);
//                            notifications.add(notification);
//                            Log.d(TAG, "Loaded notification: " + notification.getMessage());
//                        }
//                        inboxAdapter.notifyDataSetChanged();
//
//                        if (notifications.isEmpty()) {
//                            Log.d(TAG, "No notifications found for user: " + userId);
//                        }
//
//                    } else {
//                        Log.e(TAG, "Error getting notifications: ", task.getException());
//                        Toast.makeText(getContext(), "Failed to load notifications.", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }
}

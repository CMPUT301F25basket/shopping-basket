package com.example.shopping_basket;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentInboxBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * A fragment representing a list of notifications.
 *
 * Behaviour:
 * - Normal user: shows only notifications targeted at the current user.
 * - Admin mode (from Admin Menu): shows ALL notifications as a log.
 */
public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";

    private ListView notificationListView;
    private MenuProvider menuProvider;
    private InboxAdapter inboxAdapter;
    private ArrayList<Notif> notifications;
    private FirebaseFirestore db;
    private Profile currentUser;

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        db = FirebaseFirestore.getInstance();
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

        boolean adminMode = ProfileManager.getInstance().isAdminMode();

        // Set title depending on mode
        if (getActivity() != null
                && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity())
                    .getSupportActionBar()
                    .setTitle(adminMode ? "Admin – Notification Logs" : "Inbox");
        }

        loadNotifications();

        // Set visibility for the toggle notification button on toolbar
        setupMenu();
        // Only show the notification preference toggle for NORMAL users
        if (!adminMode) {
            requireActivity().addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu,
                                         @NonNull MenuInflater menuInflater) {
                    MenuItem notificationToggle =
                            menu.findItem(R.id.action_toggle_notification);
                    if (notificationToggle != null) {
                        notificationToggle.setVisible(true);
                        if (currentUser != null && currentUser.isNotificationPref()) {
                            notificationToggle.setIcon(R.drawable.bell_svgrepo_com);
                        } else {
                            notificationToggle.setIcon(R.drawable.bell_off_svgrepo_com);
                        }
                    }
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_toggle_notification
                            && currentUser != null) {
                        boolean currentPref = currentUser.isNotificationPref();
                        if (currentPref) {
                            menuItem.setIcon(R.drawable.bell_off_svgrepo_com);
                        } else {
                            menuItem.setIcon(R.drawable.bell_svgrepo_com);
                        }
                        currentUser.setNotificationPref(!currentPref);
                        return true;
                    }
                    return false;
                }
            }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        }
    }

    /**
     * Loads notifications from Firestore.
     * <p>
     * - In normal user mode: only notifications targeted at the current user.
     * - In admin mode: all notifications (log of everything sent).
     */
    private void loadNotifications() {
        if (db == null) {
            Log.e(TAG, "Firestore not initialised.");
            return;
        }

        final boolean adminMode = ProfileManager.getInstance().isAdminMode();

        if (!adminMode) {
            if (currentUser == null) {
                Log.w(TAG, "No user logged in. Cannot load notifications.");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Please log in to see your notifications.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        Query query = db.collection("notifications");
        final String userId;   // <-- final, assigned exactly once

        if (adminMode) {
            userId = null; // just for logging; not used in the query
            query = query.orderBy("time", Query.Direction.DESCENDING);
        } else {
            userId = currentUser.getGuid();
            query = query.whereEqualTo("target", userId).orderBy("time", Query.Direction.DESCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                notifications.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Notif notification = document.toObject(Notif.class);
                    notifications.add(notification);
                    Log.d(TAG, "Loaded notification: " + notification.getMessage());
                }
                inboxAdapter.notifyDataSetChanged();

                if (notifications.isEmpty()) {
                    if (adminMode) {
                        Log.d(TAG, "No notifications found in the system.");
                    } else {
                        Log.d(TAG, "No notifications found for user: " + userId);
                    }
                }
            } else {
                Log.e(TAG, "Error getting notifications: ", task.getException());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupMenu() {
        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem notificationToggle = menu.findItem(R.id.action_toggle_notification);
                if (notificationToggle != null) {
                    notificationToggle.setVisible(true);
                    if (currentUser.isNotificationPref()) notificationToggle.setIcon(R.drawable.bell_svgrepo_com);
                    else notificationToggle.setIcon(R.drawable.bell_off_svgrepo_com);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_toggle_notification) {
                    boolean currentPref = currentUser.isNotificationPref();
                    if (currentPref) {
                        menuItem.setIcon(R.drawable.bell_off_svgrepo_com);
                    } else {
                        menuItem.setIcon(R.drawable.bell_svgrepo_com);
                    }
                    currentUser.setNotificationPref(!currentPref);
                    return true;
                }
                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove the MenuProvider to prevent leaks and duplicate menus
        if (menuProvider != null) {
            requireActivity().removeMenuProvider(menuProvider);
            menuProvider = null;
        }
    }
}

package com.example.shopping_basket;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A fragment representing a list of notifications.
 */
public class InboxFragment extends Fragment {
    private ListView notificationListView;
    private InboxAdapter inboxAdapter;
    private ArrayList<Notif> notifications;
    private FirebaseFirestore db;

    public InboxFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase services
        db = FirebaseFirestore.getInstance();

        // Initialize the data list
        notifications = new ArrayList<>();
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        notificationListView = (ListView) view;
        inboxAdapter = new InboxAdapter(requireContext(), notifications);
        notificationListView.setAdapter(inboxAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadNotifications();
    }

    private void loadNotifications() {

    }
}

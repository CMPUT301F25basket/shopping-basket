package com.example.shopping_basket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private RegistrationHistoryAdapter adapter;
    private FirebaseFirestore db;

    public RegistrationHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When user presses Back on this screen, show Profile again and pop this fragment.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ProfileFragment profileDialog = new ProfileFragment();
                profileDialog.show(getParentFragmentManager(), "ProfileDialogFragment");
                NavHostFragment.findNavController(RegistrationHistoryFragment.this)
                        .popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_registration_history);
        progressBar = view.findViewById(R.id.progress_registration_history);
        emptyView = view.findViewById(R.id.text_registration_history_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RegistrationHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        Profile currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "No logged-in user. Cannot load registration history.",
                    Toast.LENGTH_LONG).show();
            showEmptyState(true);
            return;
        }

        String entrantId = currentUser.getGuid();  // Use guid as entrant id
        loadRegistrationHistory(entrantId);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean empty) {
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void loadRegistrationHistory(@NonNull String entrantId) {
        showLoading(true);

        CollectionReference registrationsRef = db.collection("registrations");

        registrationsRef
                .whereEqualTo("entrantId", entrantId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        adapter.setItems(new ArrayList<RegistrationHistoryItem>());
                        showEmptyState(true);
                        return;
                    }

                    List<DocumentSnapshot> registrationDocs = querySnapshot.getDocuments();
                    List<String> eventIds = new ArrayList<>();

                    for (DocumentSnapshot regDoc : registrationDocs) {
                        String eventId = regDoc.getString("eventId");
                        if (eventId != null) {
                            eventIds.add(eventId);
                        }
                    }

                    if (eventIds.isEmpty()) {
                        adapter.setItems(new ArrayList<RegistrationHistoryItem>());
                        showEmptyState(true);
                        return;
                    }

                    CollectionReference eventsRef = db.collection("events");
                    List<Task<DocumentSnapshot>> eventTasks = new ArrayList<>();
                    for (String eventId : eventIds) {
                        eventTasks.add(eventsRef.document(eventId).get());
                    }

                    Tasks.whenAllSuccess(eventTasks)
                            .addOnSuccessListener(results -> {
                                Map<String, DocumentSnapshot> eventMap = new HashMap<>();
                                for (Object result : results) {
                                    if (result instanceof DocumentSnapshot) {
                                        DocumentSnapshot eventDoc = (DocumentSnapshot) result;
                                        eventMap.put(eventDoc.getId(), eventDoc);
                                    }
                                }

                                List<RegistrationHistoryItem> items = new ArrayList<>();

                                for (DocumentSnapshot regDoc : registrationDocs) {
                                    String eventId = regDoc.getString("eventId");
                                    String status = regDoc.getString("status");
                                    Timestamp timestamp = regDoc.getTimestamp("timestamp");

                                    DocumentSnapshot eventDoc = eventMap.get(eventId);
                                    String title = "Unknown Event";
                                    if (eventDoc != null) {
                                        String t = eventDoc.getString("title");
                                        if (t != null) {
                                            title = t;
                                        }
                                    }

                                    RegistrationHistoryItem item =
                                            new RegistrationHistoryItem(
                                                    eventId,
                                                    title,
                                                    status,
                                                    timestamp
                                            );
                                    items.add(item);
                                }

                                adapter.setItems(items);
                                showEmptyState(items.isEmpty());
                            })
                            .addOnFailureListener(e -> {
                                showEmptyState(true);
                                Toast.makeText(requireContext(),
                                        "Failed to load events: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showEmptyState(true);
                    Toast.makeText(requireContext(),
                            "Failed to load registrations: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}

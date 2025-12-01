package com.example.shopping_basket;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminBrowseImagesFragment
 *
 * Admin screen that shows all event posters.
 * Posters are stored as fields inside the "events" documents:
 *  - hasPoster (boolean)
 *  - posterBase64 (string)
 *  - posterUploaderName (string)
 *
 * We query "events" where hasPoster == true, decode Base64, and display.
 */
public class AdminBrowseImagesFragment extends Fragment {

    private static final String TAG = "AdminBrowseImagesFrag";
    private static final String EVENTS_COLLECTION = "events";

    private RecyclerView recyclerView;
    private ImageRecyclerViewAdapter adapter;
    private final List<EventPoster> posters = new ArrayList<>();
    private FirebaseFirestore db;
    private MenuProvider menuProvider;   // for showing the Admin button

    public AdminBrowseImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);

        recyclerView = view.findViewById(R.id.admin_browse_images);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new ImageRecyclerViewAdapter(posters, this::confirmDeletePoster);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Browse Posters");
        }

        // Show the Admin toolbar button on this screen
        setupMenu();

        loadPosters();
    }

    /**
     * Makes the Admin button visible in the toolbar for this fragment
     * and navigates to the Admin menu when tapped.
     */
    private void setupMenu() {
        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Just make the admin button visible on this admin page
                MenuItem adminItem = menu.findItem(R.id.action_admin);
                if (adminItem != null) {
                    adminItem.setVisible(true);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_admin) {
                    // Go to the Admin menu fragment defined in bottom_nav_graph.xml
                    NavHostFragment.findNavController(AdminBrowseImagesFragment.this)
                            .navigate(R.id.adminMenuFragment);
                    return true;
                }
                return false;
            }
        };

        requireActivity().addMenuProvider(
                menuProvider,
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
    }

    /**
     * Loads posters directly from the "events" collection.
     * We only care about events where hasPoster == true.
     */
    private void loadPosters() {
        db.collection(EVENTS_COLLECTION)
                .whereEqualTo("hasPoster", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    posters.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventId = doc.getString("eventId");
                        if (eventId == null || eventId.isEmpty()) {
                            eventId = doc.getId(); // fallback
                        }

                        String name = doc.getString("name");
                        String base64 = doc.getString("posterBase64");
                        String uploaderName = doc.getString("posterUploaderName");

                        if (base64 == null || base64.isEmpty()) {
                            continue; // nothing to display
                        }

                        posters.add(new EventPoster(eventId, name, base64, uploaderName));
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading posters", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load posters.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeletePoster(EventPoster poster) {
        if (getContext() == null) return;

        String name = poster.getEventName() != null ? poster.getEventName() : "this event";

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete poster")
                .setMessage("Remove the poster for " + name + "?")
                .setPositiveButton("Delete", (dialog, which) -> deletePoster(poster))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Clears the poster fields on the related event document.
     */
    private void deletePoster(EventPoster poster) {
        if (db == null || getContext() == null) return;

        String eventId = poster.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid event ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("hasPoster", false);
        updates.put("posterBase64", FieldValue.delete());
        updates.put("posterUploaderId", FieldValue.delete());
        updates.put("posterUploaderName", FieldValue.delete());

        db.collection(EVENTS_COLLECTION).document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    posters.remove(poster);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Poster removed.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting poster", e);
                    Toast.makeText(getContext(), "Failed to remove poster.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the menu provider so we don't leak it
        if (menuProvider != null) {
            requireActivity().removeMenuProvider(menuProvider);
            menuProvider = null;
        }
    }
}
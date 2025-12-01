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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import androidx.navigation.fragment.NavHostFragment;


/**
 * AdminBrowseProfilesFragment
 *
 * Allows an administrator to browse all profiles stored in Firestore
 * and remove individual profiles when necessary.
 */
public class AdminBrowseProfilesFragment extends Fragment {

    private static final String TAG = "AdminBrowseProfiles";

    // Optional argument for column count – kept from the original template
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private RecyclerView recyclerView;
    private UserProfileRecyclerViewAdapter adapter;
    private final List<Profile> profiles = new ArrayList<>();
    private FirebaseFirestore db;
    private MenuProvider menuProvider;   // for showing the Admin button

    public AdminBrowseProfilesFragment() {
        // Required empty public constructor
    }

    public static AdminBrowseProfilesFragment newInstance(int columnCount) {
        AdminBrowseProfilesFragment fragment = new AdminBrowseProfilesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        // Root of this layout is a RecyclerView
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            adapter = new UserProfileRecyclerViewAdapter(profiles, this::confirmDeleteProfile);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Update the action bar title for clarity
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity())
                    .getSupportActionBar()
                    .setTitle("Admin \u2013 Profiles");
        }

        // Show the Admin toolbar button on this screen
        setupMenu();

        // Load all profiles from Firestore
        loadProfiles();
    }

    /**
     * Makes the Admin button visible in the toolbar for this fragment.
     * We only need to toggle visibility; click handling can stay global.
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
                    // TODO: replace adminFragment with your actual admin menu destination if different
                    NavHostFragment.findNavController(AdminBrowseProfilesFragment.this)
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
     * Fetches all profiles from the "profiles" collection and displays them.
     */
    private void loadProfiles() {
        db.collection("profiles").get().addOnSuccessListener(queryDocumentSnapshots -> {
            profiles.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Profile profile = document.toObject(Profile.class);

                // Ensure the guid field is set even if it wasn't stored explicitly.
                if (profile.getGuid() == null || profile.getGuid().isEmpty()) {
                    profile.setGuid(document.getId());
                }

                profiles.add(profile);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading profiles", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Failed to load profiles. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a confirmation dialog before deleting a profile.
     */
    private void confirmDeleteProfile(Profile profile) {
        if (getContext() == null) {
            return;
        }

        // Safeguard: prevent an admin from deleting their own profile from here.
        Profile currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        if (currentUser != null && currentUser.getGuid().equals(profile.getGuid())) {
            Toast.makeText(getContext(), "You cannot delete your own profile from this screen.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete profile")
                .setMessage("Are you sure you want to delete " + profile.getName() + "'s profile?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile(profile))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the given profile document from Firestore and removes it from the list.
     * Implements US 03.02.01 – Remove profiles (admin).
     */
    private void deleteProfile(Profile profile) {
        if (db == null || getContext() == null) {
            return;
        }

        String guid = profile.getGuid();
        if (guid == null || guid.isEmpty()) {
            Toast.makeText(getContext(), "Unable to delete profile: missing user id.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("profiles").document(guid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    profiles.remove(profile);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Profile deleted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting profile", e);
                    Toast.makeText(getContext(), "Failed to delete profile. Please try again.", Toast.LENGTH_SHORT).show();
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

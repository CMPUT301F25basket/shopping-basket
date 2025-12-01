package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A fragment representing a list of Items.
 */
public class EntrantListFragment extends Fragment {
    // TODO: Back Button
    private Event event;
    private ArrayList<Profile> profiles;
    private EntrantListAdapter adapter;
    private String entrantListTitle;
    private MenuProvider menuProvider;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntrantListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){
            event = (Event) getArguments().getSerializable("event");
            entrantListTitle = (String) getArguments().getSerializable("list_type");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupMenu();

        switch (entrantListTitle) {
            case "All":
                profiles = event.getWaitingList();
                profiles.addAll(event.getInviteList());
                profiles.addAll(event.getEnrollList());
                profiles.addAll(event.getCancelList());
                break;
            case "Enrolled":
                profiles = event.getEnrollList();
                break;
            case "Invited":
                profiles = event.getInviteList();
                break;
            case "Waiting":
                profiles = event.getWaitingList();
                break;
            case "Cancelled":
                profiles = event.getCancelList();
                break;
        }
        adapter = new EntrantListAdapter(this.getContext(), profiles);

        ListView list = new ListView(this.getContext());
        list.findViewById(R.id.entrant_list);
        list.setAdapter(adapter);

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(entrantListTitle + " Entrants");
        }

        setupClickListeners();
    }

    private void setupMenu() {
        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if (Objects.equals(entrantListTitle, "Enrolled")) {
                    menu.findItem(R.id.action_export_csv).setVisible(true);
                }
                
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_export_csv) {
                    ExportEntrantListFragment dialog = ExportEntrantListFragment.newInstance(profiles, event);
                    dialog.show(getParentFragmentManager(), "ExportEntrantListFragment");
                    return true;
                }
                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void setupClickListeners() {

    }
}
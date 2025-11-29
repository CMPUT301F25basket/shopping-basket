package com.example.shopping_basket;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class EntrantListFragment extends Fragment {

    private Event event;
    private ArrayList<Profile> profiles;
    //private ArrayAdapter<Profile> adapter;
    private EntrantListAdapter adapter;
    private String entrantListTitle;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntrantListFragment() {
    }

    @SuppressWarnings("unused")
    public static EntrantListFragment newInstance(int columnCount) {
        EntrantListFragment fragment = new EntrantListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            event = (Event) getArguments().getSerializable("Event");
            entrantListTitle = (String) getArguments().getSerializable("List");
        }
//        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
//        }
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
        View view = inflater.inflate(R.layout.fragment_entrant_list, container, false);

        // Set the adapter
//        if (view instanceof RecyclerView) {
//            Context context = view.getContext();
//            RecyclerView recyclerView = (RecyclerView) view;
//            if (mColumnCount <= 1) {
//                recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            } else {
//                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
//            }
//        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        //adapter = new ArrayAdapter<>(this.getContext(), R.layout.entrant_item, profiles);

        ListView list = new ListView(this.getContext());
        list.findViewById(R.id.list);
        list.setAdapter(adapter);

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(entrantListTitle);
        }
    }
}
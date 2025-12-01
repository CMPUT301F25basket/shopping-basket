package com.example.shopping_basket;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentEntrantProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EntrantProfileFragment extends DialogFragment {

    private FragmentEntrantProfileBinding binding;
    private Profile entrantProfile;
    private Event event;

    public EntrantProfileFragment() {
        // Required empty public constructor
    }


    public static EntrantProfileFragment newInstance(Profile entrantProfile, Event event) {
        EntrantProfileFragment fragment = new EntrantProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("entrant", entrantProfile);
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entrantProfile = (Profile) getArguments().getSerializable("entrant");
            event = (Event) getArguments().getSerializable("event");
        }

        if (entrantProfile == null || event == null) {
            dismiss();
        }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEntrantProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();

        String entrantProfileTitleString = entrantProfile.getName() + " registered for this event.";
        binding.textViewEntrantProfileTitle.setText(entrantProfileTitleString);
    }

    private void setupClickListeners() {
        binding.buttonCancelRemoveEntrant.setOnClickListener(v -> dismiss());

        binding.buttonConfirmRemoveEntrant.setOnClickListener(v -> {
            event.decline(entrantProfile);
            updateFirestore();
            dismiss();
        });
    }

    private void updateFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .document(event.getEventId())
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    // Optionally send the result
                    Bundle result = new Bundle();
                    result.putBoolean("entrantRemoved", true);
                    getParentFragmentManager().setFragmentResult("entrantProfileResult", result);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to remove entrant", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
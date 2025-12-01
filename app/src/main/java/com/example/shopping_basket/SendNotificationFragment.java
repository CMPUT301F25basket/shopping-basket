package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentSendNotificationBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
// TODO: Document
public class SendNotificationFragment extends DialogFragment {

    private FragmentSendNotificationBinding binding;
    private Event event;
    private List<String> targetUserIds = new ArrayList<>();

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public SendNotificationFragment() {
        // Required empty public constructor
    }


    public static SendNotificationFragment newInstance(Event event) {
        SendNotificationFragment fragment = new SendNotificationFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
        // If the event is null, dismiss to prevent crashes.
        if (event == null) {
            dismiss();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSendNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
        }

        // These lists being empty imply the lottery has not been drawn, so disable all checkboxes except for "All entrants"
        if (event.getCancelList().isEmpty() || event.getWaitingList().isEmpty()) {
            binding.radioUnselectedEntrants.setEnabled(false);
        }

        if (event.getInviteList().isEmpty()) {
            binding.radioSelectedEntrants.setEnabled(false);
        }
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonCancelNotification.setOnClickListener(v -> dismiss());

        binding.radioAllEntrants.setOnClickListener(v -> {
            // TODO: Combine all other lists except CancelList
            targetUserIds = event.getWaitingList().stream().map(Profile::getGuid).collect(Collectors.toList());
        });

        binding.radioSelectedEntrants.setOnClickListener(v -> {
            targetUserIds = event.getInviteList().stream().map(Profile::getGuid).collect(Collectors.toList());
        });

        binding.radioUnselectedEntrants.setOnClickListener(v -> {
            targetUserIds = event.getWaitingList().stream().map(Profile::getGuid).collect(Collectors.toList());
        });

        binding.buttonSendNotification.setOnClickListener(v -> {
            String notifMessage = binding.editTextNotificationMessage.getText().toString();
            List<Notif> notifBatch = new ArrayList<>();
            for (String userId: targetUserIds)
                notifBatch.add(new Notif(userId, notifMessage));
            uploadToFirebase(notifBatch);
        });
    }

    private void uploadToFirebase(List<Notif> notifBatch) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Check if there is anything to send to prevent an empty batch commit
        if (notifBatch == null || notifBatch.isEmpty()) {
            Toast.makeText(getContext(), "No recipients registered or selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Loop through the list of notifications and add each one to the batch
        for (Notif notification : notifBatch) {
            DocumentReference notifRef = db.collection("notifications").document();
            batch.set(notifRef, notification);
        }

        // Commit the batch to Firestore.
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Notification sent successfully!", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "All notifications sent successfully.");
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to send notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firestore", "Error writing notification batch", e);
                });
    }
}
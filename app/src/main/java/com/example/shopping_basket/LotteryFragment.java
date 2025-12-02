package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentLotteryBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A DialogFragment that allows an event owner to run a lottery for their event.
 * It provides an interface for the owner to specify the number of entrants to select.
 *
 * This fragment handles the following logic:
 * 1.  Validating the number of winners entered by the user.
 * 2.  Calling the lottery logic within the {@link Event} object.
 * 3.  Sending notifications to both the selected (invited) and unselected (waitlisted) entrants.
 * 4.  Updating the event object in Firestore to finalize the lottery results.
 * 5.  Returning a result to the calling fragment (e.g., {@link MyEventFragment}) to signal that the UI should be refreshed.
 *
 * It ensures that database operations (sending notifications and updating the event) are chained to prevent data inconsistencies.
 */
public class LotteryFragment extends DialogFragment {
    private FragmentLotteryBinding binding;
    private Event event;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public LotteryFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of LotteryFragment, passing the event as an argument.
     *
     * @param event The event object to run the lottery on.
     * @return A new instance of LotteryFragment.
     */
    public static LotteryFragment newInstance(Event event) {
        LotteryFragment fragment = new LotteryFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
        // If the event is somehow null, dismiss to prevent crashes.
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
        binding = FragmentLotteryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
        }
        setupClickListeners();
    }

    /**
     * Sets up the OnClickListeners for the buttons in the fragment.
     * - The "Cancel" button dismisses the dialog.
     * - The "Commence Lottery" button validates the user input, runs the lottery logic,
     *   and initiates the process of sending notifications and updating Firestore.
     */
    public void setupClickListeners() {
        binding.buttonCancelLottery.setOnClickListener(v -> dismiss());

        binding.buttonCommenceLottery.setOnClickListener(v -> {
            String entrantNumberString = binding.editTextLotteryEntrantNumber.getText().toString().trim();
            if (validateInput(entrantNumberString)) {
                int entrantNumber = Integer.parseInt(entrantNumberString);
                event.setSelectNum(entrantNumber);
                ArrayList<Invite> newInvites = event.runLottery();
                sendNotifications(newInvites, event.getWaitingList());
            }
        });
    }

    /**
     * Prepares notification objects for both selected (newly invited) and unselected entrants.
     * It constructs the notification messages and then passes the batch of notifications
     * to the method responsible for Firestore updates.
     *
     * @param newInvites A list of Invite objects for the users who won the lottery.
     * @param unselectedEntrants A list of Profile objects for users who were on the waiting list but were not selected.
     */

    private void sendNotifications(ArrayList<Invite> newInvites, ArrayList<Profile> unselectedEntrants) {
        String selectedNotifMessage = "Congratulations! You have been chosen to enroll in " + event.getName();
        String unselectedNotifMessage = "You were not chosen to enroll in " + event.getName();

        List<Notif> notifBatch = new ArrayList<>();
        if (newInvites != null && !newInvites.isEmpty()) {
            for (Invite invite : newInvites) {
                notifBatch.add(new Notif(invite.getTarget(), selectedNotifMessage));
            }
        }

        if (unselectedEntrants != null && !unselectedEntrants.isEmpty()) {
            for (Profile unselected : unselectedEntrants) {
                notifBatch.add(new Notif(unselected.getGuid(), unselectedNotifMessage));
            }
        }

        updateFirestore(notifBatch);
    }

    /**
     * Handles the sequential, asynchronous updates to Firestore.
     * 1. It first commits a WriteBatch containing all the new notifications.
     * 2. On successful commit of the notifications, it proceeds to update the main Event object in Firestore
     *    to reflect the new state of its internal lists (inviteList, waitingList).
     * 3. It handles success and failure for both operations, ensuring the dialog is dismissed and
     *    appropriate feedback is shown to the user. It also sends a result back to the parent fragment upon full success.
     *
     * @param notifBatch A list of Notif objects to be uploaded to Firestore.
     */
    private void updateFirestore(List<Notif> notifBatch) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Add all notifications to the batch
        if (notifBatch != null && !notifBatch.isEmpty()) {
            for (Notif notification : notifBatch) {
                DocumentReference notifRef = db.collection("notifications").document();
                batch.set(notifRef, notification);
            }
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Notifications are sent.
                    Log.i("LotteryFragment", "Notification batch sent successfully.");

                    // Update the Event object in Firestore.
                    db.collection("events").document(event.getEventId())
                            .set(event)
                            .addOnSuccessListener(aVoid2 -> {
                                // STEP 3: SUCCESS. Event is updated.
                                Log.i("LotteryFragment", "Event updated successfully after lottery.");
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Lottery run and notifications sent!", Toast.LENGTH_SHORT).show();
                                }
                                // Send result back to the calling fragment.
                                Bundle result = new Bundle();
                                result.putBoolean("lotteryRun", true);
                                getParentFragmentManager().setFragmentResult("lotteryResult", result);

                                // STEP 4: Finally, dismiss the dialog.
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                // Event update failed. This is a partial failure state.
                                Log.e("LotteryFragment", "Notifications sent, but failed to update event.", e);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Critical error: Notifications sent, but event update failed.", Toast.LENGTH_LONG).show();
                                }
                                dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    // Initial notification batch failed. Nothing was sent, nothing was updated.
                    Log.e("LotteryFragment", "Failed to send notification batch. Halting operation.", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to send notifications. Please try again.", Toast.LENGTH_LONG).show();
                    }
                    dismiss();
                });
    }

    /**
     * Validates the user's input. Shows an error on the TextInputLayout if invalid.
     * @param input The text from the EditText.
     * @return true if the input is a valid positive number, false otherwise.
     */
    private boolean validateInput(String input) {
        TextInputLayout textInputLayout = binding.textInputLayoutLottery;

        if (input.isEmpty()) {
            textInputLayout.setError("Number cannot be empty.");
            return false;
        }

        try {
            int number = Integer.parseInt(input);
            if (number <= 0) {
                textInputLayout.setError("Number must be greater than zero.");
                return false;
            }
            if (number > event.getMaxReg() && event.getMaxReg() != 0) {
                textInputLayout.setError("Number must be smaller than the registration limit.");
                return false;
            };
        } catch (NumberFormatException e) {
            textInputLayout.setError("Please enter a valid number.");
            return false;
        }

        // Clear error if input is valid
        textInputLayout.setError(null);
        return true;
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Nullifies the binding object to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
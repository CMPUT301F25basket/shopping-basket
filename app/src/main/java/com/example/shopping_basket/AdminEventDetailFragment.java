package com.example.shopping_basket;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.shopping_basket.databinding.FragmentAdminEventDetailBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * AdminEventDetailFragment
 *
 * Read-only detail screen that lets an administrator:
 *  - See full information about an {@link Event}.
 *  - Review statistics about waiting, invited, enrolled and cancelled lists.
 *  - Delete the event from Firestore.
 *
 * This fragment is reached when an admin taps an event card while in admin mode
 * (see {@link HomeFragment *navigateToEventDetail(Event)}).
 */
public class AdminEventDetailFragment extends Fragment {

    private static final String TAG = "AdminEventDetail";

    private Event event;
    private FragmentAdminEventDetailBinding binding;

    public AdminEventDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method that bundles an {@link Event} into the fragment arguments.
     */
    public static AdminEventDetailFragment newInstance(Event event) {
        AdminEventDetailFragment fragment = new AdminEventDetailFragment();
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (event == null) {
            // Nothing to display – go back safely.
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        if (getActivity() != null
                && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity())
                    .getSupportActionBar()
                    .setTitle("Admin – Event Detail");
        }

        setupEventDetail();
        setupClickListeners();
    }

    /**
     * Populates the admin detail layout with information from the event.
     */
    private void setupEventDetail() {
        // Basic text fields
        binding.adminEventName.setText(event.getName());

        if (event.getDesc() != null) {
            binding.adminEventDescription.setText(event.getDesc());
        }

        if (event.getGuideline() != null) {
            binding.adminEventGuideline.setText(event.getGuideline());
        }

        // Date & time
        Date eventTime = event.getEventTime();
        if (eventTime != null) {
            binding.adminEventDate.setText(
                    CalendarUtils.dateFormatter(eventTime, "MM/dd/yyyy"));
            binding.detailEventTime.setText(
                    CalendarUtils.dateFormatter(eventTime, "hh:mm a"));
        }

        // Registration status text (e.g. "Closes in X days" / "Registration closed")
        renderRegistrationDuration();

        // Simple count of users on the waiting list
        String registrationCountText = String.format(
                Locale.US,
                "%d users have registered for this event",
                event.getWaitListSize());
        binding.adminRegistrationCount.setText(registrationCountText);

        // Extra summary for the admin: breakdown of each list
        String waitlistSummary = String.format(
                Locale.US,
                "Waiting: %d • Invited: %d • Enrolled: %d • Cancelled: %d",
                event.getWaitingList() != null ? event.getWaitingList().size() : 0,
                event.getInviteList() != null ? event.getInviteList().size() : 0,
                event.getEnrollList() != null ? event.getEnrollList().size() : 0,
                event.getCancelList() != null ? event.getCancelList().size() : 0
        );
        binding.detailWaitlistMessage.setText(waitlistSummary);

        // Poster/image loading is left as-is (placeholder) until poster storage is implemented.
    }

    /**
     * Calculates and displays the time left for registration and sets the status text colour.
     * Mirrors EventDetailFragment's behaviour but writes into the admin layout.
     */
    private void renderRegistrationDuration() {
        if (event.getEndDate() == null) {
            binding.adminEventStatus.setText("Registration period unknown");
            return;
        }

        long diffMillis = event.getEndDate().getTime() - System.currentTimeMillis();
        long daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis);

        String registrationStatusText;
        int statusColor;

        if (daysLeft > 1) {
            registrationStatusText = String.format(Locale.US,
                    "Closes in %d days", daysLeft);
            statusColor = getResources().getColor(R.color.oxford_blue, null);
        } else if (daysLeft == 1) {
            long hoursLeft = TimeUnit.MILLISECONDS.toHours(diffMillis);
            registrationStatusText = String.format(Locale.US,
                    "Closes in %d hours", hoursLeft);
            statusColor = getResources().getColor(R.color.error, null);
        } else { // daysLeft is 0 or negative
            registrationStatusText = "Registration closed";
            statusColor = getResources().getColor(R.color.error, null);
        }

        binding.adminEventStatus.setText(registrationStatusText);
        binding.adminEventStatus.setTextColor(statusColor);
    }

    /**
     * Wires up the Back and Delete buttons.
     */
    private void setupClickListeners() {
        binding.buttonAdminEventToHome.setOnClickListener(
                v -> NavHostFragment.findNavController(this).popBackStack());

        binding.buttonAdminDeleteEvent.setOnClickListener(
                v -> deleteEventFromFirestore());
    }

    /**
     * Removes this event document from the "events" collection in Firestore.
     * On success, shows a toast and returns to the previous screen.
     */
    private void deleteEventFromFirestore() {
        if (event == null || event.getEventId() == null || event.getEventId().isEmpty()) {
            Toast.makeText(getContext(),
                    "Unable to delete: missing event identifier.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("events").document(event.getEventId())
            .delete().addOnSuccessListener(aVoid -> {Log.d(TAG, "Event deleted by admin: " + event.getEventId());
            Toast.makeText(getContext(), "Event deleted successfully.", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();})
            .addOnFailureListener(e -> {Log.e(TAG, "Failed to delete event: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Failed to delete event. Please try again.", Toast.LENGTH_SHORT).show();});
    }
}
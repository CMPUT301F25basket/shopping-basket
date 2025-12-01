package com.example.shopping_basket;

import static android.view.View.GONE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentEventDetailBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A {@link Fragment} that displays detailed information about a specific event.
 * <p>
 * This fragment is responsible for showing all details of an event, such as its name,
 * description, registration period, and the number of registered users. It allows
 * a logged-in user to register for or unregister from the event.
 * <p>
 * Key features:
 * <ul>
 *     <li>Receives an Event object via fragment arguments using the factory method.</li>
 *     <li>Uses ProfileManager singleton to get the current user's Profile and determine their registration status.</li>
 *     <li>Dynamically updates a button to show "Register" or "Unregister" based on whether the user is on the event's waiting list.</li>
 *     <li>Handles the logic for joining or leaving an event by calling methods on the Event object itself.</li>
 *     <li>Provides a back button to navigate to the previous screen.</li>
 * </ul>
 */
public class EventDetailFragment extends Fragment {
    private Event event;
    private String eventId;
    private Profile profile;
    private FragmentEventDetailBinding binding;


    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public EventDetailFragment() {
        // Required empty public constructor
    }

    /**
     * A factory method to create a new instance of this fragment with a specific event.
     *
     * @param event The Event object to be displayed in the detail view.
     * @return A new instance of EventDetailFragment.
     */
    public static EventDetailFragment newInstance(Event event) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is first created.
     * Initializes the profile from ProfileManager and retrieves the
     * Event object from the fragment's arguments.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.profile = ProfileManager.getInstance().getCurrentUserProfile();
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
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
        // Inflate the layout for this fragment
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * This method binds the views, populates the UI with event details, and sets up click listeners.
     *
     * @param view The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Access the hosting activity's action bar and set the title
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Event Detail");
        }

        if (event == null) {
            // Navigate back if no event data is available.
            NavHostFragment.findNavController(this).popBackStack();
            // Toast.makeText(requireContext(), "Error: Event not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        setupEventDetail();
        setupClickListeners();
        updateRegisterButtonState();
    }

    /**
     * Populates the UI components with data from the Event object.
     */
    private void setupEventDetail() {
        // Basic info
        binding.detailEventName.setText(event.getName());
        if (event.getDesc() != null) binding.detailEventDescription.setText(event.getDesc());
        if (event.getGuideline() != null) binding.detailEventGuideline.setText(event.getGuideline());

        // Actual image content is logged as metadata in the "images" collection".
        binding.detailEventPoster.setImageResource(R.drawable.image_placeholder);

        // Date and time components
        binding.detailEventDate.setText(CalendarUtils.dateFormatter(event.getEventTime(),"MM/dd/yyyy"));
        binding.detailEventTime.setText(CalendarUtils.dateFormatter(event.getEventTime(),"hh:mm a"));

        // Registration status
        renderRegistrationDuration();
        String registrationCountText = String.format(Locale.US, "%d users have registered for this event", event.getWaitListSize());
        binding.detailRegistrationCount.setText(registrationCountText);
    }

    /**
     * Calculates and displays the time left for registration and sets the text color accordingly.
     */
    private void renderRegistrationDuration() {
        long diffMillis = event.getEndDate().getTime() - System.currentTimeMillis();
        long daysLeft = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMillis);

        String registrationStatusText;
        int statusColor;

        if (daysLeft > 1) {
            registrationStatusText = String.format(Locale.US, "Closes in %d days", daysLeft);
            statusColor = getResources().getColor(R.color.oxford_blue, null);
        } else if (daysLeft == 1) {
            long hoursLeft = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diffMillis);
            registrationStatusText = String.format(Locale.US, "Closes in %d hours", hoursLeft);
            statusColor = getResources().getColor(R.color.error, null);
        } else { // daysLeft is 0 or negative
            registrationStatusText = "Registration closed";
            statusColor = getResources().getColor(R.color.error, null);
        }

        binding.detailEventStatus.setText(registrationStatusText);
        binding.detailEventStatus.setTextColor(statusColor);
    }

    /**
     * Updates the register/enroll button state based on the date and user's status.
     */
    private void updateRegisterButtonState() {
        if (profile == null) {
            // No user is logged in, so disable the button and show a generic "Register" message.
            binding.buttonRegisterEvent.setEnabled(false);
            binding.buttonRegisterEvent.setText("Register");
            binding.buttonDecline.setVisibility(View.GONE); // Hide decline button
            binding.buttonDetailToHome.setVisibility(View.VISIBLE); // Ensure back button is visible
            return;
        }
        // Default state
        binding.buttonRegisterEvent.setEnabled(true);
        binding.buttonRegisterEvent.setVisibility(View.VISIBLE);
        binding.buttonDecline.setVisibility(View.GONE);
        binding.buttonDetailToHome.setVisibility(View.VISIBLE);

        // Check if the current user's profile is in the inviteList
        boolean isInvited = event.getInviteList().stream().anyMatch(p -> p.getGuid().equals(profile.getGuid()));
        boolean isRegistered = event.getWaitingList().stream().anyMatch(p -> p.getGuid().equals(profile.getGuid()));
        boolean isEnrolled = event.getEnrollList().stream().anyMatch(p -> p.getGuid().equals(profile.getGuid()));
        boolean isRegistrationOpen = event.getEndDate().after(new Date());

        if (isRegistrationOpen) {
            if (isRegistered) {
                // User is on the waiting list -> "Unregister"
                binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.error_bg));
                binding.buttonRegisterEvent.setText("Unregister");
            } else {
                // User can join the waiting list -> "Register"
                binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
                binding.buttonRegisterEvent.setText("Register");
            }
        } else { // Registration period is closed
            if (isInvited) {
                // User is invited to enroll. Show both "Enroll" and "Decline" buttons.
                binding.buttonRegisterEvent.setText("Enroll");
                binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));

                // Show the new decline button and hide the normal back button
                binding.buttonDecline.setVisibility(View.VISIBLE);
                binding.buttonDetailToHome.setVisibility(View.GONE);

            } else if (isEnrolled) {
                // User is already enrolled, action is final.
                binding.buttonRegisterEvent.setText("Enrolled");
                binding.buttonRegisterEvent.setEnabled(false);
            } else {
                // Not invited and not enrolled after registration closes. Hide action button.
                binding.buttonRegisterEvent.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets up click listeners for the back button and the register/unregister button.
     */
    private void setupClickListeners() {
        binding.buttonDetailToHome.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_eventDetailFragment_to_homeFragment);
        });
        binding.buttonRegisterEvent.setOnClickListener(v -> {
            String buttonText = binding.buttonRegisterEvent.getText().toString();
            switch (buttonText) {
                case "Register":
                    event.joinEvent(profile);
                    break;
                case "Unregister":
                    event.leaveEvent(profile);
                    break;
                case "Decline":
                    event.decline(profile);
                    break;
                case "Enroll":
                    event.enroll(profile);
                    break;
            }
            // Update Firestore's database
            updateEventParticipationCollections();
            // After the click, update the button's state and text
            updateRegisterButtonState();
            // Update TextView displaying the count of registered users
            String registrationCountText = String.format(Locale.US, "%d users have registered for this event", event.getWaitListSize());
            binding.detailRegistrationCount.setText(registrationCountText);
        });
    }

    /**
     * Updates the event document in Firestore with the current state of its lists.
     * This method extracts the GUIDs from the Profile objects in each list
     * and overwrites the corresponding arrays in the Firestore document.
     */
    private void updateEventParticipationCollections() {
        if (event == null || event.getEventId() == null) {
            Log.e("Firestore", "Event or Event ID is null. Cannot update.");
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> waitingListIds = event.getWaitingList().stream()
                .map(Profile::getGuid)
                .collect(Collectors.toList());


        // Convert ArrayList<Profile> to ArrayList<String> of GUIDs for Firestore
        List<String> enrollListIds = event.getEnrollList().stream()
                .map(Profile::getGuid)
                .collect(Collectors.toList());

        List<String> cancelListIds = event.getCancelList().stream()
                .map(Profile::getGuid)
                .collect(Collectors.toList());

        List<String> inviteListIds = event.getInviteList().stream()
                .map(Profile::getGuid)
                .collect(Collectors.toList());

        // Update the specific fields in the Firestore document
        db.collection("events").document(event.getEventId())
                .update(
                        "waitingList", waitingListIds,
                        "enrollList", enrollListIds,
                        "cancelList", cancelListIds,
                        "inviteList", inviteListIds
                )
                .addOnSuccessListener(aVoid -> Log.d("FirestoreUpdate", "Event lists updated successfully!"))
                .addOnFailureListener(e -> Log.e("FirestoreUpdate", "Error updating event lists", e));
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
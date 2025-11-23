package com.example.shopping_basket;

import static com.example.shopping_basket.CalendarUtils.dateFormatter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentEventDetailBinding;

import java.util.Date;
import java.util.Locale;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        if (event == null) {
            // Navigate back if no event data is available.
            NavHostFragment.findNavController(this).popBackStack();
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
        // TODO: set up eventPoster

        // Date and time components
        binding.detailEventDate.setText(event.getEventTime()); // TODO: Format
        binding.detailEventTime.setText(event.getEventTime()); // TODO: Format

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
            registrationStatusText = "Closes in 1 day";
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
    // TODO: Handle enrollment
    private void updateRegisterButtonState() {
        if (profile == null) {
            // No user is logged in, so disable the button and show a generic "Register" message.
            binding.buttonRegisterEvent.setEnabled(false);
            return;
        }
        binding.buttonRegisterEvent.setEnabled(true);

        if (event.getEndDate().after(new Date())) {
            if (event.getWaitingList().contains(profile)) {
                // User is registered, so show "Unregister" state
                binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.error_bg));
                binding.buttonRegisterEvent.setText("Unregister");
            } else {
                // User is not registered, so show "Register" state
                binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
                binding.buttonRegisterEvent.setText("Register");
            }
        } else {
            if (event.getEnrollList().contains(profile)) {
                // User has enrolled, and this action is final. Disable the button.
                binding.buttonRegisterEvent.setText("Enrolled");
                binding.buttonRegisterEvent.setEnabled(false);
            } else if (event.getInviteList().contains(profile.getGuid())) {
                binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.error_bg));
                binding.buttonRegisterEvent.setText("Decline");
            } else {
                binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.oxford_blue));
                binding.buttonRegisterEvent.setText("Enroll");
            }
        }
    }

    /**
     * Sets up click listeners for the back button and the register/unregister button.
     */
    private void setupClickListeners() {
        binding.buttonDetailToHome.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
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
            // After the click, update the button's state and text
            updateRegisterButtonState();
            // Update TextView displaying the count of registered users
            String registrationCountText = String.format(Locale.US, "%d users have registered for this event", event.getWaitListSize());
            binding.detailRegistrationCount.setText(registrationCountText);
        });
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
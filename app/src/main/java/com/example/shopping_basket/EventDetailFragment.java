package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentEventDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
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
        binding = FragmentEventDetailBinding.bind(view);
        setupEventDetail();
        setupClickListeners();
        updateRegisterButtonState();
    }

    /**
     * Formats a {@link Date} object into a "MM/dd/yyyy" string.
     * @param d The Date to format.
     * @return The formatted date string.
     */
    // TODO: Use CalendarUtils instead (and implement the method)
    private String dateFormatter(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return sdf.format(d.getTime());
    }

    /**
     * Populates the UI components with data from the Event object.
     */
    private void setupEventDetail() {
        binding.detailEventName.setText(event.getName());
        String startDate = dateFormatter(event.getStartDate());
        String endDate = dateFormatter(event.getEndDate());
        String registrationTime = startDate + " - " + endDate;
        binding.detailRegistrationTime.setText(registrationTime);
        binding.detailEventTime.setText(event.getEventTime());
        // TODO: Count remaining registration time for eventStatus, set up eventGuideline, eventPoster
        String registrationStatus = String.format(Locale.US, "%d users have registered for this event", event.getWaitListSize());
        binding.detailEventStatus.setText(registrationStatus);
        binding.detailEventDescription.setText(event.getDesc());
    }

    /**
     * Updates the state and appearance of the register/unregister button based on the
     * current user's registration status for this event.
     * If event period is over, options to enroll/quit being the event chosen entrants
     * is displayed instead.
     */
    // TODO: Handle enrollment
    private void updateRegisterButtonState() {
        if (profile == null) {
            // No user is logged in, so disable the button and show a generic "Register" message.
            binding.buttonRegisterEvent.setEnabled(false);
            return;
        }
        binding.buttonRegisterEvent.setEnabled(true);
        if (event.getWaitingList().contains(profile)) {
            // User is registered, so show "Unregister" state
            binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.error_bg));
            binding.buttonRegisterEvent.setText("Unregister");
        } else {
            // User is not registered, so show "Register" state
            binding.buttonRegisterEvent.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
            binding.buttonRegisterEvent.setText("Register");
        }
        // Update the count of registered users
        String registrationStatus = String.format(Locale.US, "%d users have registered for this event", event.getWaitListSize());
        binding.detailEventStatus.setText(registrationStatus);
    }

    /**
     * Sets up click listeners for the back button and the register/unregister button.
     */
    private void setupClickListeners() {
        binding.buttonDetailToHome.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });
        binding.buttonRegisterEvent.setOnClickListener(v -> {
            if (event.getWaitingList().contains(profile)) {
                // If user is in the list, leave the event
                event.leaveEvent(profile);
            } else {
                // Otherwise, join the event
                event.joinEvent(profile);
            }
            // After the click, update the button's state and text
            updateRegisterButtonState();
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
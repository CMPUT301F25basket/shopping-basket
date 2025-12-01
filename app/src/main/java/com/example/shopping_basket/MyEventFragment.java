package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentMyEventBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MyEventFragment extends Fragment {

    private FragmentMyEventBinding binding;
    private Event event;
    private MenuProvider menuProvider;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public MyEventFragment() {
        // Required empty public constructor
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
        binding = FragmentMyEventBinding.inflate(inflater, container, false);
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
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Event");
        }

        if (event == null) {
            // Navigate back if no event data is available.
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        setupMenu();

        if (event.getEventTime().after(new Date())) {
            binding.buttonUpdateEvent.setEnabled(false);
        }

        setupEventDetail();
        setupClickListeners();
        setupButtonsVisibility();
    }

    /**
     * Sets up and adds the menu provider to the activity.
     */
    private void setupMenu() {
        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.findItem(R.id.action_send).setVisible(true);
                menu.findItem(R.id.action_qr).setVisible(true);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_send) {
                    SendNotificationFragment dialog = SendNotificationFragment.newInstance(event);
                    Log.d("My Event Fragment", "Clicked Send button.");
                    dialog.show(getParentFragmentManager(), "SendNotificationFragment");
                    return true;
                }
                if (menuItem.getItemId() == R.id.action_qr) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", event);
                    NavHostFragment.findNavController(MyEventFragment.this).navigate(R.id.action_myEventFragment_to_eventQRFragment, bundle);
                    return true;
                }
                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Populates the UI components with data from the Event object.
     */
    private void setupEventDetail() {
        // Basic info
        binding.myEventName.setText(event.getName());
        if (event.getDesc() != null) binding.myEventDescription.setText(event.getDesc());
        if (event.getGuideline() != null) binding.myEventGuideline.setText(event.getGuideline());

        // Date and time
        binding.myEventDate.setText(CalendarUtils.dateFormatter(event.getEventTime(),"MM/dd/yyyy"));
        binding.myEventTime.setText(CalendarUtils.dateFormatter(event.getEventTime(),"hh:mm a"));

        // Registration status
        renderRegistrationDuration();
        String registrationCountText = String.format(Locale.US, "%d users have registered for this event", event.getWaitListSize());
        binding.myEventRegistrationCount.setText(registrationCountText);
    }

    /**
     *
     */
    private void setupButtonsVisibility() {
        boolean lotteryDrawn = ((event.getInviteList() != null && !event.getInviteList().isEmpty()) || !event.getEnrollList().isEmpty());

        if (lotteryDrawn) {
            binding.layoutPreLottery.setVisibility(View.GONE);
            binding.layoutPostLottery.setVisibility(View.VISIBLE);
        } else {
            binding.layoutPreLottery.setVisibility(View.VISIBLE);
            binding.layoutPostLottery.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up click listeners for all interactive buttons on the screen.
     */
    private void setupClickListeners() {
        // TODO: Implement navigation logic for each of these buttons.

        binding.buttonOpenLottery.setOnClickListener(v -> {
            LotteryFragment dialog = LotteryFragment.newInstance(event);
            dialog.show(getParentFragmentManager(), "LotteryFragment");
            setupButtonsVisibility();
        });

        binding.buttonToEnrolledEntrants.setOnClickListener(v -> {
            // This button navigates to a screen showing the enrolled list.
        });

        binding.buttonToSelectedEntrants.setOnClickListener(v -> {
            // This button navigates to a screen showing the selected entrants (inviteList).
            // Also let organizer see canceled entrants
        });

        binding.buttonToUnselectedEntrants.setOnClickListener(v -> {
            // This button navigates to a screen showing the unselected entrants.
            // Also let organizer filter the waiting list
        });

        binding.buttonUpdateEvent.setOnClickListener(v -> {

        });

        binding.buttonToRegisteredEntrants.setOnClickListener(v -> {

        });
    }

    /**
     * Calculates and displays the time left for registration and sets the text color accordingly.
     */
    private void renderRegistrationDuration() {
        long diffMillis = event.getEndDate().getTime() - System.currentTimeMillis();
        long daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis);

        String statusText;
        int statusColor = ContextCompat.getColor(requireContext(), R.color.oxford_blue); // Default color

        if (daysLeft > 1) {
            statusText = String.format(Locale.US, "Closes in %d days", daysLeft);
        } else if (daysLeft == 1) {
            statusText = "Closes in 1 day";
            statusColor = ContextCompat.getColor(requireContext(), R.color.error); // Urgent color
        } else {
            statusText = "Registration period closed";
            statusColor = ContextCompat.getColor(requireContext(), R.color.error); // Closed color
            // After registration closes, the lottery button should be enabled.
            binding.buttonOpenLottery.setEnabled(true);
        }

        binding.myEventStatus.setText(statusText);
        binding.myEventStatus.setTextColor(statusColor);

        // Disable the lottery button if the registration period is still active.
        if (daysLeft > 0) {
            binding.buttonOpenLottery.setEnabled(false);
        }
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Nullifies the binding object to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove the MenuProvider to prevent leaks and duplicate menus
        if (menuProvider != null) {
            requireActivity().removeMenuProvider(menuProvider);
            menuProvider = null;
        }
        binding = null;
    }
}
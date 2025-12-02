package com.example.shopping_basket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
     * Called immediately after onCreateView has returned.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Action bar title
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Event");
        }

        if (event == null) {
            // Navigate back if no event data is available.
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        getParentFragmentManager().setFragmentResultListener("lotteryResult", this, (requestKey, bundle) -> {
            boolean lotteryRun = bundle.getBoolean("lotteryRun");
            if (lotteryRun) {
                // The lottery was run successfully, refresh data and buttons' visibility
                refreshEventData();
            }
        });

        setupMenu();

        if (event.getEventTime().after(new Date())) {
            binding.buttonUpdateEvent.setEnabled(true);
        } else {
            binding.buttonUpdateEvent.setEnabled(false);
        }

        // Load the uploaded poster for this event (if any)
        loadPosterImage();

        setupEventDetail();
        setupClickListeners();
        setupButtonsVisibility();
    }

    /**
     * Fetches the latest event data from Firestore and re-populates the entire UI.
     * This is called after a significant change, like running the lottery.
     */
    private void refreshEventData() {
        if (event == null || event.getEventId() == null) {
            Log.e("MyEventFragment", "refreshEventData called with null event or eventId.");
            return; // Cannot refresh without an event ID
        }

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(event.getEventId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return; // Ensure fragment is still active

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Update the local event object with the fresh data from Firestore
                        this.event = documentSnapshot.toObject(Event.class);

                        if (this.event != null) {
                            // Now, re-run all UI setup methods to reflect the new data
                            setupEventDetail();
                            setupButtonsVisibility(); // This is the most important call here
                            // Optionally, notify the user
                            Toast.makeText(getContext(), "Event data has been updated.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("MyEventFragment", "Event document no longer exists.");
                        Toast.makeText(getContext(), "Could not find event.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e("MyEventFragment", "Failed to refresh event data.", e);
                    Toast.makeText(getContext(), "Failed to refresh event data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads the poster image for this event from Firestore and displays it
     * in the header ImageView. If no poster is present or decoding fails,
     * a placeholder image is shown instead.
     */
    private void loadPosterImage() {
        if (binding == null || event == null) {
            return;
        }

        String eventId = event.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            // We don't know which document to look up → show placeholder
            binding.myEventPoster.setImageResource(R.drawable.image_placeholder);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || binding == null) {
                        return; // Fragment is no longer attached
                    }

                    if (doc != null && doc.exists()) {
                        String posterBase64 = doc.getString("posterBase64");
                        if (posterBase64 != null && !posterBase64.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(posterBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (bitmap != null) {
                                    binding.myEventPoster.setImageBitmap(bitmap);
                                } else {
                                    binding.myEventPoster.setImageResource(R.drawable.image_placeholder);
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e("MyEventFragment", "Invalid Base64 poster data", e);
                                binding.myEventPoster.setImageResource(R.drawable.image_placeholder);
                            }
                        } else {
                            binding.myEventPoster.setImageResource(R.drawable.image_placeholder);
                        }
                    } else {
                        binding.myEventPoster.setImageResource(R.drawable.image_placeholder);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MyEventFragment", "Failed to load poster image", e);
                    if (isAdded() && binding != null) {
                        binding.myEventPoster.setImageResource(R.drawable.image_placeholder);
                    }
                });
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
    /**
     * Controls the visibility of pre- and post-lottery layouts,
     * and enables/disables entrant list buttons based on lottery status.
     */
    private void setupButtonsVisibility() {
        boolean lotteryDrawn = (event.getInviteList() != null && !event.getInviteList().isEmpty()) ||
                (event.getEnrollList() != null && !event.getEnrollList().isEmpty());

        if (lotteryDrawn) {
            // --- POST-LOTTERY STATE ---
            binding.layoutPreLottery.setVisibility(View.GONE);
            binding.layoutPostLottery.setVisibility(View.VISIBLE);

            // Enable all post-lottery buttons
            binding.buttonToEnrolledEntrants.setEnabled(true);
            binding.buttonToSelectedEntrants.setEnabled(true);
            binding.buttonToUnselectedEntrants.setEnabled(true);
            binding.buttonToCancelledEntrants.setEnabled(true);
        } else {
            // --- PRE-LOTTERY STATE ---
            binding.layoutPreLottery.setVisibility(View.VISIBLE);
            binding.layoutPostLottery.setVisibility(View.GONE);

            // Disable all post-lottery buttons but keep them visible
            binding.buttonToEnrolledEntrants.setEnabled(false);
            binding.buttonToSelectedEntrants.setEnabled(false);
            binding.buttonToUnselectedEntrants.setEnabled(false);
            binding.buttonToCancelledEntrants.setEnabled(false);
        }
    }

    /**
     * Sets up click listeners for all interactive buttons on the screen.
     */
    private void setupClickListeners() {
        binding.buttonUpdateEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);

            NavHostFragment.findNavController(MyEventFragment.this)
                    .navigate(R.id.action_myEventFragment_to_eventCreationFragment, bundle);
        });


        binding.buttonOpenLottery.setOnClickListener(v -> {
            LotteryFragment dialog = LotteryFragment.newInstance(event);
            dialog.show(getParentFragmentManager(), "LotteryFragment");
            setupButtonsVisibility();
        });

        binding.buttonToEnrolledEntrants.setOnClickListener(v -> {
            // This button navigates to a screen showing the enrolled list.
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            bundle.putSerializable("list_type", "Enrolled");
            NavHostFragment.findNavController(MyEventFragment.this).navigate(R.id.action_myEventFragment_to_entrantListFragment, bundle);
        });

        binding.buttonToSelectedEntrants.setOnClickListener(v -> {
            // This button navigates to a screen showing the selected entrants (inviteList).
            // Also let organizer see canceled entrants
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            bundle.putSerializable("list_type", "Invited");
            NavHostFragment.findNavController(MyEventFragment.this).navigate(R.id.action_myEventFragment_to_entrantListFragment, bundle);
        });

        binding.buttonToUnselectedEntrants.setOnClickListener(v -> {
            // This button navigates to a screen showing the unselected entrants.
            // Also let organizer filter the waiting list
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            bundle.putSerializable("list_type", "Waiting");
            NavHostFragment.findNavController(MyEventFragment.this).navigate(R.id.action_myEventFragment_to_entrantListFragment, bundle);
        });

        binding.buttonToCancelledEntrants.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            bundle.putSerializable("list_type", "Cancelled");
            NavHostFragment.findNavController(MyEventFragment.this).navigate(R.id.action_myEventFragment_to_entrantListFragment, bundle);
        });

        binding.buttonToRegisteredEntrants.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            bundle.putSerializable("list_type", "All");
            NavHostFragment.findNavController(MyEventFragment.this).navigate(R.id.action_myEventFragment_to_entrantListFragment, bundle);
        });
    }

    /**
     * Calculates and displays the time left for registration and sets the text color accordingly.
     */
    private void renderRegistrationDuration() {
        long diffMillis = event.getEndDate().getTime() - System.currentTimeMillis();
        long daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis);

        String statusText;
        int statusColor;
        boolean isRegistrationOver = diffMillis <= 0;

        if (isRegistrationOver) {
            statusText = "Registration period closed";
            statusColor = ContextCompat.getColor(requireContext(), R.color.error); // Closed color
        } else if (daysLeft < 1) { // This handles the last day (less than 24 hours left)
            statusText = "Closes in less than a day";
            statusColor = ContextCompat.getColor(requireContext(), R.color.error); // Urgent color
        } else if (daysLeft == 1) {
            statusText = "Closes in 1 day";
            statusColor = ContextCompat.getColor(requireContext(), R.color.error); // Urgent color
        } else {
            statusText = String.format(Locale.US, "Closes in %d days", daysLeft);
            statusColor = ContextCompat.getColor(requireContext(), R.color.oxford_blue); // Default color
        }

        binding.myEventStatus.setText(statusText);
        binding.myEventStatus.setTextColor(statusColor);

        binding.buttonOpenLottery.setEnabled(isRegistrationOver);
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
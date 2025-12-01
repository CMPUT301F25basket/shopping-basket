package com.example.shopping_basket;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.example.shopping_basket.databinding.FragmentEventCreationBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

/**
 * A {@link Fragment} that provides a user interface for creating new events.
 * <p>
 * This fragment contains a form for users to input event details such as name,
 * description, start/end dates for registration, event time, and an optional location.
 * It uses the CalendarUtils helper class to display date and time pickers.
 * <p>
 * Upon successful creation, the new Event object is saved to the "events"
 * collection in Firestore.
 */
public class EventCreationFragment extends Fragment {
    FragmentEventCreationBinding binding;
    Event event = null;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public EventCreationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventCreationFragment.
     */
    public static EventCreationFragment newInstance() {
        EventCreationFragment fragment = new EventCreationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

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
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned.
     * This method initializes view binding and sets up UI listeners.
     *
     * @param view The View returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentEventCreationBinding.bind(view);

        // Access the hosting activity's action bar and set the title
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Create Event");
        }

        setupCheckboxListener();
        setupClickListeners();
    }

    /**
     * Sets up a listener for the location checkbox to toggle the visibility
     * of the location input field.
     */
    private void setupCheckboxListener() {
        binding.checkboxRequireLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.textInputCreateLocation.setVisibility(View.VISIBLE);
                } else {
                    binding.textInputCreateLocation.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Sets up click listeners for all interactive elements on the screen,
     * including navigation, event creation, and date/time picker dialogs.
     */
    private void setupClickListeners() {
        // TODO: Only enable Create button when all required fields are filled in
        binding.buttonCreateToHome.setOnClickListener(v -> {
            NavigationUI.onNavDestinationSelected(
                    requireActivity().findViewById(R.id.home), // The menu item to select
                    findNavController(v)                       // The NavController to use
            );
        });

        binding.buttonCreateEvent.setOnClickListener(v -> {
            createEvent();
        });

        // Allow the organizer to upload poster metadata to the images collection.
        binding.buttonUploadPoster.setOnClickListener(v -> uploadPoster());

        binding.textInputCreateEventStart.setOnClickListener(v ->
                CalendarUtils.showDatePicker(requireContext(), binding.textInputCreateEventStart));
        binding.textInputCreateEventStart.setFocusable(false);

        binding.textInputCreateEventEnd.setOnClickListener(v ->
                CalendarUtils.showDatePicker(requireContext(), binding.textInputCreateEventEnd));
        binding.textInputCreateEventEnd.setFocusable(false);

        binding.textInputCreateEventTime.setOnClickListener(v -> {
            CalendarUtils.showDateTimePicker(requireContext(), binding.textInputCreateEventTime);
        });
        binding.textInputCreateEventTime.setFocusable(false);
    }

    /**
     * Gathers all user input from the form fields, validates it,
     * creates a new {@link Event} object, and triggers the upload process.
     */
    private void createEvent() {
        String eventName = binding.textInputCreateEventName.getText().toString().trim();
        String eventDesc = binding.textInputCreateEventDesc.getText().toString().trim();
        String eventGuideline = binding.textInputCreateEventGuideline.getText().toString().trim();
        String startDateStr = binding.textInputCreateEventStart.getText().toString().trim();
        String endDateStr = binding.textInputCreateEventEnd.getText().toString().trim();
        String eventTimeStr = binding.textInputCreateEventTime.getText().toString().trim();
        String location = binding.textInputCreateLocation.getText().toString().trim();
        String limitStr = binding.textInputCreateLimit.getText().toString().trim();
        int entrantLimit = limitStr.isEmpty() ? 0 : Integer.parseInt(limitStr);

        boolean requireLocation = binding.checkboxRequireLocation.isChecked();  // TODO: (Optionally) implement this

        Date startDate = CalendarUtils.stringToDate(startDateStr, "MM/dd/yyyy");
        Date endDate = CalendarUtils.stringToDate(endDateStr, "MM/dd/yyyy");
        Date eventTime = CalendarUtils.stringToDate(eventTimeStr, "MM/dd/yyyy HH:mm");
        Event event = new Event(ProfileManager.getInstance().getCurrentUserProfile(), eventName, eventDesc, eventGuideline, 0, entrantLimit, startDate, endDate, eventTime);
        uploadToFirebase(event);
    }

    /**
     * "Uploads" an event poster on behalf of the current organizer.
     *
     * For this project we do not store actual image files. Instead, we log a
     * GalleryImage entry in the Firestore "images" collection so that:
     *  - Admins can review and delete images (US 03.03.01 / 03.06.01).
     *  - The image document keeps track of who uploaded the poster.
     *
     * Each GalleryImage stores:
     *  - id          : document ID (imageId)
     *  - imageUrl    : unused / null (no Storage)
     *  - uploaderId  : GUID of the uploader
     *  - uploaderName: display name of the uploader
     *
     * This method is reused both when creating a brand-new event and when
     * editing an existing event (US 02.04.01 & US 02.04.02).
     */
    void uploadPoster() {
        Profile currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        if (currentUser == null) {
            Log.w("EventCreationFragment", "uploadPoster: no logged in user");
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "You must be logged in to upload a poster.", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create metadata entry for this poster.
        GalleryImage galleryImage = new GalleryImage();
        galleryImage.setImageUrl(null); // No real image storage in this project.
        galleryImage.setUploaderId(currentUser.getGuid());
        galleryImage.setUploaderName(currentUser.getName());

        FirebaseFirestore.getInstance().collection("images").add(galleryImage).addOnSuccessListener(documentReference -> {
            // Once created, store the generated id back into the document.
            String imageId = documentReference.getId();
            galleryImage.setId(imageId);
            documentReference.update("id", imageId);
            Log.d("EventCreationFragment", "Poster metadata uploaded with id: " + imageId);

            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Poster uploaded (placeholder image).", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {Log.e("EventCreationFragment", "Failed to upload poster metadata", e);
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Failed to upload poster. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the provided Event object to the "events" collection in Firestore.
     * On success, it chains a second operation to update the new document with
     * its generated ID and deep link URL. Finally, it navigates to the QR code screen.
     *
     * @param event The Event object to be uploaded.
     */
    private void uploadToFirebase(Event event) {
        FirebaseFirestore
                .getInstance()
                .collection("events")
                .add(event) // Step 1: Add the initial event document to Firestore
                .onSuccessTask(documentReference -> {
                    String eventId = documentReference.getId();
                    String eventURL = "shopping-basket://event/" + eventId;

                    event.setEventId(eventId);
                    event.setEventURL(eventURL);
                    // Step 2: Create and return the next task below: updating the document accordingly,
                    // The result is passed to addOnSuccessListener to navigate to the next fragment
                    return documentReference.update("eventId", eventId, "eventURL", eventURL);
                }).addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event created and updated with ID: " + event.getEventId());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", event);
                    findNavController(requireView()).navigate(R.id.action_eventCreationFragment_to_eventQRFragment, bundle);
                }).addOnFailureListener(e -> {
                    Log.e("Firestore", "Error during event creation or update: " + e.getMessage());
                });
    }

    /**
     *
     */
    private void populateEventData() {
        if (event == null) {
            return;
        }
        binding.textInputCreateEventName.setText(event.getName());
        binding.textInputCreateEventDesc.setText(event.getDesc() != null ? event.getDesc() : "No event description.");
        binding.textInputCreateEventGuideline.setText(event.getGuideline() != null ? event.getGuideline() : "No criteria or guidelines specified.");
        binding.textInputCreateEventStart.setText(CalendarUtils.dateFormatter(event.getStartDate(), "MM/dd/yyyy"));
        binding.textInputCreateEventEnd.setText(CalendarUtils.dateFormatter(event.getStartDate(), "MM/dd/yyyy"));
        binding.textInputCreateEventTime.setText(CalendarUtils.dateFormatter(event.getEventTime(), "MM/dd/yyyy HH:mm"));
        binding.textInputCreateLimit.setText(event.getMaxReg() > 0 ? Integer.toString(event.getMaxReg()) : ""); // If maxReg is set (>0), populate the view, otherwise don't
        // binding.checkboxRequireLocation.setChecked();
        binding.buttonCreateEvent.setText("Update");
    }
}
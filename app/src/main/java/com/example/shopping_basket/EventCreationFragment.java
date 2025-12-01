package com.example.shopping_basket;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.navigation.Navigation.findNavController;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.NavigationUI;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentEventCreationBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.firestore.SetOptions;


/**
 * Fragment for creating or editing an event.
 *
 * Poster workflow (NO Firebase Storage):
 *  - Organizer taps "Set event poster" and picks an image from device.
 *  - Image is compressed & encoded to Base64 and kept in local fields.
 *  - When event is saved/updated, we write these poster fields directly
 *    onto the event document in Firestore:
 *      hasPoster, posterBase64, posterUploaderId, posterUploaderName
 *
 * NOTE: Location is only used for UI here; Event model does not store it.
 */
public class EventCreationFragment extends Fragment {

    private static final String TAG = "EventCreationFragment";
    private static final String EVENTS_COLLECTION = "events";

    private FragmentEventCreationBinding binding;
    private Event event;

    private FirebaseFirestore db;

    // Poster data kept in memory until we save the event
    private String posterBase64;
    private String posterUploaderId;
    private String posterUploaderName;
    private boolean hasPoster = false;

    private ActivityResultLauncher<Intent> pickPosterLauncher;

    public EventCreationFragment() {
        // Required empty public constructor
    }

    public static EventCreationFragment newInstance() {
        return new EventCreationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        pickPosterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            handlePosterSelection(uri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentEventCreationBinding.bind(view);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            if (event == null) {
                activity.getSupportActionBar().setTitle("Create Event");
                binding.buttonCreateToHome.setVisibility(GONE);
            } else {
                activity.getSupportActionBar().setTitle("Edit Event");
                binding.buttonCreateToHome.setVisibility(VISIBLE);
            }
        }

        setupCheckboxListener();
        setupClickListeners();

        if (event != null) {
            populateEventData();
        }
    }

    private void setupCheckboxListener() {
        binding.checkboxRequireLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.textInputCreateLocation.setVisibility(VISIBLE);
            } else {
                binding.textInputCreateLocation.setVisibility(GONE);
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonCreateToHome.setOnClickListener(v ->
                        findNavController(v).popBackStack()
        );

        binding.buttonCreateEvent.setOnClickListener(v -> createEvent());

        // Pick an image from the device to be used as poster
        binding.buttonUploadPoster.setOnClickListener(v -> openImagePicker());

        binding.textInputCreateEventStart.setOnClickListener(v ->
                CalendarUtils.showDatePicker(requireContext(), binding.textInputCreateEventStart));
        binding.textInputCreateEventStart.setFocusable(false);

        binding.textInputCreateEventEnd.setOnClickListener(v ->
                CalendarUtils.showDatePicker(requireContext(), binding.textInputCreateEventEnd));
        binding.textInputCreateEventEnd.setFocusable(false);

        binding.textInputCreateEventTime.setOnClickListener(v ->
                CalendarUtils.showDateTimePicker(requireContext(), binding.textInputCreateEventTime));
        binding.textInputCreateEventTime.setFocusable(false);
    }

    // ----------------------------------------------------------------------
    // Poster selection (Base64, no Firebase Storage)
    // ----------------------------------------------------------------------

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickPosterLauncher.launch(Intent.createChooser(intent, "Select event poster"));
    }

    /**
     * Reads the selected image, compresses it, and stores as Base64
     * in the fragment's fields.
     */
    private void handlePosterSelection(@NonNull Uri imageUri) {
        try {
            InputStream stream = requireContext().getContentResolver().openInputStream(imageUri);
            if (stream == null) {
                showToast("Unable to read selected image.");
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();

            if (bitmap == null) {
                showToast("Unsupported image.");
                return;
            }

            // Resize to max 512px on longer side to keep Firestore doc small
            bitmap = scaleBitmap(bitmap, 512);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] data = baos.toByteArray();

            posterBase64 = Base64.encodeToString(data, Base64.DEFAULT);

            Profile currentUser = ProfileManager.getInstance().getCurrentUserProfile();
            if (currentUser != null) {
                posterUploaderId = currentUser.getGuid();
                posterUploaderName = currentUser.getName();
            } else {
                posterUploaderId = null;
                posterUploaderName = null;
            }

            hasPoster = true;

            showToast("Poster selected. It will be saved with the event.");
        } catch (Exception e) {
            Log.e(TAG, "Error handling poster selection", e);
            showToast("Failed to read image. Try a smaller one.");
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        if (scale >= 1.0f) {
            return bitmap; // already small enough
        }

        int newW = Math.round(width * scale);
        int newH = Math.round(height * scale);
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true);
    }

    // ----------------------------------------------------------------------
    // Event creation / update
    // ----------------------------------------------------------------------

    private void createEvent() {
        String eventName = binding.textInputCreateEventName.getText().toString().trim();
        String eventDesc = binding.textInputCreateEventDesc.getText().toString().trim();
        String eventGuideline = binding.textInputCreateEventGuideline.getText().toString().trim();
        String startDateStr = binding.textInputCreateEventStart.getText().toString().trim();
        String endDateStr = binding.textInputCreateEventEnd.getText().toString().trim();
        String eventTimeStr = binding.textInputCreateEventTime.getText().toString().trim();
        String limitStr = binding.textInputCreateLimit.getText().toString().trim();

        int entrantLimit = limitStr.isEmpty() ? 0 : Integer.parseInt(limitStr);

        Date startDate = CalendarUtils.stringToDate(startDateStr, "MM/dd/yyyy");
        Date endDate = CalendarUtils.stringToDate(endDateStr, "MM/dd/yyyy");
        Date eventTime = CalendarUtils.stringToDate(eventTimeStr, "MM/dd/yyyy HH:mm");

        if (event == null) {
            Profile owner = ProfileManager.getInstance().getCurrentUserProfile();
            event = new Event(
                    owner,
                    eventName,
                    eventDesc,
                    eventGuideline,
                    0,
                    entrantLimit,
                    startDate,
                    endDate,
                    eventTime
            );
            // Location is not stored on Event in this version
        } else {
            event.setName(eventName);
            event.setDesc(eventDesc);
            event.setGuideline(eventGuideline);
            event.setMaxReg(entrantLimit);
            event.setStartDate(startDate);
            event.setEndDate(endDate);
            event.setEventTime(eventTime);
        }

        uploadToFirebase(event);
    }

    private void uploadToFirebase(Event event) {

        if (event.getEventId() != null && !event.getEventId().isEmpty()) {
            // Update existing event document
            db.collection(EVENTS_COLLECTION)
                    .document(event.getEventId())
                    .set(event, SetOptions.merge())  // Keep existing poster field
                    .addOnSuccessListener(aVoid -> {

                        // Update poster fields if we have new one selected
                        updatePosterFieldsIfNeeded(event.getEventId());

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", event);
                        findNavController(requireView())
                                .navigate(R.id.action_eventCreationFragment_to_eventQRFragment, bundle);
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error updating event: " + e.getMessage(), e)
                    );
        } else {
            // New event: add, then update with ID, URL, and any poster fields
            db.collection(EVENTS_COLLECTION)
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        String eventId = documentReference.getId();
                        String eventURL = "shopping-basket://event/" + eventId;

                        event.setEventId(eventId);
                        event.setEventURL(eventURL);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("eventId", eventId);
                        updates.put("eventURL", eventURL);

                        if (hasPoster && posterBase64 != null && !posterBase64.isEmpty()) {
                            updates.put("hasPoster", true);
                            updates.put("posterBase64", posterBase64);
                            if (posterUploaderId != null) {
                                updates.put("posterUploaderId", posterUploaderId);
                            }
                            if (posterUploaderName != null) {
                                updates.put("posterUploaderName", posterUploaderName);
                            }
                        }

                        documentReference.update(updates)
                                .addOnSuccessListener(v -> {
                                    Log.d(TAG, "Event created and updated with ID: " + event.getEventId());
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("event", event);
                                    findNavController(requireView())
                                            .navigate(R.id.action_eventCreationFragment_to_eventQRFragment, bundle);
                                })
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error during event creation/update: " + e.getMessage(), e)
                                );
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error adding new event: " + e.getMessage(), e)
                    );
        }
    }

    /**
     * For existing events, we separately update the poster fields on the document.
     */
    private void updatePosterFieldsIfNeeded(String eventId) {
        if (!hasPoster || posterBase64 == null || posterBase64.isEmpty()) {
            return;
        }

        Map<String, Object> posterUpdates = new HashMap<>();
        posterUpdates.put("hasPoster", true);
        posterUpdates.put("posterBase64", posterBase64);
        if (posterUploaderId != null) {
            posterUpdates.put("posterUploaderId", posterUploaderId);
        }
        if (posterUploaderName != null) {
            posterUpdates.put("posterUploaderName", posterUploaderName);
        }

        db.collection(EVENTS_COLLECTION).document(eventId)
                .update(posterUpdates)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Poster fields updated for event " + eventId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update poster fields: " + e.getMessage(), e));
    }

    private void populateEventData() {
        if (event == null) {
            return;
        }

        binding.textInputCreateEventName.setText(event.getName());
        binding.textInputCreateEventDesc.setText(
                event.getDesc() != null ? event.getDesc() : "No event description."
        );
        binding.textInputCreateEventGuideline.setText(
                event.getGuideline() != null ? event.getGuideline() : "No criteria or guidelines specified."
        );

        if (event.getStartDate() != null) {
            binding.textInputCreateEventStart.setText(
                    CalendarUtils.dateFormatter(event.getStartDate(), "MM/dd/yyyy")
            );
        }
        if (event.getEndDate() != null) {
            binding.textInputCreateEventEnd.setText(
                    CalendarUtils.dateFormatter(event.getEndDate(), "MM/dd/yyyy")
            );
        }
        if (event.getEventTime() != null) {
            binding.textInputCreateEventTime.setText(
                    CalendarUtils.dateFormatter(event.getEventTime(), "MM/dd/yyyy HH:mm")
            );
        }

        if (event.getMaxReg() > 0) {
            binding.textInputCreateLimit.setText(
                    String.format(Locale.US, "%d", event.getMaxReg())
            );
        }

        // Location is not stored on Event; keep UI hidden by default
        binding.checkboxRequireLocation.setChecked(false);
        binding.textInputCreateLocation.setVisibility(GONE);

        binding.buttonCreateEvent.setText("Update");
    }

    private void showToast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
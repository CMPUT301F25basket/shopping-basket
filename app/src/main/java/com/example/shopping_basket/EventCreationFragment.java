package com.example.shopping_basket;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.example.shopping_basket.databinding.FragmentEventCreationBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventCreationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventCreationFragment extends Fragment {
    FragmentEventCreationBinding binding;
    public EventCreationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventCreationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventCreationFragment newInstance() {
        EventCreationFragment fragment = new EventCreationFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentEventCreationBinding.bind(view);
        setupCheckboxListener();
        setupClickListeners();
    }

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

    private void setupClickListeners() {
        binding.buttonCreateToHome.setOnClickListener(v -> {
            findNavController(v).navigate(R.id.homeFragment);
        });

        binding.buttonCreateEvent.setOnClickListener(v -> {
            createEvent();
        });

        binding.buttonUploadPoster.setEnabled(false);
//        binding.buttonUploadPoster.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                uploadPoster();
//            }
//        });

        binding.textInputCreateEventStart.setOnClickListener(v ->
                CalendarUtils.showDatePicker(requireContext(), binding.textInputCreateEventStart));
        binding.textInputCreateEventStart.setFocusable(false);

        binding.textInputCreateEventEnd.setOnClickListener(v ->
                CalendarUtils.showDatePicker(requireContext(), binding.textInputCreateEventEnd));
        binding.textInputCreateEventEnd.setFocusable(false);

        binding.textInputCreateEventTime.setOnClickListener(v -> {
            CalendarUtils.showDatePicker(requireContext(), binding.textInputCreateEventTime);
            CalendarUtils.showTimePicker(requireContext(), binding.textInputCreateEventTime);
        });
        binding.textInputCreateEventTime.setFocusable(false);
    }

    private void createEvent() {
        String eventName = binding.textInputCreateEventName.getText().toString().trim();
        String eventDesc = binding.textInputCreateEventDesc.getText().toString().trim();
        String startDateStr = binding.textInputCreateEventStart.getText().toString().trim();
        String endDateStr = binding.textInputCreateEventEnd.getText().toString().trim();
        String eventTimeStr = binding.textInputCreateEventTime.getText().toString().trim();
        String location = binding.textInputCreateLocation.getText().toString().trim();
        String limitStr = binding.textInputCreateLimit.getText().toString().trim();
        int entrantLimit = limitStr.isEmpty() ? 0 : Integer.parseInt(limitStr);

        boolean requireLocation = binding.checkboxRequireLocation.isChecked();

        Date startDate = CalendarUtils.stringToDate(startDateStr, "MM/dd/yyyy");
        Date endDate = CalendarUtils.stringToDate(endDateStr, "MM/dd/yyyy");

        Event event = new Event(null, eventName, eventDesc, 0, entrantLimit, startDate, endDate, eventTimeStr);
        uploadToFirebase(event);
    }

//    private void uploadPoster() {}

    private void uploadToFirebase(Event event) {
        FirebaseFirestore
                .getInstance()
                .collection("events")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String eventId = documentReference.getId();
                        Log.d("Firestore", "Event created with ID: " + eventId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error creating event: " + e.getMessage());
                    }
                });
    }
}
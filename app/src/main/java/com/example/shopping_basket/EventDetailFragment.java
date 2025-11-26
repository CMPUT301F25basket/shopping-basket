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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// TODO: Display event detail and (probably subclasses) for entrant/organizer operations
public class EventDetailFragment extends Fragment {
    private Event event;
    private Profile profile;
    private FragmentEventDetailBinding binding;

    public EventDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param event an event instance to be displayed.
     * @return A new instance of fragment EventDetailFragment.
     */
    public static EventDetailFragment newInstance(Event event) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Events")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String name = doc.getString("name");
                        String date = doc.getString("date");
                        System.out.println(name + " on " + date);
                    }
                })
                .addOnFailureListener(e -> System.err.println("Error loading events: " + e));

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

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

    private String dateFormatter(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return sdf.format(d.getTime());
    }

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

    private void updateRegisterButtonState() {
        if (profile == null) {
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

    private void setupClickListeners() {
        binding.buttonDetailToHome.setOnClickListener(v -> {
            assert getActivity() != null;
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.popBackStack();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
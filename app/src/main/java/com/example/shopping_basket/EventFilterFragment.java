package com.example.shopping_basket;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.shopping_basket.databinding.FragmentEventFilterBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventFilterFragment extends DialogFragment {

    private FragmentEventFilterBinding binding;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    // FilterCriteria inner class remains the same
    public static class FilterCriteria implements java.io.Serializable {
        public String keywords;
        public Date regStartDate;
        public Date regEndDate;
        public Date eventTimeDate;

        public FilterCriteria() {
            this.keywords = "";
        }

        public boolean isDefault() {
            return (keywords == null || keywords.isEmpty()) &&
                    regStartDate == null &&
                    regEndDate == null &&
                    eventTimeDate == null;
        }

        public boolean matches(Event event) {
            if (keywords != null && !keywords.isEmpty()) {
                String lowerKeywords = keywords.toLowerCase();
                boolean nameMatch = event.getName() != null && event.getName().toLowerCase().contains(lowerKeywords);
                boolean descMatch = event.getDesc() != null && event.getDesc().toLowerCase().contains(lowerKeywords);
                if (!nameMatch && !descMatch) {
                    return false;
                }
            }

            if (regStartDate != null && event.getStartDate() != null) {
                if (event.getStartDate().before(regStartDate)) {
                    return false;
                }
            }

            if (regEndDate != null && event.getEndDate() != null) {
                if (event.getEndDate().after(regEndDate)) {
                    return false;
                }
            }

            if (eventTimeDate != null && event.getEventTime() != null) {
                Calendar filterCal = Calendar.getInstance();
                filterCal.setTime(eventTimeDate);

                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(event.getEventTime());

                // Compare only the year, month, and day. Ignore time-of-day.
                boolean sameDay = filterCal.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                        filterCal.get(Calendar.MONTH) == eventCal.get(Calendar.MONTH) &&
                        filterCal.get(Calendar.DAY_OF_MONTH) == eventCal.get(Calendar.DAY_OF_MONTH);

                if (!sameDay) {
                    return false;
                }
            }
            return true;
        }
    }

    public EventFilterFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment's dialog is started.
     * Configures the dialog to be dismissable on an outside touch and sets its layout dimensions.
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true); // Dismiss when tapped outside
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bottom_rounded_bg);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentEventFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore filter criteria if they exist
        if (getArguments() != null && getArguments().containsKey("filterCriteria")) {
            FilterCriteria existing = (FilterCriteria) getArguments().getSerializable("filterCriteria");
            if (existing != null) {
                restoreFilterCriteria(existing);
            }
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Setup date pickers using the CalendarUtils class
        setupDatePicker(binding.editTextRegStart);
        setupDatePicker(binding.editTextRegEnd);
        setupDatePicker(binding.editTextEventTime);

        // Setup button listeners
        binding.buttonCancelFilter.setOnClickListener(v -> dismiss());
        binding.buttonApplyFilter.setOnClickListener(v -> {
            applyFilters();
            dismiss(); // Dismiss the dialog after applying filters
        });
    }

    /**
     * Sets up an EditText to show a DatePickerDialog on click, using CalendarUtils.
     */
    private void setupDatePicker(EditText editText) {
        editText.setFocusable(false); // Make it not editable by keyboard
        editText.setClickable(true);
        editText.setOnClickListener(v -> CalendarUtils.showDatePicker(requireContext(), editText));

        // Also add a long click listener to easily clear the date
        editText.setOnLongClickListener(v -> {
            editText.setText("");
            return true; // Consume the long click
        });
    }

    /**
     * Fills the UI fields with data from an existing FilterCriteria object.
     */
    private void restoreFilterCriteria(FilterCriteria criteria) {
        if (criteria.keywords != null) {
            binding.editTextEventKeywords.setText(criteria.keywords);
        }
        if (criteria.regStartDate != null) {
            binding.editTextRegStart.setText(dateFormat.format(criteria.regStartDate));
        }
        if (criteria.regEndDate != null) {
            binding.editTextRegEnd.setText(dateFormat.format(criteria.regEndDate));
        }
        if (criteria.eventTimeDate != null) {
            binding.editTextEventTime.setText(dateFormat.format(criteria.eventTimeDate));
        }
    }

    /**
     * Gathers data from UI, packages it, and sends it back to the calling fragment.
     */
    private void applyFilters() {
        FilterCriteria criteria = new FilterCriteria();

        // Use the binding object to get text from EditTexts
        criteria.keywords = binding.editTextEventKeywords.getText().toString().trim();

        // Use CalendarUtils.stringToDate for parsing
        criteria.regStartDate = CalendarUtils.stringToDate(binding.editTextRegStart.getText().toString(), "MM/dd/yyyy");
        criteria.regEndDate = CalendarUtils.stringToDate(binding.editTextRegEnd.getText().toString(), "MM/dd/yyyy");
        criteria.eventTimeDate = CalendarUtils.stringToDate(binding.editTextEventTime.getText().toString(), "MM/dd/yyyy");

        Bundle result = new Bundle();
        result.putSerializable("filterCriteria", criteria);

        // Send the result back to the parent fragment (HomeFragment)
        getParentFragmentManager().setFragmentResult("filterRequest", result);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify the binding object to avoid memory leaks
        binding = null;
    }
}

package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentLotteryBinding;
import com.google.android.material.textfield.TextInputLayout;

/**
 *
 */
public class LotteryFragment extends DialogFragment {
    private FragmentLotteryBinding binding;
    private Event event;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public LotteryFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of LotteryFragment, passing the event as an argument.
     *
     * @param event The event object to run the lottery on.
     * @return A new instance of LotteryFragment.
     */
    public static LotteryFragment newInstance(Event event) {
        LotteryFragment fragment = new LotteryFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
        // If the event is somehow null, dismiss to prevent crashes.
        if (event == null) {
            dismiss();
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
        binding = FragmentLotteryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
        }
        setupClickListeners();
    }

    public void setupClickListeners() {
        binding.buttonCancelLottery.setOnClickListener(v -> dismiss());

        binding.buttonCommenceLottery.setOnClickListener(v -> {
            String entrantNumberString = binding.editTextLotteryEntrantNumber.getText().toString().trim();
            if (validateInput(entrantNumberString)) {
                int entrantNumber = Integer.parseInt(entrantNumberString);
                event.setSelectNum(entrantNumber);
                event.runLottery();
                dismiss();
            }
        });
    }

    /**
     * Validates the user's input. Shows an error on the TextInputLayout if invalid.
     * @param input The text from the EditText.
     * @return true if the input is a valid positive number, false otherwise.
     */
    private boolean validateInput(String input) {
        TextInputLayout textInputLayout = binding.textInputLayoutLottery;

        if (input.isEmpty()) {
            textInputLayout.setError("Number cannot be empty.");
            return false;
        }

        try {
            int number = Integer.parseInt(input);
            if (number <= 0) {
                textInputLayout.setError("Number must be greater than zero.");
                return false;
            }
            if (number > event.getMaxReg()) {
                textInputLayout.setError("Number must be smaller than the registration limit.");
                return false;
            };
        } catch (NumberFormatException e) {
            textInputLayout.setError("Please enter a valid number.");
            return false;
        }

        // Clear error if input is valid
        textInputLayout.setError(null);
        return true;
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
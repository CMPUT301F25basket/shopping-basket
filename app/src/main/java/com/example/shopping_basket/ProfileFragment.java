package com.example.shopping_basket;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment; // --- CHANGE: Import DialogFragment

import com.example.shopping_basket.databinding.FragmentProfileBinding;

/**
 * A DialogFragment to display the current user's profile information.
 * It can be dismissed by tapping outside the dialog window.
 */
public class ProfileFragment extends DialogFragment { // --- CHANGE: Extend DialogFragment

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private Profile currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the current user's profile from the singleton
        currentUser = ProfileManager.getInstance().getCurrentUserProfile();

        getParentFragmentManager().setFragmentResultListener("profile-edited", this, (requestKey, result) -> {
            Log.d("ProfileFragment", "Received result from edit. Refreshing profile data.");

            currentUser = ProfileManager.getInstance().getCurrentUserProfile();

            setupProfileData();
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make the dialog dismissable when tapped outside and set dimensions ---
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser != null) {
            Log.d(TAG, "Displaying profile for user: " + currentUser.getName());
            setupProfileData();
            setupClickListeners();
        } else {
            // No user is logged in. Show a message and dismiss the dialog.
            Log.w(TAG, "No user profile found in ProfileManager.");
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_LONG).show();
            dismiss(); // Close the dialog
            navigateToLogin(); // Send user to login screen
        }
    }

    /**
     * Populates the UI with the current user's data.
     */
    private void setupProfileData() {
        binding.profileName.setText(currentUser.getName());
        binding.profileEmail.setText(currentUser.getEmail());

        String phone = currentUser.getPhone();
        if (phone != null && !phone.isEmpty()) {
            binding.profilePhone.setText(phone);
        } else {
            binding.profilePhone.setText("No phone number provided");
        }
    }

    /**
     * Sets up click listeners for the buttons in the dialog.
     */
    private void setupClickListeners() {
        binding.buttonEditProfile.setOnClickListener(v -> {
            dismiss();
            // Show the new EditProfileFragment dialog
            EditProfileFragment editProfileDialog = new EditProfileFragment();
            // Use getParentFragmentManager() to show a dialog from within another fragment
            editProfileDialog.show(getParentFragmentManager(), "EditProfileFragment");
        });

//        binding.buttonToRegistrationHistory.setOnClickListener(v -> {
//            dismiss();
//            RegisteredEventFragment registeredEventFragment = new RegisteredEventFragment();
//            registeredEventFragment.show(getParentFragmentManager(), "RegistrationHistoryFragment");
//        });
    }

    /**
     * Navigates to the LoginActivity and clears the navigation stack.
     */
    private void navigateToLogin() {
        if (getActivity() != null) {
            dismiss(); // Ensure the dialog is closed before navigating
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the binding reference to avoid memory leaks
        binding = null;
    }
}
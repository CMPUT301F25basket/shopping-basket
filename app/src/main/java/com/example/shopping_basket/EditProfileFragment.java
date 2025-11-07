package com.example.shopping_basket;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.shopping_basket.databinding.FragmentEditProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A DialogFragment for editing the user's profile (name and phone number).
 */
public class EditProfileFragment extends DialogFragment {

    private static final String TAG = "EditProfileFragment";
    private FragmentEditProfileBinding binding;
    private Profile currentUser;
    private FirebaseFirestore db;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
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
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true); // Dismiss when tapped outside
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser != null) {
            setupInitialData();
            setupClickListeners();
        } else {
            // Safeguard in case no user is logged in
            Toast.makeText(getContext(), "Error: No user found to edit.", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    /**
     * Pre-fills the input fields with the current user's data.
     * The email field is disabled to prevent users from changing their unique identifier.
     */
    private void setupInitialData() {
        binding.editTextProfileName.setText(currentUser.getName());
        binding.editTextProfileEmail.setText(currentUser.getEmail());
        binding.editTextProfilePhone.setText(currentUser.getPhone());

        // --- IMPORTANT: Disable the email field ---
        binding.editTextProfileEmail.setEnabled(false);
        binding.editTextProfileEmail.setFocusable(false);
    }

    /**
     * Sets up click listeners for the Confirm and Back buttons.
     */
    private void setupClickListeners() {
        binding.buttonConfirmEditProfile.setOnClickListener(v -> saveProfileChanges());
        binding.buttonCancelEditProfile.setOnClickListener(v -> dismiss()); // Just close the dialog
    }

    /**
     * Validates input and saves the updated profile data to Firestore.
     */
    private void saveProfileChanges() {
        String newName = binding.editTextProfileName.getText().toString().trim();
        String newPhone = binding.editTextProfilePhone.getText().toString().trim();

        if (newName.isEmpty()) {
            binding.editTextProfileName.setError("Name cannot be empty");
            return;
        }

        // --- Create a map to update only the changed fields in Firestore ---
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", newName);
        updatedData.put("phone", newPhone); // Phone can be empty

        String userId = currentUser.getGuid();

        db.collection("profiles").document(userId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile successfully updated in Firestore for user: " + userId);
                    currentUser.setName(newName);
                    currentUser.setPhone(newPhone);
                    ProfileManager.getInstance().setCurrentUserProfile(currentUser);

                    getParentFragmentManager().setFragmentResult("profile-edited", new Bundle());
                    Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    dismiss(); // Close the dialog on success
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating profile in Firestore", e);
                    Toast.makeText(getContext(), "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

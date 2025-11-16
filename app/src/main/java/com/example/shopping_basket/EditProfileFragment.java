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
 * A {@link DialogFragment} that provides a user interface
 * for updating the user's name and phone number.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Retrieves the current user's Profile from the ProfileManager singleton.</li>
 *     <li>Disables the email field to prevent modification of the (current) unique identifier.</li>
 *     <li>Validates user input before saving.</li>
 *     <li>Updates the user's profile data in the Firestore "profiles" collection.</li>
 *     <li>Updates the local Profile instance within the ProfileManager to ensure UI consistency across the app.</li>
 * </ul>
 *
 * Usage:
 * This fragment should be launched from another fragment (like ProfileFragment).
 * EditProfileFragment editDialog = new EditProfileFragment();
 * editDialog.show(getParentFragmentManager(), "EditProfileFragment");
 */
public class EditProfileFragment extends DialogFragment {

    private static final String TAG = "EditProfileFragment";
    private FragmentEditProfileBinding binding;
    private Profile currentUser;
    private FirebaseFirestore db;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public EditProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     * Initializes the {@code currentUser} from {@link ProfileManager} and the {@code db} Firestore instance.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ProfileManager.getInstance().getCurrentUserProfile();
        db = FirebaseFirestore.getInstance();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Overrides the default dialog creation to request a window without a title bar.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if this is a fresh fragment.
     * @return A new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
        }
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned.
     * This method setups the view by populating data and attaching click listeners.
     * If no user is logged in, it shows an error and dismisses itself.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
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
     * The email field is (currently) disabled to prevent users from changing their unique identifier.
     */
    private void setupInitialData() {
        binding.editTextProfileName.setText(currentUser.getName());
        binding.editTextProfileEmail.setText(currentUser.getEmail());
        binding.editTextProfilePhone.setText(currentUser.getPhone());

        // For security, disables the email field
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
     * Validates user input, then saves the updated profile data to Firestore.
     * On success, it updates the local ProfileManager instance, sends a fragment
     * result to notify listeners, and dismisses the dialog.
     */
    private void saveProfileChanges() {
        String newName = binding.editTextProfileName.getText().toString().trim();
        String newPhone = binding.editTextProfilePhone.getText().toString().trim();

        if (newName.isEmpty()) {
            binding.editTextProfileName.setError("Name cannot be empty");
            return;
        }

        // Create a map to update only the changed fields in Firestore
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

package com.example.shopping_basket;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentDeleteProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A {@link DialogFragment} that presents a confirmation dialog to the user
 * before deleting their profile. Upon confirmation, it handles the deletion
 * of the user's data from Firestore, clears the local session from
 * {@link ProfileManager}, and navigates the user to the {@link SignupActivity}
 * to create a new account.
 */
public class DeleteProfileFragment extends DialogFragment {

    private static final String TAG = "DeleteProfileFragment";
    private FragmentDeleteProfileBinding binding;
    private Profile currentUser;
    private FirebaseFirestore db;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public DeleteProfileFragment() {}


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDeleteProfileBinding.inflate(inflater, container, false);
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
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bottom_rounded_bg);
        }
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned,
     * but before any saved state has been restored in to the view.
     * This is where click listeners are set up.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentUser != null) {
            setupClickListeners();
        } else {
            // Safeguard in case no user is logged in
            Toast.makeText(getContext(), "Error: No user found to delete.", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    /**
     * Sets up click listeners for the Yes and No buttons.
     */
    private void setupClickListeners() {
        binding.buttonDeleteNegative.setOnClickListener(v -> dismiss());
        binding.buttonDeletePositive.setOnClickListener(v -> deleteProfile());
    }

    /**
     * Deletes the current user's profile from Firestore and clears the local session data.
     */
    private void deleteProfile() {
        String userId = currentUser.getGuid();
        // TODO: Delete all their active events
        db.collection("profiles").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile successfully deleted in Firestore for user: " + userId);
                    // Clear the user's profile from the singleton immediately after deletion.
                    ProfileManager.getInstance().setCurrentUserProfile(null);
                    Toast.makeText(getContext(), "Profile deleted.", Toast.LENGTH_SHORT).show();
                    navigateToSignup();
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting profile in Firestore", e);
                        Toast.makeText(getContext(), "Failed to delete profile. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Navigates to {@link SignupActivity} and clears the navigation stack.
     */
    private void navigateToSignup() {
        if (getActivity() != null) {
            dismiss(); // Ensure the dialog is closed before navigating
            Intent intent = new Intent(getActivity(), SignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
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
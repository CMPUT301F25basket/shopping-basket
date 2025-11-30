package com.example.shopping_basket;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;


import com.example.shopping_basket.databinding.FragmentProfileBinding;

/**
 * A {@link DialogFragment} that displays the current user's profile information.
 * <p>
 * This fragment shows the user's name, email, and phone number. It retrieves this
 * data from the {@link ProfileManager} singleton. It also provides a button to navigate
 * to the {@link EditProfileFragment}.
 * <p>
 * If no user is logged in, it automatically closes and redirects to the {@link LoginActivity}.
 * It also listens for results from {@code EditProfileFragment} to refresh the data automatically
 * after an edit.
 */
public class ProfileFragment extends DialogFragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private Profile currentUser;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     * Initializes the user profile and sets up a listener to refresh data
     * when the profile is edited.
     */
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
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Overrides the default dialog creation to request a window without a title bar.
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
     * Configures the dialog's appearance and allow cancelling
     * the dialog upon touching outside it.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Make the dialog dismissable when tapped outside and set dimensions ---
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bottom_rounded_bg);
        }
    }



    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned.
     * This method setups the view by populating data and setting up click listeners.
     * If no user is logged in, it shows an error and navigates to {@link SignupActivity}.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
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
            navigateToSignUp(); // Send user to signup screen
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
        // Existing edit profile button
        binding.buttonEditProfile.setOnClickListener(v -> {
            dismiss();
            EditProfileFragment editProfileDialog = new EditProfileFragment();
            editProfileDialog.show(getParentFragmentManager(), "EditProfileFragment");
        });

        // NEW: Registration History button
        binding.buttonToRegistrationHistory.setOnClickListener(v -> {
            // Close the profile dialog so the new screen is visible
            dismiss();

            // Get the NavController from the NavHostFragment in MainActivity
            NavHostFragment navHostFragment =
                    (NavHostFragment) requireActivity()
                            .getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_content_main);

            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.registrationHistoryFragment);
            }
        });

        // (Leave your other listeners here: logout, delete profile, etc.)

        binding.buttonDeleteProfile.setOnClickListener(v -> {
            dismiss();
            DeleteProfileFragment deleteProfileFragment = new DeleteProfileFragment();
            deleteProfileFragment.show(getParentFragmentManager(), "DeleteProfileFragment");
//            RegisteredEventFragment registeredEventFragment = new RegisteredEventFragment();
//            registeredEventFragment.show(getParentFragmentManager(), "RegistrationHistoryFragment");
        });
    }

    /**
     * Navigates to {@link SignupActivity} and clears the navigation stack.
     */
    private void navigateToSignUp() {
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
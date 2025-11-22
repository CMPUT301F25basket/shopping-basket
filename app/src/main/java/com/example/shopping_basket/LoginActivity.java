package com.example.shopping_basket;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * An {@link AppCompatActivity} that provides a user interface for signing in.
 * <p>
 * This activity implements the app's login mechanism. It verifies a user's existence by searching for a matching
 * email in the "profiles" collection in Firestore. The activity also provides a button to navigate to the
 * {@link SignupActivity} for new users.
 */
// NOTE: Most of the methods for login might not be used. Instead querying for device ID might be delegated to MainActivity
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI Elements
    private TextInputEditText editTextLoginEmail;
    private Button buttonLogin, buttonToSignup;
    private ProgressBar progressBarLogin;
    private boolean isAutoLoginAttempted = false;

    // Firebase
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes the views, Firebase instances, and sets up click listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        initializeViews();

        if (!isAutoLoginAttempted) {
            isAutoLoginAttempted = true;
            attemptAutoLogin();
        }

        // Setup button click listeners
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonToSignup.setOnClickListener(v -> {
            // Navigate to the SignupActivity
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Initializes all UI components from the layout file.
     */
    private void initializeViews() {
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonToSignup = findViewById(R.id.buttonToSignup);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    /**
     * Validates the user's input and initiates the login process by calling
     * {@link #findProfileByEmail(String)}.
     */
    private void attemptLogin() {
        String email = editTextLoginEmail.getText().toString().trim();

        if (email.isEmpty()) {
            editTextLoginEmail.setError("Email is required");
            editTextLoginEmail.requestFocus();
            return;
        }

        setLoading(true);
        findProfileByEmail(email);
    }

    /**
     * Attempts to automatically log in the user by finding a profile
     * that matches the current device's unique ID.
     */
    private void attemptAutoLogin() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId == null || androidId.isEmpty()) {
            setLoading(false);
            return;
        }
        db.collection("profiles")
                .whereEqualTo("deviceId", androidId)
                .limit(1) // We only expect one match
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Profile was found, get the first (and only) document
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Profile foundProfile = document.toObject(Profile.class);
                        Log.d(TAG, "Auto-login successful for device ID: " + androidId);
                        ProfileManager.getInstance().setCurrentUserProfile(foundProfile);
                        navigateToMain(foundProfile.getName());
                    }
                    else {
                        Log.d(TAG, "Auto-login failed: No profile found for this device.");
                        setLoading(false); // Hide the progress bar and show login buttons
                    }
                });
    }

    /**
     * Queries the "profiles" collection in Firestore for a document matching the provided email.
     * On success, it populates the {@link ProfileManager} and navigates to the main app.
     * On failure or if no user is found, it displays a toast message.
     *
     * @param email The email address to search for in the database.
     */
    private void findProfileByEmail(String email) {
        db.collection("profiles")
                .whereEqualTo("email", email)
                .limit(1) // We only expect one user per email
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Profile foundProfile = document.toObject(Profile.class);
                            Log.d(TAG, "Profile found for email: " + email);
                            // Set the found profile in the Singleton Manager
                            ProfileManager.getInstance().setCurrentUserProfile(foundProfile);

                            // Navigate to the main activity
                            navigateToMain(foundProfile.getName());
                            return; // Exit after finding the first match
                        }
                    }

                    // This block runs if the task fails or if no documents are found
                    Log.w(TAG, "Login failed. No profile found for email: " + email, task.getException());
                    Toast.makeText(LoginActivity.this, "Login failed. No account found with that email.", Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    /**
     * Navigates to the MainActivity after a successful login.
     * @param name The name of the logged-in user to display in a welcome message.
     */
    private void navigateToMain(String name) {
        Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the activity stack so the user can't go back to the login screen
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Toggles the loading indicator's visibility.
     * @param isLoading true to show the progress bar, false to hide it.
     */
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBarLogin.setVisibility(View.VISIBLE);
            buttonLogin.setVisibility(View.INVISIBLE);
        } else {
            progressBarLogin.setVisibility(View.GONE);
            buttonLogin.setVisibility(View.VISIBLE);
        }
    }
}


package com.example.shopping_basket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

/**
 * An {@link AppCompatActivity} that provides a user interface for new user registration.
 * <p>
 * This activity facilitates a signup process where an user is uniquely identified by their
 * emails, apart from required name and an optional phone number.
 * This activity also includes a check on startup to see if a user is already logged in via the
 * {@link ProfileManager}, in which case it redirects them directly to {@link MainActivity}.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    // UI Elements
    private TextInputEditText editTextName, editTextEmail, editTextPhone;
    private Button buttonSignup;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * <p>
     * It first checks if a user is already logged in (via the {@link ProfileManager})
     * and redirects to {@link MainActivity} if so. Otherwise, it initializes the UI,
     * Firebase, and sets up click listeners for the signup and login buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if a profile already exists in the singleton
        // If the user has already "logged in" during this session, go straight to MainActivity.
        if (ProfileManager.getInstance().isUserLoggedIn()) {
            Log.d(TAG, "Profile already exists in Singleton. Navigating to MainActivity.");
            navigateToMain();
            return; // IMPORTANT: Prevents the rest of the activity from loading
        }

        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        initializeViews();

        // Setup button click listener
        buttonSignup.setOnClickListener(v -> attemptProfileCreation());
        TextView buttonToLogin = findViewById(R.id.buttonToLogin);

        buttonToLogin.setOnClickListener(v -> {
            // Create an intent to navigate to the LoginActivity
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        });
    }

    /**
     * Initializes all UI components from the layout file and assigns them to member variables.
     */
    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSignup = findViewById(R.id.buttonSignup);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Validates input and starts the process of checking for uniqueness and creating a profile.
     */
    private void attemptProfileCreation() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        checkIfEmailExists(name, email, phone);
    }

    /**
     * Queries the "profiles" collection in Firestore to verify if a document with the
     * given email already exists.
     * <p>
     * If the email is unique, it proceeds to {@link #createProfileInDatabase(String, String, String)}.
     * If the email is already taken, it displays an error message to the user.
     *
     * @param name The user's name to be passed along for profile creation.
     * @param email The email address to check for uniqueness.
     * @param phone The user's (optional) phone number to be passed along for profile creation.
     */
    private void checkIfEmailExists(String name, String email, String phone) {
        db.collection("profiles")
                .whereEqualTo("email", email)
                .limit(1) // We only need to know if at least one exists
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            // Email is already taken
                            Toast.makeText(this, "This email address is already registered.", Toast.LENGTH_LONG).show();
                            setLoading(false);
                        } else {
                            // Email is unique, proceed to create the profile
                            createProfileInDatabase(name, email, phone);
                        }
                    } else {
                        // Handle failure to query the database
                        Log.e(TAG, "Error checking for email existence: ", task.getException());
                        Toast.makeText(this, "Error checking profile. Please try again.", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
    }


    /**
     * Creates a new {@link Profile} object, saves it as a new document in the Firestore "profiles"
     * collection, and registers it with the {@link ProfileManager} singleton.
     * <p>
     * On success, it navigates the user to the main part of the application.
     *
     * @param name The user's full name.
     * @param email The user's unique email address.
     * @param phone The user's phone number.
     */
    private void createProfileInDatabase(String name, String email, String phone) {
        // Since we aren't using Firebase Auth, we generate a random unique ID.
        String guid = UUID.randomUUID().toString();
        String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        Profile newProfile = new Profile(androidId, guid, name, phone, email);

        db.collection("profiles").document(guid)
                .set(newProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile created successfully in Firestore with GUID: " + guid);

                    // Set the profile in the singleton for global app access
                    ProfileManager.getInstance().setCurrentUserProfile(newProfile);

                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error writing profile to Firestore", e);
                    Toast.makeText(this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    /**
     * Navigates to the {@link MainActivity} after a successful signup and login.
     * It clears the activity stack to prevent the user from returning to the signup screen
     * using the back button.
     */
    private void navigateToMain() {
        Toast.makeText(this, "Welcome, " + ProfileManager.getInstance().getCurrentUserProfile().getName() + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the back stack so the user can't press "back" to return to the signup screen
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
            progressBar.setVisibility(View.VISIBLE);
            buttonSignup.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonSignup.setVisibility(View.VISIBLE);
        }
    }
}

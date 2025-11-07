package com.example.shopping_basket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

/**
 * Handles user profile creation without Firebase Authentication.
 * It checks for email uniqueness in Firestore and then creates a local profile.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    // UI Elements
    private TextInputEditText editTextName, editTextEmail, editTextPhone;
    private Button buttonSignup;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;

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
     * Queries Firestore to see if a profile with the given email already exists.
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
     * Creates the profile document in Firestore and registers it with the Singleton.
     */
    private void createProfileInDatabase(String name, String email, String phone) {
        // Since we aren't using Firebase Auth, we generate a random unique ID.
        String guid = UUID.randomUUID().toString();
        Profile newProfile = new Profile(guid, name, phone, email);

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
     * Navigates to the MainActivity and clears the activity stack.
     */
    private void navigateToMain() {
        Toast.makeText(this, "Welcome, " + ProfileManager.getInstance().getCurrentUserProfile().getName() + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the back stack so the user can't press "back" to return to the signup screen
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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

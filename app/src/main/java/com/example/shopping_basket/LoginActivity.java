package com.example.shopping_basket;

import android.content.Intent;
import android.os.Bundle;
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
 * Handles a simple user sign-in by checking for an existing profile in Firestore.
 * This does not use Firebase Authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI Elements
    private TextInputEditText editTextLoginEmail;
    private Button buttonLogin, buttonToSignup;
    private ProgressBar progressBarLogin;

    // Firebase
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        initializeViews();

        // Setup button click listeners
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonToSignup.setOnClickListener(v -> {
            // Navigate to the SignupActivity
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonToSignup = findViewById(R.id.buttonToSignup);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    /**
     * Validates input and starts the login process by searching for the user in Firestore.
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
     * Queries the "profiles" collection in Firestore for a document matching the provided email.
     * @param email The email to search for.
     */
    private void findProfileByEmail(String email) {
        db.collection("profiles")
                .whereEqualTo("email", email)
                .limit(1) // We only expect one user per email
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Profile was found
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


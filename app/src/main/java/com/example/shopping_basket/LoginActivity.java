package com.example.shopping_basket;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * The initial screen of the application.
 * It attempts to automatically log in the user based on their device ID. If no existing profile is found,
 * it provides an option to navigate to the {@link SignupActivity} to create a new profile.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI Elements
    private Button buttonGetStarted;
    private ProgressBar progressBarLogin;

    // Firebase
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes the views, Firebase instances, and attempts to automatically log in the user.
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

        // Set up the "Get started" button to navigate to the SignupActivity
        buttonGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Attempt to log in the user automatically
        attemptAutoLogin();
    }

    /**
     * Initializes all UI components from the layout file.
     */
    private void initializeViews() {
        buttonGetStarted = findViewById(R.id.buttonGetStarted);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    /**
     * Attempts to automatically log in the user by finding a profile
     * that matches the current device's unique ID.
     */
    private void attemptAutoLogin() {
        setLoading(true); // Show loading indicator while we check for a profile
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // If we can't get a device ID, we can't auto-login. Show the "Get started" button.
        if (androidId == null || androidId.isEmpty()) {
            Log.w(TAG, "Cannot attempt auto-login: Android ID is null or empty.");
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
                    } else {
                        // This block runs if the task fails or if no profile is found for this device
                        Log.d(TAG, "Auto-login failed: No profile found for this device.", task.getException());
                        setLoading(false); // Hide the progress bar and show the "Get started" button
                    }
                });
    }

    /**
     * Navigates to the MainActivity after a successful login.
     * @param name The name of the logged-in user to display in a welcome message.
     */
    private void navigateToMain(String name) {
        showInfoToast(this, "Welcome back, " + name + "!");
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the activity stack so the user can't go back to the login screen
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish this activity so it's removed from the back stack
    }

    /**
     * Toggles the visibility of the loading indicator and the "Get started" button.
     * @param isLoading true to show the progress bar and hide the button, false otherwise.
     */
    private void setLoading(boolean isLoading) {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        if (isLoading) {
            progressBarLogin.setVisibility(VISIBLE);
            buttonGetStarted.setVisibility(GONE);
        } else {
            progressBarLogin.setVisibility(GONE);
            buttonGetStarted.setVisibility(VISIBLE);
            buttonGetStarted.startAnimation(fadeInAnimation);
        }
    }

    /**
     * Displays a custom toast message.
     * @param context The context to use.
     * @param message The message to display.
     */
    // TODO: Make this static
    public void showInfoToast(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View layout = inflater.inflate(R.layout.info_toast_layout, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);

        toast.show();
    }
}

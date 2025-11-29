package com.example.shopping_basket;

/**
 * A Singleton class to manage the current user's profile data throughout the app.
 * This provides a single, static point of access to the logged-in user's profile.
 */
public class ProfileManager {

    // The single, static instance of this class
    private static ProfileManager instance;

    // The profile object for the currently logged-in user
    private Profile currentUserProfile;

    // Flag to track if the admin is currently in "admin mode".
    private boolean adminMode = false;

    /**
     * Private constructor to prevent anyone else from creating an instance.
     */
    private ProfileManager() {
        // Private constructor for Singleton pattern
    }

    /**
     * The static method to get the single instance of the class.
     * @return The singleton ProfileManager instance.
     */
    public static synchronized ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    /**
     * Sets the profile for the current user after a successful login or profile creation.
     * @param profile The Profile object to be stored.
     */
    public void setCurrentUserProfile(Profile profile) {

        this.currentUserProfile = profile;
        // Reset admin mode whenever a new user profile is set.
        this.adminMode = false;
    }

    /**
     * Retrieves the profile of the currently logged-in user.
     * @return The current user's Profile object, or null if no user is logged in.
     */
    public Profile getCurrentUserProfile() {
        return this.currentUserProfile;
    }

    /**
     * Clears the current user's profile data. This should be called on logout.
     */
    public void clearUserProfile() {

        this.currentUserProfile = null;
        // Also clear admin mode on logout.
        this.adminMode = false;
    }

    /**
     * A convenience method to check if a user is currently logged in.
     * @return true if a profile is set, false otherwise.
     */
    public boolean isUserLoggedIn() {
        return this.currentUserProfile != null;
    }

    /**
     * Sets the current session to be in admin mode or regular mode.
     * Only works if the logged-in user is actually an admin.
     *
     * @param isInAdminMode true to enter admin mode, false to exit.
     */
    public void setAdminMode(boolean isInAdminMode) {
        // Safety check: Only allow setting admin mode if the user is an admin.
        if (currentUserProfile != null && currentUserProfile.isAdmin()) {
            this.adminMode = isInAdminMode;
        } else {
            // Otherwise, ensure it's always false.
            this.adminMode = false;
        }
    }

    /**
     * Checks if the current session is in admin mode.
     *
     * @return true if the user is an admin AND is currently in admin mode.
     */
    public boolean isAdminMode() {
        // The user must be an admin and have the mode enabled.
        return currentUserProfile != null && currentUserProfile.isAdmin() && this.adminMode;
    }
}


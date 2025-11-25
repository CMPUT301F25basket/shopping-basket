package com.example.shopping_basket;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.shopping_basket.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;

/**
 * The main activity of the application, serving as the primary container for the user interface
 * after they have successfully logged in.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Setting up the main app.</li>
 *     <li>Hosting the {@link NavHostFragment} which manages the navigation between different fragments.</li>
 *     <li>Initializing and handling item selections for the {@link BottomNavigationView} to navigate between
 *         {@link HomeFragment}, {@link EventCreationFragment} or {@link MyEventFragment}, and {@link InboxFragment}.</li>
 *     <li>Inflating the options menu in the toolbar, which includes the profile action item.</li>
 *     <li>Handling clicks on the profile action item to display the {@link ProfileFragment} as a dialog.</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Profile profile;


    /**
     * Called when the activity is first created.
     * <p>
     * This method initializes view binding, sets up the toolbar, and configures the
     * {@link NavController} with the {@link BottomNavigationView} to handle the main
     * navigation structure using item listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.profile = ProfileManager.getInstance().getCurrentUserProfile();

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);

        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNavigationView = binding.bottomNavView;
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                navController.navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.myEvent) {
                EventRepository.getLatestEventByOwner(profile.getGuid(), event -> {
                    // Event exists and has not ended, go to MyEventFragment
                    if (event != null && event.getEventTime().after(new Date())) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", event);
                        navController.navigate(R.id.myEventFragment, bundle);
                    }
                });
                // No event found or event has ended, go to EventCreationFragment
                navController.navigate(R.id.eventCreationFragment);
                return true;
            } else if (itemId == R.id.inbox) {
                navController.navigate(R.id.inboxFragment);
                return true;
            }
            return false;
        });
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * <p>
     * It handles clicks on the toolbar action items. Specifically, it listens for a click
     * on the {@code R.id.action_profile} item and shows the {@link ProfileFragment} as a dialog.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            ProfileFragment profileDialog = new ProfileFragment();
            profileDialog.show(getSupportFragmentManager(), "ProfileDialogFragment");
            return true;
        }
        else if (id == R.id.action_send ) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     *
     * @return true if Up navigation completed successfully and this Activity was finished, false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
package com.example.shopping_basket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentAdminMenuBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMenuFragment extends Fragment {
    private FragmentAdminMenuBinding binding;

    public AdminMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAdminMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide bottom navigation bar
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavView);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        // Access the hosting activity's action bar and set the title
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Admin Menu");
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonAdminBrowseProfiles.setOnClickListener(v -> {
            ProfileManager.getInstance().setAdminMode(true);
            // NavHostFragment.findNavController(this).navigate(R.id.action_adminMenuFragment_to_adminBrowseProfilesFragment);
        });

        binding.buttonAdminBrowseEvents.setOnClickListener(v -> {
            ProfileManager.getInstance().setAdminMode(true);
            NavHostFragment.findNavController(this).navigate(R.id.action_adminMenuFragment_to_homeFragment);
        });

        binding.buttonAdminBrowseImages.setOnClickListener(v -> {
            ProfileManager.getInstance().setAdminMode(true);
            // NavHostFragment.findNavController(this).navigate(R.id.action_adminMenuFragment_to_adminBrowseImagesFragment);
        });

        binding.buttonAdminBrowseInbox.setOnClickListener(v -> {
            ProfileManager.getInstance().setAdminMode(true);
            NavHostFragment.findNavController(this).navigate(R.id.action_adminMenuFragment_to_inboxFragment);
        });

        binding.buttonAdminToHome.setOnClickListener(v -> {
            ProfileManager.getInstance().setAdminMode(false);
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Make the bottom navigation bar visible again when the fragment is destroyed.
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavView);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
        binding = null;
    }
}
package com.example.shopping_basket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentUserProfileItemBinding;

import java.util.List;

/**
 * RecyclerView adapter for displaying user profiles in the admin "Browse Profiles" screen.
 * Each row shows basic profile info and a Delete button that delegates back to the fragment.
 * U.S. 03.07.01 - The Admin similarly removes organizers that violate app policy from the Profiles List Screen
 */
public class UserProfileRecyclerViewAdapter
        extends RecyclerView.Adapter<UserProfileRecyclerViewAdapter.ViewHolder> {

    /**
     * Callback so the Fragment can handle delete actions.
     */
    public interface OnProfileDeleteListener {
        void onProfileDelete(Profile profile);
    }

    private final List<Profile> profiles;
    private final OnProfileDeleteListener deleteListener;

    public UserProfileRecyclerViewAdapter(List<Profile> profiles, OnProfileDeleteListener deleteListener) {
        this.profiles = profiles;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentUserProfileItemBinding binding = FragmentUserProfileItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        holder.bind(profile, deleteListener);
    }

    @Override
    public int getItemCount() {
        return profiles != null ? profiles.size() : 0;
    }

    /**
     * ViewHolder that binds a single Profile to the card layout.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private final FragmentUserProfileItemBinding binding;

        ViewHolder(FragmentUserProfileItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Profile profile, final OnProfileDeleteListener deleteListener) {
            binding.userProfileName.setText(profile.getName());
            binding.userProfileEmail.setText(profile.getEmail());
            binding.userProfilePhone.setText(profile.getPhone());

            binding.buttonDeleteUser.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onProfileDelete(profile);
                }
            });
        }
    }
}
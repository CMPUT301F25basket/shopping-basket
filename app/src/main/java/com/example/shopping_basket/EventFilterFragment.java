package com.example.shopping_basket;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentEventFilterBinding;

public class EventFilterFragment extends DialogFragment {
    private FragmentEventFilterBinding binding;

    public EventFilterFragment() {
        // Required empty public constructor
    }
    public static EventFilterFragment newInstance() {
        return new EventFilterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEventFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
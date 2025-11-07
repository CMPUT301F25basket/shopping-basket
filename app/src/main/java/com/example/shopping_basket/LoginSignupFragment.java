package com.example.shopping_basket;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentLoginSignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} for Login and Signup using FirebaseAuth.
 */
public class LoginSignupFragment extends Fragment {
    private FragmentLoginSignupBinding binding;
    private FirebaseAuth auth;

    public LoginSignupFragment() {
        // Required empty public constructor
    }

    public static LoginSignupFragment newInstance() {
        return new LoginSignupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentLoginSignupBinding.bind(view);
        auth = FirebaseAuth.getInstance();

        binding.buttonLogin.setOnClickListener(v -> login(v));
        binding.buttonSignup.setOnClickListener(v -> signup(v));
    }

    private void login(View v) {
        String email = binding.textInputEmailEdit.getText() == null ? "" : binding.textInputEmailEdit.getText().toString().trim();
        String password = binding.textInputPasswordEdit.getText() == null ? "" : binding.textInputPasswordEdit.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            findNavController(v).navigate(R.id.homeFragment);
                        } else {
                            Toast.makeText(requireContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void signup(View v) {
        String email = binding.textInputEmailEdit.getText() == null ? "" : binding.textInputEmailEdit.getText().toString().trim();
        String password = binding.textInputPasswordEdit.getText() == null ? "" : binding.textInputPasswordEdit.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Account created", Toast.LENGTH_SHORT).show();
                            findNavController(v).navigate(R.id.homeFragment);
                        } else {
                            Toast.makeText(requireContext(), "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}

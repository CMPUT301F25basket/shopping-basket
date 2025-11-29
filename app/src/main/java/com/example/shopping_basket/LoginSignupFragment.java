package com.example.shopping_basket;

import static androidx.navigation.Navigation.findNavController;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentLoginSignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} for Login and Signup using FirebaseAuth.
 */
public class LoginSignupFragment extends Fragment {
    private FragmentLoginSignupBinding binding;
    private FirebaseAuth auth;
    private String storedVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

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

        binding.buttonLogin.setOnClickListener(v -> attemptPasswordless(v));
        binding.buttonSignup.setOnClickListener(v -> attemptPasswordless(v));
    }

    private void attemptPasswordless(View v) {
        String email = binding.textInputEmailEdit.getText() == null ? "" : binding.textInputEmailEdit.getText().toString().trim();
        String phone = binding.textInputPhoneEdit.getText() == null ? "" : binding.textInputPhoneEdit.getText().toString().trim();

        if (email.isEmpty() && phone.isEmpty()) {
            Toast.makeText(requireContext(), "Provide an email or phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.isEmpty()) {
            startPhoneNumberVerification(phone, v);
            return;
        }

        // Email-only flow: send email sign-in link
        sendEmailSignInLink(email, v);
    }

    private void sendEmailSignInLink(String email, View v) {
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                // TODO: set a real continue URL in production & configure in Firebase Console
                .setUrl("https://www.example.com/finishSignIn")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(requireActivity().getPackageName(), true, null)
                .build();

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Save email locally to complete sign-in when link clicked
                            SharedPreferences prefs = requireActivity().getSharedPreferences("auth", 0);
                            prefs.edit().putString("emailForSignIn", email).apply();
                            Toast.makeText(requireContext(), "Sent sign-in link. Check your email.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to send link: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber, View v) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto-retrieval or instant validation succeeded
                        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Phone sign-in successful", Toast.LENGTH_SHORT).show();
                                    findNavController(v).navigate(R.id.homeFragment);
                                } else {
                                    Toast.makeText(requireContext(), "Sign-in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        Toast.makeText(requireContext(), "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        storedVerificationId = verificationId;
                        resendToken = token;
                        // Prompt user to enter the code
                        promptForVerificationCode(v);
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void promptForVerificationCode(View v) {
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(requireContext())
                .setTitle("Enter verification code")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String code = input.getText().toString().trim();
                        if (code.isEmpty() || storedVerificationId == null) {
                            Toast.makeText(requireContext(), "Enter the SMS code", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(storedVerificationId, code);
                        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Phone sign-in successful", Toast.LENGTH_SHORT).show();
                                    findNavController(v).navigate(R.id.homeFragment);
                                } else {
                                    Toast.makeText(requireContext(), "Sign-in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}

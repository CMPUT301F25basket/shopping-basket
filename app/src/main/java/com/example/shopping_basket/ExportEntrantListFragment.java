package com.example.shopping_basket;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentExportEntrantListBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ExportEntrantListFragment extends DialogFragment {
    private FragmentExportEntrantListBinding binding;
    private ArrayList<Profile> enrolledEntrants;
    private Event event;
    private ActivityResultLauncher<Intent> createFileLauncher;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public ExportEntrantListFragment() {
        // Required empty public constructor
    }

    public static ExportEntrantListFragment newInstance(ArrayList<Profile> enrolledEntrants, Event event) {
        ExportEntrantListFragment fragment = new ExportEntrantListFragment();
        Bundle args = new Bundle();
        args.putSerializable("enrolled_entrants", enrolledEntrants);
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            enrolledEntrants = (ArrayList<Profile>) getArguments().getSerializable("enrolled_entrants");
        }

        if (event == null || enrolledEntrants == null) {
            if(enrolledEntrants == null) enrolledEntrants = new ArrayList<>();
            dismiss();
        }

        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                writeCSVtoURI(uri);
                                Toast.makeText(getContext(), "Export successful!", Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    dismiss();  // Dismiss the dialog after the operation is complete or cancelled
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentExportEntrantListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
        }

        String suggestedFilename = event.getName()
                                        .toLowerCase()
                                        .replaceAll("\\s+", "_") // Replace one or more spaces with a single underscore
                                        .replaceAll("[^a-z0-9_]", ""); // Remove any character that is not a letter, number, or underscore
        binding.editTextEventNameCsv.setText(suggestedFilename);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonCancelCsv.setOnClickListener(v -> dismiss());

        binding.buttonExportCsv.setOnClickListener(v -> {
            String chosenFilename = binding.editTextEventNameCsv.getText().toString().trim();
            if (chosenFilename.isEmpty()) {
                binding.editTextEventNameCsv.setError("Filename cannot be empty");
                return;
            }
            // Append .csv if the user didn't
            if (!chosenFilename.endsWith(".csv")) {
                chosenFilename += ".csv";
            }
            exportAsCSV(chosenFilename);
        });
    }

    /**
     * Starts the Storage Access Framework process to create and save the CSV file.
     * @param filename The suggested name for the file.
     */
    private void exportAsCSV(String filename) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        // Launch the intent to get a URI from the user
        createFileLauncher.launch(intent);
    }

    /**
     * Writes the profile data (ID, Name, Email, Phone number) to the given URI as a CSV.
     * @param uri The URI of the file to write to.
     * @throws IOException If the file cannot be written.
     */
    private void writeCSVtoURI(Uri uri) throws IOException {
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IOException("Failed to get output stream.");
            }

            String header = "User ID,Name,Email,Phone Number\n";
            outputStream.write(header.getBytes());

            for (Profile profile : enrolledEntrants) {
                StringBuilder row = new StringBuilder();
                row.append(escapeCSV(profile.getGuid())).append(",");
                row.append(escapeCSV(profile.getName())).append(",");
                row.append(escapeCSV(profile.getEmail())).append(",");
                row.append(escapeCSV(profile.getPhone())).append("\n");

                outputStream.write(row.toString().getBytes());
            }
        }
    }

    /**
     * A helper function to escape characters that have special meaning in CSV
     * (like commas and quotes).
     * @param value The string to escape.
     * @return The properly escaped CSV field.
     */
    private String escapeCSV(String value) {
        if (value == null) return "";

        // If the value contains a comma, newline, or quote, wrap it in double quotes.
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";

        return  value;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.shopping_basket;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shopping_basket.databinding.FragmentEventDetailBinding;
import com.example.shopping_basket.databinding.FragmentEventQrBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;


/**
 * A fragment that displays a confirmation message and a QR code after a user
 * successfully creates a new event. The QR code can be shared to allow others
 * to find and register for the event.
 */
public class EventQRFragment extends Fragment {

    private FragmentEventQrBinding binding;
    private Event event;
    private Bitmap qrCodeBitmap;

    /**
     * Default public constructor.
     * Required for instantiation by the Android framework.
     */
    public EventQRFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     * Initializes the profile from ProfileManager and retrieves the
     * Event object from the fragment's arguments.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the Event object that was just created.
        // It is passed as an argument to this fragment
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEventQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // If the event data is missing, navigate back to prevent a crash.
        // Optionally, navigate to my event instead
        if (event == null) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        qrCodeBitmap = generateQRCode();
        if (qrCodeBitmap != null) {
            binding.eventQr.setImageBitmap(qrCodeBitmap);
        }

        binding.buttonSaveQr.setEnabled(false);
        setupClickListeners();
    }

    /**
     * Generates a QR code from the event's URL. Encodes the event's deep link
     * URL into a QR code bitmap. Logs an error if the URL is missing or if the
     * encoding process fails.
     */
    private Bitmap generateQRCode() {
        String eventURL = event.getEventURL();

        if (eventURL == null || eventURL.isEmpty()) {
            Log.e("EventQRFragment", "Event URL is null or empty. Cannot generate QR code.");
            if (binding != null) binding.eventQr.setVisibility(View.GONE);
            return null;
        }

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(barcodeEncoder.encode(eventURL, BarcodeFormat.QR_CODE, 800, 800));
        } catch (WriterException e) {
            Log.e("EventQRFragment", "Failed to generate QR code for URL: " + eventURL, e);
            return null;
        }
    }

    /**
     * Sets up the click listener for the "Go to your event" button.
     */
    private void setupClickListeners() {
        binding.buttonQrToMyEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            NavHostFragment.findNavController(this).navigate(R.id.action_eventQRFragment_to_myEventFragment, bundle);
        });
        binding.buttonSaveQr.setOnClickListener(v -> {
            saveQRCodeToGallery(qrCodeBitmap);
        });
    }

    private void saveQRCodeToGallery(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(getContext(), "QR Code not available to save.", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: Implement
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Nullifies the binding object to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the binding object to prevent memory leaks when the view is destroyed.
        binding = null;
    }
}
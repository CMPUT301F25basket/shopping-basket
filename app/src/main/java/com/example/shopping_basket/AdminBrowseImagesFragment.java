package com.example.shopping_basket;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminBrowseImagesFragment
 *
 * US 03.06.01 – As an administrator, I want to be able to browse images that are uploaded
 *               so I can remove them if necessary.
 * US 03.03.01 – As an administrator, I want to be able to remove images.
 *
 * Shows all image entries stored in the "images" Firestore collection and
 * lets the admin delete them. No Firebase Storage is used.
 */
public class AdminBrowseImagesFragment extends Fragment {

    private static final String TAG = "AdminBrowseImages";
    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 2;

    private RecyclerView recyclerView;
    private ImageRecyclerViewAdapter adapter;
    private final List<GalleryImage> images = new ArrayList<>();
    private FirebaseFirestore db;

    public AdminBrowseImagesFragment() {
        // Required empty public constructor
    }

    public static AdminBrowseImagesFragment newInstance(int columnCount) {
        AdminBrowseImagesFragment fragment = new AdminBrowseImagesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);

        // Root of this layout is the RecyclerView itself
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            adapter = new ImageRecyclerViewAdapter(images, this::confirmDeleteImage);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Admin – Images");
        }

        loadImages();
    }

    /**
     * Loads all image documents from the "images" Firestore collection.
     * Each document is mapped into a GalleryImage (id + uploaderName).
     */
    private void loadImages() {
        db.collection("images").get().addOnSuccessListener(queryDocumentSnapshots -> {
            images.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                GalleryImage image = document.toObject(GalleryImage.class);

                // Ensure the document ID is set.
                if (image.getId() == null || image.getId().isEmpty()) {
                    image.setId(document.getId());
                }
                images.add(image);
            }
            adapter.notifyDataSetChanged();
        })
                .addOnFailureListener(e -> {Log.e(TAG, "Error loading images", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load images. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Shows a confirmation dialog before actually deleting an image entry.
     */
    private void confirmDeleteImage(GalleryImage image) {
        if (getContext() == null) {
            return;
        }

        String uploader = (image.getUploaderName() != null && !image.getUploaderName().isEmpty())
                ? image.getUploaderName() : "this user";

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete image")
                .setMessage("Delete the image uploaded by " + uploader + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(image))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the image document from Firestore and removes it from the list.
     * No Firebase Storage is involved.
     */
    private void deleteImage(GalleryImage image) {
        if (db == null || getContext() == null) {
            return;
        }

        String id = image.getId();
        if (id == null || id.isEmpty()) {
            Toast.makeText(getContext(), "Unable to delete image: missing id.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("images").document(id).delete().addOnSuccessListener(aVoid -> {
            images.remove(image);adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Image deleted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {Log.e(TAG, "Error deleting image", e);
                    Toast.makeText(getContext(), "Failed to delete image. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
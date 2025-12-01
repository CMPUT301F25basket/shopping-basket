package com.example.shopping_basket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentImageItemBinding;

import java.util.List;

/**
 * RecyclerView adapter used on the Admin "Browse Images" screen.
 * Each card shows a single image entry and a Delete button.
 *
 * Note: The actual image content is not stored in Firebase Storage for this project.
 * The layout shows a placeholder image, but the admin can still browse entries and delete them.
 */
public class ImageRecyclerViewAdapter
        extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder> {

    /**
     * Callback so the fragment can handle delete actions.
     */
    public interface OnImageDeleteListener {
        void onImageDelete(GalleryImage image);
    }

    private final List<GalleryImage> images;
    private final OnImageDeleteListener deleteListener;

    public ImageRecyclerViewAdapter(List<GalleryImage> images, OnImageDeleteListener deleteListener) {
        this.images = images;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentImageItemBinding binding = FragmentImageItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GalleryImage image = images.get(position);
        holder.bind(image, deleteListener);
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    /**
     * ViewHolder that binds a single GalleryImage to the card layout.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private final FragmentImageItemBinding binding;

        ViewHolder(FragmentImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final GalleryImage image,
                  final OnImageDeleteListener deleteListener) {

            // Show uploader name (if available) under the placeholder image.
            String uploader = (image.getUploaderName() != null && !image.getUploaderName().isEmpty())
                    ? image.getUploaderName() : "Unknown uploader";

            binding.galleryItemUploaderName.setText("Uploaded by " + uploader);

            // The ImageView (gallery_item_image) uses whatever placeholder is defined in XML.
            // No Storage integration is required.

            binding.buttonDeleteImage.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onImageDelete(image);
                }
            });
        }
    }
}
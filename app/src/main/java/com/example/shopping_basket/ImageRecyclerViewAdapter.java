package com.example.shopping_basket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentImageItemBinding;

import java.util.List;

/**
 * RecyclerView adapter used on the Admin "Browse Images" screen.
 * Shows each image entry with uploader info and Delete button.
 */
public class ImageRecyclerViewAdapter
        extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder> {

    public interface OnImageDeleteListener {
        void onImageDelete(GalleryImage image);
    }

    private final List<GalleryImage> images;
    private final OnImageDeleteListener deleteListener;

    public ImageRecyclerViewAdapter(List<GalleryImage> images,
                                    OnImageDeleteListener deleteListener) {
        this.images = images;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentImageItemBinding binding = FragmentImageItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final FragmentImageItemBinding binding;

        ViewHolder(FragmentImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final GalleryImage image,
                  final OnImageDeleteListener deleteListener) {

            String uploaderText;

            if (image.getUploaderName() != null && !image.getUploaderName().isEmpty()) {
                uploaderText = image.getUploaderName();
            } else if (image.getUploaderId() != null && !image.getUploaderId().isEmpty()) {
                uploaderText = "User " + image.getUploaderId();
            } else {
                uploaderText = "Unknown uploader";
            }

            binding.galleryItemUploaderName.setText("Uploaded by " + uploaderText);

            binding.buttonDeleteImage.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onImageDelete(image);
                }
            });
        }
    }
}
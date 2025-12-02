package com.example.shopping_basket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.shopping_basket.databinding.FragmentImageItemBinding;

import java.util.List;

/**
 * RecyclerView adapter for Admin browse posters screen.
 * It shows each event poster (decoded from Base64) with event name
 * and uploader, plus a delete button.
 */
public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder> {

    public interface OnPosterDeleteListener {
        void onPosterDelete(EventPoster poster);
    }

    private final List<EventPoster> posters;
    private final OnPosterDeleteListener deleteListener;

    public ImageRecyclerViewAdapter(List<EventPoster> posters,
                                    OnPosterDeleteListener deleteListener) {
        this.posters = posters;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        FragmentImageItemBinding binding = FragmentImageItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventPoster poster = posters.get(position);
        holder.bind(poster, deleteListener);
    }

    @Override
    public int getItemCount() {
        return posters != null ? posters.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final FragmentImageItemBinding binding;

        ViewHolder(FragmentImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final EventPoster poster,
                  final OnPosterDeleteListener deleteListener) {

            // Decode Base64 poster into a Bitmap
            Bitmap bitmap = null;
            try {
                if (poster.getPosterBase64() != null && !poster.getPosterBase64().isEmpty()) {
                    byte[] bytes = Base64.decode(poster.getPosterBase64(), Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
            } catch (IllegalArgumentException e) {
                // invalid base64, ignore
            }

            if (bitmap != null) {
                binding.galleryItemImage.setImageBitmap(bitmap);
            } else {
                // fallback placeholder if something went wrong
                binding.galleryItemImage.setImageResource(R.drawable.image_placeholder);
            }

            String uploader = poster.getUploaderName() != null ? poster.getUploaderName() : "Unknown uploader";

            binding.galleryItemUploaderName.setText("Uploaded by " + uploader);

            binding.buttonDeleteImage.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onPosterDelete(poster);
                }
            });
        }
    }
}
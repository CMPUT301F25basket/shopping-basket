package com.example.shopping_basket;

/**
 * Simple model class for an uploaded image entry that appears in the admin gallery.
 * Documents live in the "images" collection in Firestore.
 *
 * Only metadata is kept here; the assignment does not use Firebase Storage.
 */
public class GalleryImage {

    // Firestore document id
    private String id;

    // Optional metadata: name of the uploader
    private String uploaderName;

    // Required empty constructor for Firestore deserialization
    public GalleryImage() { }

    public GalleryImage(String id, String uploaderName) {
        this.id = id;
        this.uploaderName = uploaderName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }
}
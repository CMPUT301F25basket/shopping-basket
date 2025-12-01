package com.example.shopping_basket;

/**
 * Model for an uploaded image entry stored in the "images" collection.
 *
 * Required fields:
 *  - id           : Firestore document ID
 *  - uploaderId   : GUID of the uploader
 *  - uploaderName : Display name of the uploader
 *
 * Optional:
 *  - imageUrl     : Placeholder for future use
 */
public class GalleryImage {

    private String id;            // Firestore document id
    private String imageUrl;      // optional (always null unless adding URLs)
    private String uploaderId;    // profile GUID
    private String uploaderName;  // display name

    public GalleryImage() { }

    public GalleryImage(String id,
                        String imageUrl,
                        String uploaderId,
                        String uploaderName) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
    }

    // ---- Getters and Setters ----

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
}
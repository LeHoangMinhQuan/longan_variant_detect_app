package com.example.phnloinhn.src.Model;

// History.java
import com.google.firebase.Timestamp;

public class History {
    private String variantName;
    private String imageUrl;
    private String timestamp;

    public History() {}

    public History(String variantName, String imageUrl, String timestamp) {
        this.variantName = variantName;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getters & setters
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}


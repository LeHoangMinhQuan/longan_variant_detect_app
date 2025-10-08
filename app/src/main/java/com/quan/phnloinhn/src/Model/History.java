package com.quan.phnloinhn.src.Model;

// History.java

import java.util.Objects;

public class History {
    private String variantName;
    private String imageUrl;
    private String timestamp;

    public History() {} // Firebase requires

    public History(String variantName, String imageUrl, String timestamp) {
        this.variantName = variantName;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getters & setters
    public String getVariantName() { return variantName; }

    public String getImageUrl() { return imageUrl; }

    public String getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        History history = (History) o;
        return Objects.equals(getVariantName(), history.getVariantName()) && Objects.equals(getImageUrl(), history.getImageUrl()) && Objects.equals(getTimestamp(), history.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVariantName(), getImageUrl(), getTimestamp());
    }
}


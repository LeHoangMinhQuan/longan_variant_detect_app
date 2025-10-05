package com.quan.phnloinhn.src.Remote;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.*;

public class StorageHelper {

    private final FirebaseStorage storage;
    private static final String TAG = "StorageHelper";

    public StorageHelper() {
        storage = FirebaseStorage.getInstance();
    }

    public StorageReference getUserHistoryRef(String fileName) {
        String uid = FirebaseAuth.getInstance().getUid();
        return FirebaseStorage.getInstance().getReference()
                .child("users/" + uid + "/history/" + fileName);
    }

    public void uploadImage(Context context, StorageReference ref, Uri fileUri, ResultCallback<String> callback) {
        ref.putFile(fileUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteImage(String imageUrl, ResultCallback<Boolean> callback) {
        try {
            // Get reference from the full image URL
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

            // Delete file
            photoRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Successfully deleted
                        callback.onSuccess(true);
                    })
                    .addOnFailureListener(e -> {
                        // Deletion failed
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
}

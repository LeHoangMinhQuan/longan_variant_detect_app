package com.example.phnloinhn.src.Remote;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.phnloinhn.src.Utils.Utils;
import com.example.phnloinhn.src.View.TestActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.*;

public class StorageHelper {

    private final FirebaseStorage storage;
    private static final String TAG = "StorageHelper";

    public StorageHelper() {
        storage = FirebaseStorage.getInstance();
        Log.d(TAG, "Initialize storage instance successfully");
    }

    public StorageReference getUserHistoryRef(String fileName) {
        String uid = FirebaseAuth.getInstance().getUid();
        return FirebaseStorage.getInstance().getReference()
                .child("users/" + uid + "/history/" + fileName);
    }

    public void uploadImage(Context context, StorageReference ref, Uri fileUri, ResultCallback<String> callback) {
        Utils.showLoading(context, "Đang xử lý");
        ref.putFile(fileUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }

    public void getImageUrl(String path, ResultCallback<String> callback) {
        storage.getReference().child(path).getDownloadUrl()
                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteImage(String path, ResultCallback<Void> callback) {
        storage.getReference().child(path).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}

package com.example.phnloinhn.src.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phnloinhn.src.Model.GrowingMethod;
import com.example.phnloinhn.src.Model.LonganVariant;
import com.example.phnloinhn.src.Remote.FirestoreHelper;
import com.example.phnloinhn.src.Remote.ResultCallback;
import com.example.phnloinhn.src.Remote.StorageHelper;
import com.example.phnloinhn.src.Utils.Utils;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private FirestoreHelper firestoreHelper;
    private StorageHelper storageHelper;
    private Uri imageUri;
    private FirebaseAuth mAuth;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreHelper = new FirestoreHelper();
        storageHelper = new StorageHelper();

        // 🔹 Run tests here
        testGetAllVariants();
        testGetVariantById(Utils.IDO); // change to a real doc ID

        // 🔹 Register launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Log.d(TAG, "Image chosen: " + imageUri);
                        testUploadImage(imageUri);
                    }
                }
        );

        // 🔹 Launch picker
        pickImageFromGallery();
    }

    private void testGetAllVariants() {
        firestoreHelper.getAllVariants(new ResultCallback<Map<String, LonganVariant>>() {
            @Override
            public void onSuccess(Map<String, LonganVariant> data) {
                Log.d(TAG, "===== Firestore Data Structure =====");

                // Loop through variants
                for (Map.Entry<String, LonganVariant> entry : data.entrySet()) {
                    String variantId = entry.getKey();
                    LonganVariant variant = entry.getValue();

                    Log.d(TAG, "Collection: longan_variants");
                    Log.d(TAG, "  Document: " + variantId);
                    Log.d(TAG, "    name: " + variant.getName());
                    Log.d(TAG, "    origin: " + variant.getOrigin());
                    Log.d(TAG, "    productivity: " + variant.getProductivity());
                    Log.d(TAG, "    description: " + variant.getDescription());
                    Log.d(TAG, "    tips: " + variant.getTips());

                    if (variant.getGrowingMethods() != null && !variant.getGrowingMethods().isEmpty()) {
                        Log.d(TAG, "    Collection: growing_methods");

                        for (Map.Entry<String, GrowingMethod> gm : variant.getGrowingMethods().entrySet()) {
                            String methodId = gm.getKey();
                            GrowingMethod method = gm.getValue();

                            Log.d(TAG, "      Document: " + methodId);
                            Log.d(TAG, "        branch_pruning: " + method.getBranch_pruning());
                            Log.d(TAG, "        fertilizer: " + method.getFertilizer());
                            Log.d(TAG, "        fruit_pruning: " + method.getFruit_pruning());
                            Log.d(TAG, "        pesticide: " + method.getPesticide());
                            Log.d(TAG, "        plant_distance: " + method.getPlant_distance());
                            Log.d(TAG, "        plant_time: " + method.getPlant_time());
                            Log.d(TAG, "        soil: " + method.getSoil());
                            Log.d(TAG, "        other: " + method.getOther());
                        }
                    }
                }

                Log.d(TAG, "===== End of Firestore Data =====");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "getAllVariants failed", e);
            }
        });
    }


    private void testGetVariantById(String variantId) {
        firestoreHelper.getVariantById(variantId, new ResultCallback<LonganVariant>() {
            @Override
            public void onSuccess(LonganVariant data) {
                Log.d(TAG, "Fetched variant: " + data.getName() +
                        ", origin=" + data.getOrigin());
                if (data.getGrowingMethods() != null) {
                    for (Map.Entry<String, GrowingMethod> gm : data.getGrowingMethods().entrySet()) {
                        Log.d(TAG, "  Method " + gm.getKey() + " fertilizer=" + gm.getValue().getFertilizer());
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "getVariantById failed", e);
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void testUploadImage(Uri uri) {
        if (imageUri == null) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("AuthTest", "Current UID: " + (user != null ? user.getUid() : "null"));

        FirebaseAppCheck.getInstance().getAppCheckToken(false)
                .addOnSuccessListener(token -> {
                    Log.d(TAG, "App Check token: " + token.getToken());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get App Check token", e);
                });

        // User uploads go into /users/{uid}/history/
        StorageReference path = storageHelper.getUserHistoryRef("img1.jpg");
        Log.d(TAG, "Upload to path " + path);

        storageHelper.uploadImage(this, path, imageUri, new ResultCallback<String>() {
            @Override
            public void onSuccess(String url) {
                Utils.hideLoading();
                Log.d(TAG, "Uploaded successfully: " + url);
                testGetImageUrl(path.toString());
                testDeleteImage(path.toString());
            }

            @Override
            public void onFailure(Exception e) {
                Utils.hideLoading();
                Toast.makeText(TestActivity.this, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "uploadImage failed", e);
            }
        });
    }

    private void testGetImageUrl(String path) {
        storageHelper.getImageUrl(path, new ResultCallback<String>() {
            @Override
            public void onSuccess(String url) {
                Log.d(TAG, "Fetched image URL: " + url);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "getImageUrl failed", e);
            }
        });
    }

    private void testDeleteImage(String path) {
        storageHelper.deleteImage(path, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Image deleted successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "deleteImage failed", e);
            }
        });
    }
}

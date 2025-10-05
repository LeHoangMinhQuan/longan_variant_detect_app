package com.quan.phnloinhn.src.ViewModel;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.quan.phnloinhn.src.Model.History;
import com.quan.phnloinhn.src.Model.LonganVariant;
import com.quan.phnloinhn.src.Remote.FirestoreHelper;
import com.quan.phnloinhn.src.Remote.ResultCallback;
import com.quan.phnloinhn.src.Remote.StorageHelper;
import com.quan.phnloinhn.src.Utils.Utils;
import com.quan.phnloinhn.src.ml.ClassifierRepository;
import com.quan.phnloinhn.src.ml.MobilenetClassifier;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SharedViewModel extends AndroidViewModel {
    private final MutableLiveData<Map<String, LonganVariant>> variants = new MutableLiveData<>();
    private final MutableLiveData<LonganVariant> selectedVariant = new MutableLiveData<>(null);
    private final MutableLiveData<List<History>> historyList = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final ClassifierRepository classifierRepo;
    private final FirestoreHelper db;
    private final StorageHelper storage;
    private String TAG = "SharedViewModel";

    public SharedViewModel(@NonNull Application app) {
        super(app);
        try {
            classifierRepo = new ClassifierRepository(app.getAssets());
        } catch (IOException e) {
            throw new RuntimeException("Failed to init classifier", e);
        }
        db = new FirestoreHelper(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        storage = new StorageHelper();

        fetchVariants();
        fetchUserHistory();
    }

    // Expose LiveData
    public LiveData<LonganVariant> getSelectedVariant() { return selectedVariant; }
    public LiveData<List<History>> getHistoryList() {
        return historyList;
    }
    public LiveData<String> getMessage() { return message; }

    private void fetchVariants() {
        db.getAllVariants(new ResultCallback<Map<String, LonganVariant>>() {
            @Override
            public void onSuccess(Map<String, LonganVariant> data) {
                variants.postValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                message.postValue("Không tải được dữ liệu giống");
            }
        });
    }
    private void fetchUserHistory() {
        db.getAllHistory(new ResultCallback<List<History>>() {
            @Override
            public void onSuccess(List<History> result) {
                // Sort by timestamp descending
                result.sort((h1, h2) ->
                        h2.getTimestamp().compareTo(h1.getTimestamp()));
                historyList.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                message.postValue("Không tải được lịch sử tìm kiếm");
            }
        });
    }
    public void classifyImage(Bitmap bitmap, Uri imageUri) {
//        new Thread(() -> {
            List<MobilenetClassifier.Recognition> predictions = classifierRepo.classify(bitmap);

            if (!predictions.isEmpty()) {
                String variantName = predictions.get(0).getTitle();
                Map<String, LonganVariant> data = variants.getValue();
                if (data != null && data.containsKey(variantName)) {
                    LonganVariant variant = data.get(variantName);
                    selectedVariant.postValue(variant);
                    uploadImage(imageUri, variantName);
                }
            } else {
                message.postValue("Không dự đoán được");
            }
//        }).start();
    }

    private void uploadImage(Uri uri, String variantName) {
        if (uri == null) return;

        String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String fileName = variantName + "_" + date;
        StorageReference path = storage.getUserHistoryRef(fileName);
        Log.d(TAG, "path: " + path);
        Log.d(TAG, "uri: " + uri);

        storage.uploadImage(null, path, uri, new ResultCallback<String>() {
            @Override
            public void onSuccess(String url) {
                saveHistory(new History(variantName, url, date));
            }
            @Override
            public void onFailure(Exception e) {
                message.postValue("Lỗi khi tải ảnh lên");
                Log.d(TAG, "error: " + e);
            }
        });
    }

private void saveHistory(History newHistory) {
    db.addHistory(newHistory, new ResultCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
            List<History> current = historyList.getValue();
            List<History> updated = (current != null) ? new ArrayList<>(current) : new ArrayList<>();
            updated.add(newHistory);
            updated.sort((h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp()));
            historyList.postValue(updated);  // new list instance
            message.postValue("Đã lưu lịch sử");
        }

        @Override
        public void onFailure(Exception e) {
            message.postValue("Lỗi khi lưu lịch sử");
        }
    });
}

    public void deleteHistory(History history) {
        // Step 1: Delete image from Firebase Storage
        storage.deleteImage(history.getImageUrl(), new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Utils.hideLoading();
                // Step 2: Delete record from Firestore
                db.deleteHistory(history, new ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        // Step 3: Update UI after both deletions succeed
                        List<History> current = historyList.getValue();
                        if (current != null) {
                            List<History> updated = new ArrayList<>(current);
                            if (updated.remove(history)) {
                                historyList.postValue(updated);
                            }
                        }
                        message.postValue("✅ Đã xóa lịch sử và ảnh thành công");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        message.postValue("⚠️ Ảnh đã xóa nhưng lỗi khi xóa lịch sử Firestore: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                message.postValue("❌ Lỗi khi xóa ảnh trên Firebase Storage: " + e.getMessage());
            }
        });
    }
    public void clearMessage() {
        message.postValue(null);
    }

}



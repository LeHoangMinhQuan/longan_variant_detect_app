package com.example.phnloinhn.src.ViewModel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.phnloinhn.src.ml.MobilenetClassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class FragmentHomeViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Pair<Integer, Float>>> result = new MutableLiveData<>();
    private final MobilenetClassifier classifier;

    public FragmentHomeViewModel(@NonNull Application application) {
        super(application);
        try {
            // nạp model từ thư mục assets
            classifier = new MobilenetClassifier(application.getApplicationContext(), "mobilenetv3.tflite");
        } catch (IOException e) {
            throw new RuntimeException("Không thể load MobileNet model", e);
        }
    }

    public LiveData<List<Pair<Integer, Float>>> getResult() {
        return result;
    }

    public void classify(Bitmap bitmap) {
        Executors.newSingleThreadExecutor().execute(() -> {
            float[] preds = classifier.predict(bitmap);
            List<Pair<Integer, Float>> scores = new ArrayList<>();

            for (int i = 0; i < preds.length; i++) {
                scores.add(new Pair<>(i, preds[i]));
            }

            // sắp xếp từ cao đến thấp
            scores.sort((a, b) -> Float.compare(b.second, a.second));

            // lấy top-3
            result.postValue(scores.subList(0, Math.min(3, scores.size())));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        classifier.close();
    }
}

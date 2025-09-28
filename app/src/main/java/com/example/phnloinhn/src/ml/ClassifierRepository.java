package com.example.phnloinhn.src.ml;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.io.IOException;
import java.util.List;

public class ClassifierRepository {
    private final MobilenetClassifier classifier;

    public ClassifierRepository(AssetManager assets) throws IOException {
        classifier = new MobilenetClassifier(assets, "mobilenetv3_longan_quantized.tflite", "labels.txt", 224);
        classifier.init();
    }

    public List<MobilenetClassifier.Recognition> classify(Bitmap bitmap) {
        return classifier.classify(bitmap);
    }
}


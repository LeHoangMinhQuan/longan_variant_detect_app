package com.example.phnloinhn.src.ml;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Objects;

public class MobilenetClassifier {

    private AssetManager assetManager;
    private String modelPath;
    private String labelPath;
    private List<String> labelList;
    private int inputSize = 224;
    private Interpreter interpreter;

    public MobilenetClassifier(AssetManager assetManager, String modelPath, String labelPath, int inputSize) {
        this.assetManager = assetManager;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.inputSize = inputSize;
    }
    public static class Recognition {
        private String id = "";
        private String title = "";
        private float confidence = 0f;

        public Recognition(String id, String title, float confidence) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }

        @NonNull
        @Override
        public String toString() {
            return "Pred:{" +
                    "title=" + title +
                    ", confidence=" + confidence +
                    '}';
        }
        public String getTitle() {
            return title;
        }

        public float getConfidence() {
            return confidence;
        }
    }

    public void init() throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(5);
        options.setUseNNAPI(true);

        interpreter = new Interpreter(Objects.requireNonNull(loadModelFile(assetManager, modelPath)), options);
        labelList = loadLabelList(assetManager, labelPath);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private static MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) {
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int inputSize) {
        bitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                int input = intValues[pixel++];

                float r = ((input >> 16) & 0xFF);
                float g = ((input >> 8) & 0xFF);
                float b = (input & 0xFF);
                byteBuffer.putFloat(r);
                byteBuffer.putFloat(g);
                byteBuffer.putFloat(b);
            }
        }
        return byteBuffer;
    }

    public List<Recognition> classify(Bitmap bitmap) {
        ByteBuffer input = convertBitmapToByteBuffer(bitmap, inputSize);

        float[][] output = new float[1][labelList.size()];
        interpreter.run(input, output);

        List<Recognition> recognitions = new ArrayList<>();
        for (int i = 0; i < labelList.size(); i++) {
            recognitions.add(new Recognition("" + i, labelList.get(i), output[0][i]));
        }

        // Sort descending by confidence
        Collections.sort(recognitions, (r1, r2) -> Float.compare(r2.confidence, r1.confidence));
        return recognitions;
    }

}

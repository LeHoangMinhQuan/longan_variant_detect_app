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
        Log.d("LabelLoader", "=== START loadLabelList ===");
        Log.d("LabelLoader", "Opening labels file: " + labelPath);

        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
            Log.d("LabelLoader", "Read label[" + count + "] = " + line);
            count++;
        }
        reader.close();

        Log.d("LabelLoader", "Total labels loaded = " + labelList.size());
        Log.d("LabelLoader", "Labels = " + labelList.toString());
        Log.d("LabelLoader", "=== END loadLabelList ===");

        return labelList;
    }

    private static MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) {
        Log.d("ModelLoader", "=== START loadModelFile ===");
        try {
            Log.d("ModelLoader", "Opening model from assets: " + modelPath);
            AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
            Log.d("ModelLoader", "FileDescriptor acquired");

            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            Log.d("ModelLoader", "FileInputStream created");

            FileChannel fileChannel = inputStream.getChannel();
            Log.d("ModelLoader", "FileChannel created");

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            Log.d("ModelLoader", "StartOffset = " + startOffset + ", DeclaredLength = " + declaredLength);

            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            Log.d("ModelLoader", "MappedByteBuffer created, capacity = " + buffer.capacity());

            Log.d("ModelLoader", "=== END loadModelFile ===");
            return buffer;
        } catch (IOException e) {
            Log.e("ModelLoader", "Error loading model: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int inputSize) {
        Log.d("Preprocess", "=== START convertBitmapToByteBuffer ===");
        Log.d("Preprocess", "Original size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

        bitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);
        Log.d("Preprocess", "Scaled size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Log.d("Preprocess", "Pixel count = " + intValues.length);

        int pixel = 0;
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                int input = intValues[pixel++];

                int rRaw = (input >> 16) & 0xFF;
                int gRaw = (input >> 8) & 0xFF;
                int bRaw = input & 0xFF;

                // giữ nguyên [0,255] giống Python
                float r = (float) rRaw;
                float g = (float) gRaw;
                float b = (float) bRaw;

                byteBuffer.putFloat(r);
                byteBuffer.putFloat(g);
                byteBuffer.putFloat(b);

                // log thử 5 pixel đầu
                if (pixel <= 5) {
                    Log.d("Preprocess", "Pixel " + (pixel - 1) +
                            " RGB=(" + rRaw + "," + gRaw + "," + bRaw + ")" +
                            " -> (" + r + "," + g + "," + b + ")");
                }
            }
        }

        byteBuffer.rewind();
        Log.d("Preprocess", "=== END convertBitmapToByteBuffer ===");
        return byteBuffer;
    }

    public List<Recognition> classify(Bitmap bitmap) {
        Log.d("Classifier", "=== START CLASSIFICATION ===");

        // Preprocess
        Log.d("Classifier", "Step 1: Convert Bitmap to ByteBuffer");
        ByteBuffer input = convertBitmapToByteBuffer(bitmap, inputSize);
        Log.d("Classifier", "Input ByteBuffer capacity = " + input.capacity());

        // Model run
        Log.d("Classifier", "Step 2: Run TFLite Interpreter");
        float[][] output = new float[1][labelList.size()];
        interpreter.run(input, output);

        // Raw predictions
        Log.d("Classifier", "Step 3: Raw predictions = " + Arrays.toString(output[0]));
        Log.d("Classifier", "Output length = " + output[0].length);

        // Convert to Recognition
        Log.d("Classifier", "Step 4: Map predictions to labels");
        List<Recognition> recognitions = new ArrayList<>();
        for (int i = 0; i < labelList.size(); i++) {
            Log.d("Classifier", "Label: " + labelList.get(i) + " | Score: " + output[0][i]);
            recognitions.add(new Recognition("" + i, labelList.get(i), output[0][i]));
        }

        // Sort
        Log.d("Classifier", "Step 5: Sort results by confidence");
        Collections.sort(recognitions, (r1, r2) -> Float.compare(r2.getConfidence(), r1.getConfidence()));

        // Final result
        Log.d("Classifier", "Step 6: Final sorted results = " + recognitions.toString());
        Log.d("Classifier", "=== END CLASSIFICATION ===");

        return recognitions;
    }

}

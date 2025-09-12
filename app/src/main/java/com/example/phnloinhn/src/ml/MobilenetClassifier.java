package com.example.phnloinhn.src.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MobilenetClassifier {

    private static final int IMAGE_SIZE = 224; // chỉnh theo input model
    private Interpreter interpreter;
    private GpuDelegate gpuDelegate;

    public MobilenetClassifier(Context context, String modelPath) throws IOException {
        MappedByteBuffer model = loadModelFile(context, modelPath);
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        try {
            gpuDelegate = new GpuDelegate();
            options.addDelegate(gpuDelegate);
        } catch (Exception e) {
            gpuDelegate = null; // fallback CPU
        }
        interpreter = new Interpreter(model, options);
    }

    private MappedByteBuffer loadModelFile(Context context, String path) throws IOException {
        AssetFileDescriptor afd = context.getAssets().openFd(path);
        FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
        FileChannel fileChannel = fis.getChannel();
        long startOffset = afd.getStartOffset();
        long declaredLength = afd.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private ByteBuffer preprocess(Bitmap bitmap) {
        Bitmap bmp = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        bb.order(ByteOrder.nativeOrder());
        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bmp.getPixels(intValues, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        for (int px : intValues) {
            int r = (px >> 16) & 0xFF;
            int g = (px >> 8) & 0xFF;
            int b = px & 0xFF;
            // CHÚ Ý: chỉnh lại nếu model yêu cầu [-1,1] thay vì [0,1]
            bb.putFloat(r / 255.0f);
            bb.putFloat(g / 255.0f);
            bb.putFloat(b / 255.0f);
        }
        bb.rewind();
        return bb;
    }

    public float[] predict(Bitmap bitmap) {
        ByteBuffer input = preprocess(bitmap);
        int outSize = interpreter.getOutputTensor(0).shape()[1]; // [1, numClasses]
        float[][] output = new float[1][outSize];
        interpreter.run(input, output);
        return output[0];
    }

    public List<int[]> topK(float[] results, int k) {
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            pairs.add(new int[]{i, Float.floatToIntBits(results[i])});
        }
        Collections.sort(pairs, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                float v1 = Float.intBitsToFloat(o1[1]);
                float v2 = Float.intBitsToFloat(o2[1]);
                return Float.compare(v2, v1); // desc
            }
        });
        return pairs.subList(0, Math.min(k, pairs.size()));
    }

    public void close() {
        if (interpreter != null) interpreter.close();
        if (gpuDelegate != null) gpuDelegate.close();
    }
}

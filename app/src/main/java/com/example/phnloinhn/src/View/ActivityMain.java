package com.example.phnloinhn.src.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.phnloinhn.R;
import com.example.phnloinhn.databinding.ActivityMainBinding;
import com.example.phnloinhn.src.Model.GrowingMethod;
import com.example.phnloinhn.src.Model.History;
import com.example.phnloinhn.src.Model.LonganVariant;
import com.example.phnloinhn.src.Remote.FirestoreHelper;
import com.example.phnloinhn.src.Remote.ResultCallback;
import com.example.phnloinhn.src.Remote.StorageHelper;
import com.example.phnloinhn.src.Utils.Utils;
import com.example.phnloinhn.src.ml.MobilenetClassifier;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.io.IOException;
import java.util.List;

public class ActivityMain extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirestoreHelper db;
    private StorageHelper storage;
    private FirebaseAuth mauth;
    private MobilenetClassifier classifier;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private final String TAG = "ActivityMain";
    private Map<String, LonganVariant> data;
    private List<History> historyList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mauth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore and Google Storage instances
        db = new FirestoreHelper(mauth.getCurrentUser().getUid());
        storage = new StorageHelper();
        data = new HashMap<>();
        historyList = new ArrayList<>();


        // Fetch longan data and history data of user in background.
        fetchVariants();
        db.getAllHistory(new ResultCallback<List<History>>() {
            @Override
            public void onSuccess(List<History> result) {
                historyList = new ArrayList<>(result);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch history", e);
            }
        });

        // Initialize toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_fragmentHistory, R.id.nav_fragmentHome, R.id.nav_fragmentProfile
        ).build();

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);

        NavController navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            Log.e("Main Activity", "navHostFragment is null");
        }

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        // Khởi tạo classifier
        classifier = new MobilenetClassifier(getAssets(), "mobilenetv3_longan_quantized.tflite", "labels.txt", 224);
        try {
            classifier.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // ActivityResultLauncher thay cho startActivityForResult
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            Log.d("TFLITE_RAW","Classify from ActivityMain");
                            // Chạy AI
                            new Thread(() -> {
                                List<MobilenetClassifier.Recognition> predictions = classifier.classify(bitmap);
                                runOnUiThread(() -> handlePrediction(predictions, imageUri));
                            }).start();
                        } catch (IOException e) {
                            Log.e(TAG, "Error when classify: " + e);
                            Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Gallery opens when fabAdd clicked
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
        });
    }

    private void handlePrediction(List<MobilenetClassifier.Recognition> predictions, Uri imageUri) {
        if (!predictions.isEmpty()) {
            MobilenetClassifier.Recognition topResult = predictions.get(0);

            // Use prediction to get information
            String variant_name = topResult.getTitle();
            float confident = topResult.getConfidence();
            // Show info on Home Fragment
            updateFragment(variant_name, confident);
            // Upload image to Storage and history data to Firestore in background
            uploadImage(imageUri, variant_name);

            Toast.makeText(this, "Kết quả: " + topResult.getTitle() + " (" + topResult.getConfidence() + ")", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Không dự đoán được", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFragment(String variantName, float confident) {
        Toast.makeText(this, "Updating fragment", Toast.LENGTH_SHORT).show();
    }


    private void fetchVariants() {
        db.getAllVariants(new ResultCallback<Map<String, LonganVariant>>() {
            @Override
            public void onSuccess(Map<String, LonganVariant> data) {
                ActivityMain.this.data.clear();
                ActivityMain.this.data.putAll(data);
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

    private void uploadImage(Uri uri, String variant_name){
        if (uri == null){
            Log.e(TAG, "No image to upload");
        };
        // User uploads go into /users/{uid}/history/{fileName}
        // Current date-time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        String date = sdf.format(new Date());
        // Assign filename based on variant name and timestamp
        String fileName = variant_name + "_" + date;
        StorageReference path = storage.getUserHistoryRef(fileName);
        Log.d(TAG, "Upload image to path " + path);

        storage.uploadImage(this, path, uri, new ResultCallback<String>() {
            @Override
            public void onSuccess(String url) {
                Log.d(TAG, "Upload image successfully: " + url);

                // upload metadata to firestore ( users/{uid}/history/{history_name: <variant_name>_<date>}
                db.addHistory(new History(variant_name, url, date), new ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (result){
                            Log.d(TAG, "Upload history successfully");
                            updateLocalHistory(new History(variant_name, url, date));
                        }
                    }

                    private void updateLocalHistory(History history) {
                        historyList.add(history);
                        // Notify changes
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error: " + e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Utils.hideLoading();
                Toast.makeText(ActivityMain.this, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "uploadImage failed", e);
            }
        });
    }

    // menu trên toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // xử lý khi bấm menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // handle settings click
            return true;
        } else if (id == R.id.action_logout) {
            logOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // logout firebase
    protected void logOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // Toast confirmation
                            Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();

                            // Clear SharedPreferences credentials
                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            prefs.edit().clear().apply();

                            // Clear Room DB
//                            new Thread(() -> {
//                                MyDatabase.getInstance(getApplicationContext()).clearAllTables();
//                            }).start();

                            // Log sign-out with Firebase Analytics
//                            FirebaseAnalytics.getInstance(getApplicationContext())
//                                    .logEvent("user_sign_out", null);

                            // Go back to LoginActivity
                            Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            // Finish current activity
                            finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Sign-out failed due to Google AuthUI Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


}

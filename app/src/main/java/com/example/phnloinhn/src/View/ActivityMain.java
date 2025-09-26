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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.util.List;

public class ActivityMain extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirestoreHelper db;
    private StorageHelper storage;
    private Map<String, Object> longan_data; // <Variant names, Variant details>
    private MobilenetClassifier classifier;

    private ActivityResultLauncher<Intent> pickImageLauncher;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Access a Cloud Firestore and Google Storage instances
        db = new FirestoreHelper();
        storage = new StorageHelper();

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

                            List<MobilenetClassifier.Recognition> predictions = classifier.classify(bitmap);

                            if (!predictions.isEmpty()) {
                                MobilenetClassifier.Recognition topResult = predictions.get(0);

//                                Add here: use prediction to search for information
//                                        - Show info on Home Fragment

                                Toast.makeText(this, "Kết quả: " + topResult.getTitle() + " (" + topResult.getConfidence() + ")", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Không dự đoán được", Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // ✅ Khi bấm fabAdd thì mở thư viện ảnh
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
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

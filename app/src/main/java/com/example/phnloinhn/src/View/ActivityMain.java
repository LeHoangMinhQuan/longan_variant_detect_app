package com.example.phnloinhn.src.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.phnloinhn.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.phnloinhn.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ActivityMain extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private Map<String, Object> longan_data; // <Variant names, Variant details>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Access a Cloud Firestore instance and initialize data hashmap
        db = FirebaseFirestore.getInstance();
        longan_data = new HashMap<>();

        // Access data on FireStore
        // Get all the variant documents
        db.collection("longan_variants").get()
                .addOnSuccessListener(variantDocs -> {
                    for (QueryDocumentSnapshot variantDoc : variantDocs) {
                        String variantId = variantDoc.getId();
                        Map<String, Object> variantData = new HashMap<>(variantDoc.getData());

                        // Fetch growing_methods subcollection of each variant
                        db.collection("longan_variants")
                                .document(variantId)
                                .collection("growing_methods")
                                .get()
                                .addOnSuccessListener(methodDocs -> {
                                    Map<String, Object> methodsData = new HashMap<>();

                                    for (QueryDocumentSnapshot methodDoc : methodDocs) {
                                        methodsData.put(methodDoc.getId(), methodDoc.getData());
                                    }

                                    // Add growing_methods into the variant data
                                    variantData.put("growing_methods", methodsData);

                                    // Store the full variant data
                                    longan_data.put(variantId, variantData);

                                    // Log for confirmation
                                    Log.d("Fetch Data", variantId + " => " + variantData);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Fetch Data", "Error loading growing_methods for " + variantId, e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Fetch Data", "Error loading longan_variants", e);
                });

        // Initialize toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Replace ActionBar with Toolbar

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_fragmentHistory, R.id.nav_fragmentHome, R.id.nav_fragmentProfile)
                .build();
        // Initialize navHostFragment to prevent it's not fully attached
        // in the lifecycle
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else Log.e("Main Activity", "navHostFragment is null");


        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

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

    protected void logOut(){
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
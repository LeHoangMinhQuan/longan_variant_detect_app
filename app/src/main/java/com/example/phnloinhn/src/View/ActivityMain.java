package com.example.phnloinhn.src.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.phnloinhn.R;
import com.example.phnloinhn.databinding.ActivityMainBinding;
import com.example.phnloinhn.src.ViewModel.SharedViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;

public class ActivityMain extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String TAG = "ActivityMain";

    private SharedViewModel viewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

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
        if (id == R.id.action_logout) {
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

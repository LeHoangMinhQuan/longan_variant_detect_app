package com.example.phnloinhn.src.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.phnloinhn.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ActivityLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;

    // 1. FirebaseUI sign-in launcher
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // 2. Trigger sign-in UI only if user not logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startSignInFlow();
        } else {
            moveToMain(currentUser);
        }
    }

    private void startSignInFlow() {
        // 3. Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
                // You can add more providers here
        );

        // 4. Launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground) // optional
//                .setTheme(R.style.Theme_PhnLoiNhn) // optional
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            moveToMain(user);
        } else {
            if (response == null) {
                Toast.makeText(this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Login", "Error: " + response.getError());
                Toast.makeText(this, "Sign-in error: " + Objects.requireNonNull(response.getError()).getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // 5. Move to MainActivity and pass user info via Intent extras
    private void moveToMain(FirebaseUser user) {
        if (user == null) return;

        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra("userName", user.getDisplayName());
        intent.putExtra("userEmail", user.getEmail());
//      intent.putExtra("userPhoto", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        startActivity(intent);
        finish(); // Optional: prevent going back to login
    }
}

package com.example.phnloinhn.src.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.phnloinhn.R;
import com.example.phnloinhn.databinding.ActivityLoginBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import com.example.phnloinhn.src.Helper.LogInHelper;

public class ActivityLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Play App Check for verify requests to Firebase
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // If user logged in, move to main
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            moveToMain(currentUser);
        }

        // Add listeners for buttons
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.editTextUsername.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();
            if (validateCredentials(email, password) == 1){
                // Show warning above email editText

            }
            else if (validateCredentials(email, password) == 2) {
                // Show warning above password editText
            } else loginUser(email, password);
        });

        binding.signupButton.setOnClickListener(v -> {
            String email = binding.editTextUsername.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();
            if (validateCredentials(email, password) == 1){
                // Show warning above email editText

            }
            else if (validateCredentials(email, password) == 2) {
                // Show warning above password editText
            } else registerUser(email, password); // All credentials are valid
        });

        binding.firebaseUiLoginButton.setOnClickListener(v -> {
            // Sign in with Google or Guest
            startNewSignIn();
        });

    }

    // Validate credentials : 0 - All True, 1 - Email invalid, 2 - Password invalid
    private int validateCredentials(String email, String password) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        // At least 8 characters, 1 uppercase, 1 digit, 1 special character
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/]).{8,}$";

        if (!email.matches(emailRegex)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return 1;
        }

        if (!password.matches(passwordRegex)) {
            Toast.makeText(this, "Mật khẩu cần ít nhất 8 ký tự, 1 chữ in hoa, 1 số và 1 ký tự đặc biệt", Toast.LENGTH_LONG).show();
            return 2;
        }

        return 0;
    }

    private void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        moveToMain(mAuth.getCurrentUser());
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthException) {
                            String errorCode = ((FirebaseAuthException) e).getErrorCode();
                            String message = LogInHelper.PASSWORD_MSGS.getOrDefault(errorCode, "Lỗi không xác định.");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            Log.e("FirebaseError", "Code: " + errorCode + ", Message: " + message);
                        } else {
                            assert e != null;
                            Toast.makeText(this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("FirebaseError", "Exception: ", e);
                        }
                    }
                });
    }

    private void registerUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        moveToMain(mAuth.getCurrentUser());
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthException) {
                            String errorCode = ((FirebaseAuthException) e).getErrorCode();
                            String message = LogInHelper.PASSWORD_MSGS.getOrDefault(errorCode, "Lỗi không xác định.");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            Log.e("FirebaseError", "Code: " + errorCode + ", Message: " + message);
                        } else {
                            assert e != null;
                            Toast.makeText(this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("FirebaseError", "Exception: ", e);
                        }
                    }
                });
    }

    private void startNewSignIn() {
        // 3. Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build()
                // Can add more providers here
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

    // FirebaseUI sign-in launcher
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            moveToMain(user);
        } else {
            if (response == null) {
                Toast.makeText(this, "Đăng nhập bị hủy hoặc email này đã có tài khoản.", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseUiException error = response.getError();
                if (error != null) {
                    Integer errorCode = error.getErrorCode();
                    String message = LogInHelper.GOOGLEUI_MSGS.getOrDefault(errorCode, "Đã xảy ra lỗi: " + error.getLocalizedMessage());
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    Log.e("Login", "Error code: " + errorCode + ", message: " + message);
                } else {
                    // Unknown error
                    Toast.makeText(this, "Đã xảy ra lỗi chưa xác định.", Toast.LENGTH_LONG).show();
                    Log.e("Login", "Unknown error with null FirebaseUiException");
                }
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

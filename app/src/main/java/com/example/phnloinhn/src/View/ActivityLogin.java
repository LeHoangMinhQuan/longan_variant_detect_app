package com.example.phnloinhn.src.View;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.example.phnloinhn.src.Utils.Utils.hideLoading;
import static com.example.phnloinhn.src.Utils.Utils.showLoading;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import com.example.phnloinhn.src.Utils.LogInHelper;

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

        // (Only for production with Play console developer account) Initialize Firebase Play App Check for verify requests to Firebase
//        FirebaseApp.initializeApp(/*context=*/ this);
//        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
//        firebaseAppCheck.installAppCheckProviderFactory(
//                PlayIntegrityAppCheckProviderFactory.getInstance());
        // Debug version of App Check
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());
        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // If user logged in, move to main
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            moveToMain(currentUser);
        }
        addLoginListeners();
    }

    // Add listeners for login buttons
    private void addLoginListeners(){
        // Add listener for forgot password
        binding.forgotPassword.setOnClickListener(v -> handleForgotPassword());
        // Add listeners for buttons
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.editTextUsername.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();
            if (LogInHelper.validateCredentials(ActivityLogin.this, email, password) == LogInHelper.INVALID_EMAIL){
                // Show warning above email editText
                binding.invalidEmailWarning.setVisibility(GONE);
            }
            else if (LogInHelper.validateCredentials(ActivityLogin.this, email, password) == LogInHelper.INVALID_PASSWORD) {
                // Show warning below password editText
                binding.invalidPasswordWarning.setVisibility(VISIBLE);
            } else loginUser(email, password);
        });

        binding.signupSwitch.setOnClickListener(v -> switchToSignUpUI());


        binding.firebaseUiLoginButton.setOnClickListener(v -> {
            // Sign in with Google or Guest
            startNewSignIn();
        });
    }

    private void handleForgotPassword() {
        String email = binding.editTextUsername.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(ActivityLogin.this, "Hãy nhập email cần đổi mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (LogInHelper.validateEmail(ActivityLogin.this, email) == LogInHelper.INVALID_EMAIL){
            // Show warning above email editText
            binding.invalidEmailWarning.setVisibility(VISIBLE);
        } else binding.invalidEmailWarning.setVisibility(GONE);
        showLoading(ActivityLogin.this, "Đang xử lý...");
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        // Show success message
                        Toast.makeText(this, "Gửi email thành công, hãy kiểm tra thư đến và thư rác.", Toast.LENGTH_SHORT).show();
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

    // Hidden sign in button, visualize sign-up related components
    private void switchToSignUpUI() {
        List<View> hideComponents = Arrays.asList(
                binding.loginButton,
                binding.firebaseUiLoginButton,
                binding.signupSwitch,
                binding.forgotPassword
        );
        for (View component : hideComponents) {
            component.setVisibility(GONE);
            component.setOnClickListener(null);
        }
        
        // Display back button
        binding.imageButton2.setVisibility(VISIBLE);
        binding.imageButton2.setOnClickListener(v -> handleSwitchToLogIn());
        // Display signup button
        binding.signupButton.setVisibility(VISIBLE);
        binding.signupButton.setOnClickListener(v -> handleSignUp());
        // Change card title
        binding.logInText.setText(R.string.signin_title);
    }

    private void handleSwitchToLogIn() {
        List<View> hideComponents = Arrays.asList(
                binding.imageButton2,
                binding.signupButton
        );
        List<View> displayComponents = Arrays.asList(
                binding.loginButton,
                binding.firebaseUiLoginButton,
                binding.signupSwitch,
                binding.forgotPassword
        );
        for (View component : hideComponents) {
            component.setVisibility(GONE);
            component.setOnClickListener(null);
        }
        // Change card title to Login
        binding.logInText.setText(R.string.login_title);
        // Display components
        for (View component : displayComponents) {
            component.setVisibility(VISIBLE);
        }
        // Add listeners for buttons
        addLoginListeners();
    }

    private void handleSignUp() {
        String email = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        if (LogInHelper.validateCredentials(ActivityLogin.this, email, password) == LogInHelper.INVALID_EMAIL){
            // Show warning below email editText
            binding.invalidEmailWarning.setVisibility(GONE);
        }
        else if (LogInHelper.validateCredentials(ActivityLogin.this, email, password) == LogInHelper.INVALID_PASSWORD) {
            // Show warning below password editText
            binding.invalidPasswordWarning.setVisibility(VISIBLE);
        } else registerUser(email, password); // All credentials are valid
    }

    private void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(ActivityLogin.this, "Đang xử lý...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideLoading();
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
        showLoading(ActivityLogin.this, "Đang xử lý...");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideLoading();
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
                .setLogo(R.mipmap.app_logo) // optional
                .setTheme(R.style.Theme_PhanLoaiNhan)
                .build();
        signInLauncher.launch(signInIntent);
    }

    // FirebaseUI sign-in launcher
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    showLoading(ActivityLogin.this, "Đang xử lý...");
                    onSignInResult(result);
                }
            }
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        hideLoading();
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

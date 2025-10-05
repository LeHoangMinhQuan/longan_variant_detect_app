package com.quan.phnloinhn.src.View;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.quan.phnloinhn.R;
import com.quan.phnloinhn.databinding.ActivityLoginBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.quan.phnloinhn.src.ViewModel.LoginViewModel;
import com.quan.phnloinhn.src.Utils.Utils;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ActivityLogin extends AppCompatActivity {
    private LoginViewModel viewModel;
    private ActivityLoginBinding binding;
    private String TAG = "ActivityLogin";
    private CredentialManager credentialManager;
    private Executor executor;
    private OnBackPressedCallback backPressedCallback;
    private boolean isInLoginMode = true;

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

        // Initialize Firebase Play App Check
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize Credential Manager for Google Sign-In
        credentialManager = CredentialManager.create(this);
        executor = Executors.newSingleThreadExecutor();

        // Observe ViewModel
        observeViewModel();

        setupTextWatchers();
        setupUI();
        setupBackPressedCallback(); // Add this line

        // Check if user is already logged in
        if (viewModel.getCurrentUser().getValue() != null) {
            moveToMain();
        }
    }

    private void observeViewModel() {
        // Observe current user
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                moveToMain();
            }
        });

        // Observe login state
        viewModel.getLoginState().observe(this, state -> {
            if (state == null) return;

            switch (state) {
                case LOGIN_SUCCESS:
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_SUCCESS:
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    break;
                case RESET_EMAIL_SENT:
                    Toast.makeText(this, "Gửi email thành công! Kiểm tra hộp thư và thư rác của bạn.", Toast.LENGTH_LONG).show();
                    switchToLoginMode();
                    break;
                case GUEST_LOGIN_SUCCESS:
                    Toast.makeText(this, "Đăng nhập với tư cách khách thành công!", Toast.LENGTH_SHORT).show();
                    break;
                case GOOGLE_LOGIN_SUCCESS:
                    Toast.makeText(this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_FAILED:
                case REGISTER_FAILED:
                case RESET_EMAIL_FAILED:
                case GUEST_LOGIN_FAILED:
                case GOOGLE_LOGIN_FAILED:
                    // Error messages are handled by errorMessage LiveData
                    break;
            }
        });

        // Observe validation errors
        viewModel.getValidationError().observe(this, error -> {
            if (error == null) return;

            String errorMsg = viewModel.getErrorMessage().getValue();

            switch (error) {
                case NONE:
                    clearAllErrors();
                    break;
                case EMPTY_EMAIL:
                    if (errorMsg != null) {
                        showError(binding.textInputLayoutUsername, errorMsg);
                    }
                    break;
                case INVALID_EMAIL:
                    if (errorMsg != null) {
                        showError(binding.textInputLayoutUsername, errorMsg);
                    }
                    break;
                case INVALID_PASSWORD:
                    if (errorMsg != null) {
                        showError(binding.textInputLayoutPassword, errorMsg);
                    }
                    break;
                case FIREBASE_EMAIL_ERROR:
                    if (errorMsg != null) {
                        showError(binding.textInputLayoutUsername, errorMsg);
                    }
                    break;
                case FIREBASE_PASSWORD_ERROR:
                    if (errorMsg != null) {
                        showError(binding.textInputLayoutPassword, errorMsg);
                    }
                    break;
            }
        });

        // Observe error messages for general errors (not field-specific)
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                LoginViewModel.ValidationError error = viewModel.getValidationError().getValue();
                // Only show toast if it's not a field-specific error
                if (error == null || error == LoginViewModel.ValidationError.NONE) {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                Utils.showLoading(this, "Đang xử lý...");
            } else {
                Utils.hideLoading();
            }
        });
    }

    // Setup TextWatchers to auto-clear errors when user types
    private void setupTextWatchers() {
        binding.editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearError(binding.textInputLayoutUsername);
                viewModel.clearValidationError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearError(binding.textInputLayoutPassword);
                viewModel.clearValidationError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupUI() {
        // Back button - initially hidden, will show in sign up mode
        binding.imageButton2.setVisibility(GONE);
        binding.imageButton2.setOnClickListener(v -> switchToLoginMode());

        // Login button
        binding.loginButton.setOnClickListener(v -> handleLogin());

        // Forgot password
        binding.forgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Sign up switch button
        binding.signupSwitch.setOnClickListener(v -> switchToSignUpMode());

        // Google login button
        binding.buttonLoginGoogle.setOnClickListener(v -> handleGoogleLogin());

        // Guest login button
        binding.buttonLoginGuest.setOnClickListener(v -> handleGuestLogin());
    }

    private void setupBackPressedCallback() {
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                switchToLoginMode();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    // Helper method to set error and request focus
    private void showError(TextInputLayout layout, String message) {
        layout.setError(message);
        layout.requestFocus();
    }

    // Helper method to clear error
    private void clearError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    // Helper method to clear all errors
    private void clearAllErrors() {
        clearError(binding.textInputLayoutUsername);
        clearError(binding.textInputLayoutPassword);
    }

    // Helper method to clear all input fields
    private void clearAllInputs() {
        Objects.requireNonNull(binding.editTextUsername.getText()).clear();
        Objects.requireNonNull(binding.editTextPassword.getText()).clear();
    }

    private void handleLogin() {
        String email = Objects.requireNonNull(binding.editTextUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString().trim();

        clearAllErrors();

        // Validate using ViewModel
        if (!viewModel.validateCredentials(email, password)) {
            return;
        }

        // Proceed with login
        viewModel.loginUser(email, password);
    }

    private void handleForgotPassword() {
        clearAllErrors();
        clearAllInputs();

        // Hide unrelated UI elements
        binding.editTextPassword.setVisibility(GONE);
        binding.textInputLayoutPassword.setVisibility(GONE);
        binding.buttonLoginGoogle.setVisibility(GONE);
        binding.buttonLoginGuest.setVisibility(GONE);
        binding.forgotPassword.setVisibility(GONE);
        binding.dividerLayout.setVisibility(GONE);
        binding.signupSwitch.setVisibility(GONE);

        // Show back button
//        binding.imageButton2.setVisibility(VISIBLE);

        // Change title and button
        binding.logInText.setText(R.string.forgot_password_title);
        binding.loginButton.setText(R.string.send_email_title);
        binding.loginButton.setOnClickListener(v -> sendForgotPasswordEmail());

        // Enable back press callback when not in login mode
        isInLoginMode = false;
        backPressedCallback.setEnabled(true);
    }


    private void sendForgotPasswordEmail() {
        String email = Objects.requireNonNull(binding.editTextUsername.getText()).toString().trim();

        // Validate using ViewModel
        if (!viewModel.validateEmail(email)) {
            return;
        }

        // Send reset email
        viewModel.sendPasswordResetEmail(email);
    }

    private void switchToSignUpMode() {
        clearAllErrors();
        clearAllInputs();

        // Show back button
//        binding.imageButton2.setVisibility(VISIBLE);

        // Hide login-specific UI
        binding.buttonLoginGoogle.setVisibility(GONE);
        binding.buttonLoginGuest.setVisibility(GONE);
        binding.forgotPassword.setVisibility(GONE);
        binding.dividerLayout.setVisibility(GONE);
        binding.signupSwitch.setVisibility(GONE);

        // Update title and button
        binding.logInText.setText(R.string.signin_title);
        binding.loginButton.setText(R.string.signin_title);
        binding.loginButton.setOnClickListener(v -> handleSignUp());

        // Enable back press callback when not in login mode
        isInLoginMode = false;
        backPressedCallback.setEnabled(true);
    }

    private void switchToLoginMode() {
        clearAllErrors();
        clearAllInputs();

        // Hide back button
        binding.imageButton2.setVisibility(GONE);

        // Show all login UI elements
        binding.editTextPassword.setVisibility(VISIBLE);
        binding.textInputLayoutPassword.setVisibility(VISIBLE);
        binding.buttonLoginGoogle.setVisibility(VISIBLE);
        binding.buttonLoginGuest.setVisibility(VISIBLE);
        binding.forgotPassword.setVisibility(VISIBLE);
        binding.signupSwitch.setVisibility(VISIBLE);

        // Update title and button
        binding.logInText.setText(R.string.login_title);
        binding.loginButton.setText(R.string.login_title);
        binding.loginButton.setOnClickListener(v -> handleLogin());

        // Disable back press callback when in login mode
        isInLoginMode = true;
        backPressedCallback.setEnabled(false);
    }

    private void handleSignUp() {
        String email = Objects.requireNonNull(binding.editTextUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString().trim();

        clearAllErrors();

        // Validate using ViewModel
        if (!viewModel.validateCredentials(email, password)) {
            return;
        }

        // Proceed with registration
        viewModel.registerUser(email, password);
    }

    private void handleGuestLogin() {
        viewModel.loginAsGuest();
    }

    // Google Sign-In implementation
    private void handleGoogleLogin() {
        // Get your Web Client ID from Firebase Console
        String webClientId = getString(R.string.default_web_client_id);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Google Sign-In failed", e);
                            Toast.makeText(ActivityLogin.this,
                                    "Đăng nhập Google thất bại: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void handleGoogleSignInResult(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential) {
            if (TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

                    String idToken = googleIdTokenCredential.getIdToken();

                    runOnUiThread(() -> {
                        // Pass the ID token to ViewModel
                        viewModel.signInWithGoogle(idToken);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Invalid Google ID token", e);
                        Toast.makeText(ActivityLogin.this,
                                "Đăng nhập Google thất bại",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Log.e(TAG, "Unexpected credential type: " + credential.getType());
                    Toast.makeText(ActivityLogin.this,
                            "Đăng nhập Google thất bại",
                            Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            runOnUiThread(() -> {
                Log.e(TAG, "Unexpected credential class: " + credential.getClass().getName());
                Toast.makeText(ActivityLogin.this,
                        "Đăng nhập Google thất bại",
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void moveToMain() {
        Intent intent = new Intent(this, ActivityMain.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up loading dialog
        Utils.hideLoading();
        // Shutdown executor
        if (executor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) executor).shutdown();
        }
    }
}
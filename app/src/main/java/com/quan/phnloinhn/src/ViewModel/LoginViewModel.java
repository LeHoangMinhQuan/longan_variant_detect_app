package com.quan.phnloinhn.src.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.quan.phnloinhn.src.Utils.LogInHelper;

public class LoginViewModel extends AndroidViewModel {

    private final FirebaseAuth mAuth;
    private final String TAG = "LoginViewModel";

    // LiveData for UI state
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ValidationError> validationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUser.setValue(user);
        }
    }

    // Expose LiveData
    public LiveData<FirebaseUser> getCurrentUser() { return currentUser; }
    public LiveData<LoginState> getLoginState() { return loginState; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<ValidationError> getValidationError() { return validationError; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // Validate credentials
    public boolean validateCredentials(String email, String password) {
        int validationResult = LogInHelper.validateCredentials(email, password);

        if (validationResult == LogInHelper.INVALID_EMAIL) {
            validationError.setValue(ValidationError.INVALID_EMAIL);
            errorMessage.setValue("Email không hợp lệ");
            return true;
        }

        if (validationResult == LogInHelper.INVALID_PASSWORD) {
            validationError.setValue(ValidationError.INVALID_PASSWORD);
            errorMessage.setValue("Mật khẩu cần ít nhất 8 ký tự, 1 chữ in hoa, 1 số và 1 ký tự đặc biệt");
            return true;
        }

        validationError.setValue(ValidationError.NONE);
        return false;
    }

    // Validate email only
    public boolean validateEmail(String email) {
        if (email.isEmpty()) {
            validationError.setValue(ValidationError.EMPTY_EMAIL);
            errorMessage.setValue("Hãy nhập email để đặt lại mật khẩu");
            return false;
        }

        int validationResult = LogInHelper.validateEmail(email);

        if (validationResult == LogInHelper.INVALID_EMAIL) {
            validationError.setValue(ValidationError.INVALID_EMAIL);
            errorMessage.setValue("Email không hợp lệ");
            return false;
        }

        validationError.setValue(ValidationError.NONE);
        return true;
    }

    // Clear validation errors
    public void clearValidationError() {
        validationError.setValue(ValidationError.NONE);
    }

    // Login user
    public void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập email và mật khẩu");
            return;
        }

        isLoading.setValue(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        currentUser.setValue(mAuth.getCurrentUser());
                        loginState.setValue(LoginState.LOGIN_SUCCESS);
                    } else {
                        handleFirebaseException(task.getException());
                        loginState.setValue(LoginState.LOGIN_FAILED);
                    }
                });
    }

    // Register user
    public void registerUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập email và mật khẩu");
            return;
        }

        isLoading.setValue(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        currentUser.setValue(mAuth.getCurrentUser());
                        loginState.setValue(LoginState.REGISTER_SUCCESS);
                    } else {
                        handleFirebaseException(task.getException());
                        loginState.setValue(LoginState.REGISTER_FAILED);
                    }
                });
    }

    // Send password reset email
    public void sendPasswordResetEmail(String email) {
        isLoading.setValue(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        loginState.setValue(LoginState.RESET_EMAIL_SENT);
                    } else {
                        handleFirebaseException(task.getException());
                        loginState.setValue(LoginState.RESET_EMAIL_FAILED);
                    }
                });
    }

    // Login as guest
    public void loginAsGuest() {
        isLoading.setValue(true);
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        currentUser.setValue(mAuth.getCurrentUser());
                        loginState.setValue(LoginState.GUEST_LOGIN_SUCCESS);
                    } else {
                        handleFirebaseException(task.getException());
                        loginState.setValue(LoginState.GUEST_LOGIN_FAILED);
                    }
                });
    }

    // Google Sign-In with ID Token
    public void signInWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            errorMessage.setValue("Google Sign-In thất bại: Token không hợp lệ");
            loginState.setValue(LoginState.GOOGLE_LOGIN_FAILED);
            return;
        }

        isLoading.setValue(true);

        // Create credential from Google ID token
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // Sign in with Firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        currentUser.setValue(mAuth.getCurrentUser());
                        loginState.setValue(LoginState.GOOGLE_LOGIN_SUCCESS);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        handleFirebaseException(task.getException());
                        loginState.setValue(LoginState.GOOGLE_LOGIN_FAILED);
                    }
                });
    }

    // Handle Firebase exceptions
    private void handleFirebaseException(Exception e) {
        if (e instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) e).getErrorCode();
            String message = LogInHelper.PASSWORD_MSGS.getOrDefault(errorCode, "Lỗi không xác định.");

            // Determine which field has the error
            if (errorCode.contains("email") || errorCode.contains("user")) {
                validationError.setValue(ValidationError.FIREBASE_EMAIL_ERROR);
                errorMessage.setValue(message);
            } else if (errorCode.contains("password")) {
                validationError.setValue(ValidationError.FIREBASE_PASSWORD_ERROR);
                errorMessage.setValue(message);
            } else {
                errorMessage.setValue(message);
            }
        } else if (e != null) {
            errorMessage.setValue("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    // Enums for state management
    public enum LoginState {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        REGISTER_SUCCESS,
        REGISTER_FAILED,
        RESET_EMAIL_SENT,
        RESET_EMAIL_FAILED,
        GUEST_LOGIN_SUCCESS,
        GUEST_LOGIN_FAILED,
        GOOGLE_LOGIN_SUCCESS,
        GOOGLE_LOGIN_FAILED
    }

    public enum ValidationError {
        NONE,
        EMPTY_EMAIL,
        INVALID_EMAIL,
        INVALID_PASSWORD,
        FIREBASE_EMAIL_ERROR,
        FIREBASE_PASSWORD_ERROR
    }
}
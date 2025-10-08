package com.quan.phnloinhn.src.View.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.quan.phnloinhn.R;
import com.quan.phnloinhn.src.Utils.Utils;
import com.quan.phnloinhn.src.ViewModel.SharedViewModel;

import java.util.Objects;

public class FragmentProfile extends Fragment {
    private SharedViewModel viewModel;
    private ShapeableImageView profileAvatar;
    private TextView profileDisplayName;
    private TextView profileEmail;
    private Button buttonChangePassword;
    private Button buttonChangeDisplayName;
    private Button buttonLinkEmail;
    private Button buttonLogOut;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel= new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        initializeViews(view);
        initializeFirebase();
        setupUserInterface();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        profileAvatar = view.findViewById(R.id.profile_avatar);
        profileDisplayName = view.findViewById(R.id.profile_display_name);
        profileEmail = view.findViewById(R.id.profile_email);
        buttonChangePassword = view.findViewById(R.id.button_change_password);
        buttonChangeDisplayName = view.findViewById(R.id.button_change_display_name);
        buttonLinkEmail = view.findViewById(R.id.button_link_email);
        buttonLogOut = view.findViewById(R.id.button_log_out);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setupUserInterface() {
        if (currentUser != null) {
            loadUserAvatar(currentUser);
            displayUserInfo(currentUser);
            configureButtonVisibility(currentUser);
        } else {
            Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserAvatar(FirebaseUser user) {
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.longan_leaves_background)
                    .into(profileAvatar);
        } else {
            profileAvatar.setImageResource(R.drawable.longan_leaves_background);
        }
    }

    private void displayUserInfo(FirebaseUser user) {
        String displayName = user.getDisplayName();
        if (TextUtils.isEmpty(displayName)) {
            displayName = getString(R.string.anonymous_user);
        }
        profileDisplayName.setText(displayName);

        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            email = getString(R.string.no_email_linked);
        }
        profileEmail.setText(email);
    }

    private void configureButtonVisibility(FirebaseUser user) {
        boolean isAnonymous = user.isAnonymous();
        boolean hasEmailProvider = false;
        boolean hasGoogleProvider = false;

        for (int i = 0; i < user.getProviderData().size(); i++) {
            String providerId = user.getProviderData().get(i).getProviderId();
            if (EmailAuthProvider.PROVIDER_ID.equals(providerId)) {
                hasEmailProvider = true;
            }
            if (GoogleAuthProvider.PROVIDER_ID.equals(providerId)) {
                hasGoogleProvider = true;
            }
        }

        if (isAnonymous) {
            // Anonymous user: show only link email option
            buttonChangePassword.setVisibility(View.GONE);
            buttonChangeDisplayName.setVisibility(View.GONE);
            buttonLinkEmail.setVisibility(View.VISIBLE);
        } else if (hasEmailProvider) {
            // Email/Password user: show password and display name options
            buttonChangePassword.setVisibility(View.VISIBLE);
            buttonChangeDisplayName.setVisibility(View.VISIBLE);
            buttonLinkEmail.setVisibility(View.GONE);
        } else if (hasGoogleProvider) {
            // Google user: show only display name (can't change Google password)
            buttonChangePassword.setVisibility(View.GONE);
            buttonChangeDisplayName.setVisibility(View.VISIBLE);
            buttonLinkEmail.setVisibility(View.GONE);
        } else {
            // Default: hide all conditional buttons
            buttonChangePassword.setVisibility(View.GONE);
            buttonChangeDisplayName.setVisibility(View.GONE);
            buttonLinkEmail.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        buttonChangeDisplayName.setOnClickListener(v -> showChangeDisplayNameDialog());
        buttonLinkEmail.setOnClickListener(v -> showLinkEmailDialog());
        buttonLogOut.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);

        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.current_password);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.new_password);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirm_new_password);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit_change_password);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnSubmit.setOnClickListener(v -> {
            String currentPassword = Objects.requireNonNull(currentPasswordInput.getText()).toString().trim();
            String newPassword = Objects.requireNonNull(newPasswordInput.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(confirmPasswordInput.getText()).toString().trim();

            if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                dialog.dismiss();
                changePassword(currentPassword, newPassword);
            }
        });

        dialog.show();
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(requireContext(), getString(R.string.enter_current_password), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(requireContext(), getString(R.string.enter_new_password), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(requireContext(), getString(R.string.password_min_length), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void changePassword(String currentPassword, String newPassword) {
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        Utils.showLoading(requireContext(), getString(R.string.processing));

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    Utils.hideLoading();
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(requireContext(), getString(R.string.password_changed_success), Toast.LENGTH_SHORT).show();
                                        logOut();
                                    } else {
                                        String errorMessage = updateTask.getException() != null ?
                                                updateTask.getException().getMessage() : "Unknown error";
                                        Toast.makeText(requireContext(), getString(R.string.password_change_failed, errorMessage), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Utils.hideLoading();
                        Toast.makeText(requireContext(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showChangeDisplayNameDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_display_name, null);

        TextInputEditText input = dialogView.findViewById(R.id.et_new_display_name);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit_change_display_name);

        // Pre-fill with current display name
        if (currentUser != null && currentUser.getDisplayName() != null) {
            input.setText(currentUser.getDisplayName());
            input.setSelection(Objects.requireNonNull(input.getText()).length());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnSubmit.setOnClickListener(v -> {
            String newDisplayName = Objects.requireNonNull(input.getText()).toString().trim();
            if (!TextUtils.isEmpty(newDisplayName)) {
                dialog.dismiss();
                changeDisplayName(newDisplayName);
            } else {
                Toast.makeText(requireContext(), getString(R.string.display_name_empty), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void changeDisplayName(String newDisplayName) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        Utils.showLoading(requireContext(), getString(R.string.processing));

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    Utils.hideLoading();
                    if (task.isSuccessful()) {
                        profileDisplayName.setText(newDisplayName);
                        Toast.makeText(requireContext(), getString(R.string.display_name_updated), Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(requireContext(), getString(R.string.display_name_update_failed, errorMessage), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLinkEmailDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_link_email, null);

        TextInputEditText emailInput = dialogView.findViewById(R.id.et_link_email);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.et_link_password);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit_link_email);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnSubmit.setOnClickListener(v -> {
            String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

            if (validateEmailLink(email, password)) {
                dialog.dismiss();
                linkEmailToAccount(email, password);
            }
        });

        dialog.show();
    }

    private boolean validateEmailLink(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(requireContext(), getString(R.string.password_min_length), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void linkEmailToAccount(String email, String password) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        Utils.showLoading(requireContext(), getString(R.string.processing));

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        currentUser.linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    Utils.hideLoading();
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), getString(R.string.email_linked_success), Toast.LENGTH_SHORT).show();
                        // Refresh user data
                        currentUser = auth.getCurrentUser();
                        setupUserInterface();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(requireContext(), getString(R.string.email_link_failed, errorMessage), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.logout_title))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.logout_title), (dialog, which) -> logOut())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void logOut() {
        viewModel.logoutEvent.setValue(true);
        Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideLoading();
    }
}
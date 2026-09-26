package com.example.smartpantrymanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.User;
import com.example.smartpantrymanager.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvChangeAvatar, tvProfileName, tvProfileEmail;
    private MaterialCardView cardEditProfile, cardChangePassword;
    private Button btnSignOut;
    private SessionManager sessionManager;
    private AppDatabase db;
    private User currentUser;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToSignIn();
            return;
        }

        db = AppDatabase.getInstance(this);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardChangePassword = findViewById(R.id.cardChangePassword);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Setup gallery picker result launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) {}

                            currentUser.setAvatarUri(imageUri.toString());
                            db.userDao().update(currentUser);
                            imgAvatar.setImageURI(imageUri);
                            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        View.OnClickListener pickImageListener = v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        };

        imgAvatar.setOnClickListener(pickImageListener);
        tvChangeAvatar.setOnClickListener(pickImageListener);

        loadUserData();

        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnSignOut.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            redirectToSignIn();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            redirectToSignIn();
            return;
        }
        loadUserData();
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        currentUser = db.userDao().findById(userId);
        if (currentUser != null) {
            tvProfileName.setText(currentUser.getFullName());
            tvProfileEmail.setText(currentUser.getEmail());
            if (currentUser.getAvatarUri() != null && !currentUser.getAvatarUri().isEmpty()) {
                try {
                    imgAvatar.setImageURI(Uri.parse(currentUser.getAvatarUri()));
                } catch (Exception e) {
                    imgAvatar.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                imgAvatar.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    private void showChangePasswordDialog() {
        if (currentUser == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String currentPass = etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";
                    String newPass = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
                    String confirmPass = etConfirmNewPassword.getText() != null ? etConfirmNewPassword.getText().toString().trim() : "";

                    if (!currentUser.getPassword().equals(currentPass)) {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                        Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentUser.setPassword(newPass);
                    db.userDao().update(currentUser);
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

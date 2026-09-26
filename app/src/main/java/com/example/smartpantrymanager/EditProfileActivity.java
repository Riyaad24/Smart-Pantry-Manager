package com.example.smartpantrymanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.User;
import com.example.smartpantrymanager.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etEditFullName, etEditEmail;
    private Button btnSaveProfile;
    private SessionManager sessionManager;
    private AppDatabase db;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_edit_profile);

        db = AppDatabase.getInstance(this);

        etEditFullName = findViewById(R.id.etEditFullName);
        etEditEmail = findViewById(R.id.etEditEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        int userId = sessionManager.getUserId();
        currentUser = db.userDao().findById(userId);

        if (currentUser != null) {
            etEditFullName.setText(currentUser.getFullName());
            etEditEmail.setText(currentUser.getEmail());
        }

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String name = etEditFullName.getText() != null ? etEditFullName.getText().toString().trim() : "";
        String email = etEditEmail.getText() != null ? etEditEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etEditFullName.setError("Name cannot be empty");
            etEditFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEditEmail.setError("Email cannot be empty");
            etEditEmail.requestFocus();
            return;
        }

        // Prevent changing email to one already registered to another user
        User existingUser = db.userDao().findByEmail(email);
        if (existingUser != null && existingUser.getId() != currentUser.getId()) {
            etEditEmail.setError("Email is already registered to another account");
            etEditEmail.requestFocus();
            return;
        }

        currentUser.setFullName(name);
        currentUser.setEmail(email);
        db.userDao().update(currentUser);

        sessionManager.createLoginSession(currentUser.getId(), email);
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}

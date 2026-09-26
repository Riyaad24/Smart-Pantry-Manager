package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.User;
import com.example.smartpantrymanager.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvSignUp, tvForgotPassword;
    private SessionManager sessionManager;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, PantryListActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_sign_in);

        db = AppDatabase.getInstance(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnSignIn.setOnClickListener(v -> attemptSignIn());

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email address first", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Reset Password")
                        .setMessage("Password reset instructions have been sent to " + email)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void attemptSignIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        User user = db.userDao().findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            sessionManager.createLoginSession(user.getId(), user.getEmail());
            Toast.makeText(this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignInActivity.this, PantryListActivity.class);
            intent.putExtra("user_name", user.getFullName());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
        }
    }
}

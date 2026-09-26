package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchExpiryAlert;
    private RadioGroup radioUnits;
    private RadioButton radioMetric, radioImperial;
    private TextView txtPantryCount, txtRecipeCount;
    private MaterialCardView cardManageProfile;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        switchExpiryAlert = findViewById(R.id.switchExpiryAlert);
        radioUnits = findViewById(R.id.radioUnits);
        radioMetric = findViewById(R.id.radioMetric);
        radioImperial = findViewById(R.id.radioImperial);
        txtPantryCount = findViewById(R.id.txtProfilePantryCount);
        txtRecipeCount = findViewById(R.id.txtProfileRecipeCount);
        cardManageProfile = findViewById(R.id.cardManageProfile);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        SharedPreferences prefs = getSharedPreferences("SmartPantryPrefs", MODE_PRIVATE);
        switchExpiryAlert.setChecked(prefs.getBoolean("expiry_alert", true));
        String units = prefs.getString("units", "Metric");
        if (units.equals("Metric")) radioMetric.setChecked(true);
        else radioImperial.setChecked(true);

        switchExpiryAlert.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("expiry_alert", isChecked).apply());

        radioUnits.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMetric) prefs.edit().putString("units", "Metric").apply();
            else prefs.edit().putString("units", "Imperial").apply();
        });

        cardManageProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
        });

        // Initialize high fidelity user kitchen stats from local Room repository scoped to current user
        AppDatabase db = AppDatabase.getInstance(this);
        int userId = sessionManager.getUserId();
        int pCount = db.pantryDao().getAllForUser(userId).size();
        int rCount = db.recipeDao().getAll().size();
        txtPantryCount.setText(String.valueOf(pCount));
        txtRecipeCount.setText(String.valueOf(rCount));

        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, PantryListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        AppDatabase db = AppDatabase.getInstance(this);
        int userId = sessionManager.getUserId();
        int pCount = db.pantryDao().getAllForUser(userId).size();
        txtPantryCount.setText(String.valueOf(pCount));
    }
}

package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchExpiryAlert;
    private RadioGroup radioUnits;
    private RadioButton radioMetric, radioImperial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchExpiryAlert = findViewById(R.id.switchExpiryAlert);
        radioUnits = findViewById(R.id.radioUnits);
        radioMetric = findViewById(R.id.radioMetric);
        radioImperial = findViewById(R.id.radioImperial);
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

        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }
}
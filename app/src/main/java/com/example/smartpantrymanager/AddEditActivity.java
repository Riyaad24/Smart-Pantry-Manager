package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.PantryItem;

public class AddEditActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtQuantity, edtUnit, edtExpiry;
    private TextInputLayout layoutName, layoutQty, layoutUnit;
    private MaterialButton btnSave;

    private AppDatabase db;
    private int editItemId = -1; // -1 means ADD mode, else EDIT mode
    private PantryItem existingItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        // Init views
        edtName = findViewById(R.id.edtName);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtUnit = findViewById(R.id.edtUnit);
        edtExpiry = findViewById(R.id.edtExpiry);
        layoutName = findViewById(R.id.layoutName);
        layoutQty = findViewById(R.id.layoutQty);
        layoutUnit = findViewById(R.id.layoutUnit);
        btnSave = findViewById(R.id.btnSave);

        db = AppDatabase.getInstance(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAddEdit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // CHECK: Are we editing? Data comes via Intent from PantryList
        Intent intent = getIntent();
        if (intent.hasExtra("pantry_id")) {
            editItemId = intent.getIntExtra("pantry_id", -1);
            String name = intent.getStringExtra("pantry_name");
            double qty = intent.getDoubleExtra("pantry_qty", 0);
            String unit = intent.getStringExtra("pantry_unit");
            String expiry = intent.getStringExtra("pantry_expiry");

            edtName.setText(name);
            edtQuantity.setText(String.valueOf(qty));
            edtUnit.setText(unit);
            edtExpiry.setText(expiry);
            btnSave.setText("Update Ingredient");

            // Fetch full object for update
            for (PantryItem item : db.pantryDao().getAll()) {
                if (item.getId() == editItemId) {
                    existingItem = item;
                    break;
                }
            }
        }

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveToDatabase();
            }
        });
    }

    // VALIDATION - Required for marks
    private boolean validateInputs() {
        boolean isValid = true;

        String name = edtName.getText().toString().trim();
        String qtyStr = edtQuantity.getText().toString().trim();
        String unit = edtUnit.getText().toString().trim();

        // Name validation
        if (name.isEmpty()) {
            layoutName.setError("Ingredient name is required");
            isValid = false;
        } else if (name.length() < 2) {
            layoutName.setError("Name too short");
            isValid = false;
        } else {
            layoutName.setError(null);
        }

        // Quantity validation
        if (qtyStr.isEmpty()) {
            layoutQty.setError("Quantity required");
            isValid = false;
        } else {
            try {
                double qty = Double.parseDouble(qtyStr);
                if (qty <= 0) {
                    layoutQty.setError("Quantity must be > 0");
                    isValid = false;
                } else {
                    layoutQty.setError(null);
                }
            } catch (NumberFormatException e) {
                layoutQty.setError("Enter a valid number");
                isValid = false;
            }
        }

        // Unit validation
        if (unit.isEmpty()) {
            layoutUnit.setError("Unit required e.g kg, pcs");
            isValid = false;
        } else {
            layoutUnit.setError(null);
        }

        // Expiry is optional, but if entered validate format YYYY-MM-DD
        String expiry = edtExpiry.getText().toString().trim();
        if (!expiry.isEmpty() && !expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            edtExpiry.setError("Use YYYY-MM-DD format");
            isValid = false;
        }

        return isValid;
    }

    private void saveToDatabase() {
        String name = edtName.getText().toString().trim();
        double quantity = Double.parseDouble(edtQuantity.getText().toString().trim());
        String unit = edtUnit.getText().toString().trim();
        String expiry = edtExpiry.getText().toString().trim();

        if (editItemId == -1) {
            // CREATE
            PantryItem newItem = new PantryItem(name, quantity, unit, expiry);
            db.pantryDao().insert(newItem);
            Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            // UPDATE
            existingItem.name = name;
            existingItem.quantity = quantity;
            existingItem.unit = unit;
            existingItem.expiryDate = expiry;
            db.pantryDao().update(existingItem);
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }

        // Go back to Pantry List - prove data persists
        Intent intent = new Intent(AddEditActivity.this, PantryListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
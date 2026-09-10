package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.PantryRepository;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.Calendar;

/**
 * Add / Edit Ingredient screen - reused for both flows. If a pantry item id
 * is passed in via the intent, the form is pre-filled and Save performs an
 * UPDATE; otherwise Save performs an INSERT (Create). Includes input
 * validation as required in Section 3.1.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"unit", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};

    private PantryRepository repository;
    private EditText editName, editQuantity, editExpiry;
    private AutoCompleteTextView spinnerUnit;
    private long editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        repository = new PantryRepository(this);

        Toolbar toolbar = findViewById(R.id.toolbarAddEdit);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, UNITS);
        spinnerUnit.setAdapter(unitAdapter);
        spinnerUnit.setText(UNITS[0], false);

        editExpiry.setFocusable(false);
        editExpiry.setOnClickListener(v -> showDatePicker());

        editingId = getIntent().getLongExtra(MainActivity.EXTRA_PANTRY_ITEM_ID, -1);
        if (editingId != -1) {
            setTitle(R.string.title_edit_ingredient);
            loadExistingItem(editingId);
            View deleteButton = findViewById(R.id.buttonDelete);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> confirmDelete());
        } else {
            setTitle(R.string.title_add_ingredient);
        }

        findViewById(R.id.buttonSave).setOnClickListener(v -> saveIngredient());
    }

    private void loadExistingItem(long id) {
        PantryItem item = repository.getPantryItem(id);
        if (item == null) return;
        editName.setText(item.getName());
        editQuantity.setText(formatQuantity(item.getQuantity()));
        spinnerUnit.setText(item.getUnit(), false);
        editExpiry.setText(item.getExpiryDate());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, day);
            editExpiry.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveIngredient() {
        String name = editName.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();
        String unit = spinnerUnit.getText().toString().trim();
        String expiry = editExpiry.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editName.setError("Ingredient name is required");
            editName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(quantityText)) {
            editQuantity.setError("Quantity is required");
            editQuantity.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            editQuantity.setError("Enter a valid number");
            editQuantity.requestFocus();
            return;
        }
        if (quantity <= 0) {
            editQuantity.setError("Quantity must be greater than zero");
            editQuantity.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(unit)) {
            unit = UNITS[0];
        }

        PantryItem item = new PantryItem(editingId, name, quantity, unit, expiry.isEmpty() ? null : expiry);
        if (editingId == -1) {
            repository.addPantryItem(item);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            repository.updatePantryItem(item);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        repository.deletePantryItem(editingId);
        Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String formatQuantity(double q) {
        if (q == Math.floor(q)) return String.valueOf((long) q);
        return String.valueOf(q);
    }
}
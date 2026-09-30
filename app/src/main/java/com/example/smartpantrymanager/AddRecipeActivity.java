package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class AddRecipeActivity extends AppCompatActivity {

    private TextInputEditText edtRecipeName, edtIngredients, edtSteps;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_add_recipe);

        db = AppDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddRecipe);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        edtRecipeName = findViewById(R.id.edtRecipeName);
        edtIngredients = findViewById(R.id.edtIngredients);
        edtSteps = findViewById(R.id.edtSteps);
        Button btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        btnSaveRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void saveRecipe() {
        String name = edtRecipeName.getText() != null ? edtRecipeName.getText().toString().trim() : "";
        String ingredients = edtIngredients.getText() != null ? edtIngredients.getText().toString().trim() : "";
        String steps = edtSteps.getText() != null ? edtSteps.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            edtRecipeName.setError("Recipe name is required");
            edtRecipeName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ingredients)) {
            edtIngredients.setError("Ingredients are required");
            edtIngredients.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(steps)) {
            edtSteps.setError("Preparation steps are required");
            edtSteps.requestFocus();
            return;
        }

        Recipe newRecipe = new Recipe(name, ingredients, steps);
        db.recipeDao().insert(newRecipe);

        Toast.makeText(this, "Custom recipe '" + name + "' added successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

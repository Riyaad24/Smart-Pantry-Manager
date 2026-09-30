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
    private Button btnSaveRecipe;
    private AppDatabase db;
    private int editRecipeId = -1;
    private Recipe existingRecipe = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SessionManager sessionManager = new SessionManager(this);
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
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        edtRecipeName = findViewById(R.id.edtRecipeName);
        edtIngredients = findViewById(R.id.edtIngredients);
        edtSteps = findViewById(R.id.edtSteps);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        Intent intent = getIntent();
        if (intent.hasExtra("recipe_id")) {
            editRecipeId = intent.getIntExtra("recipe_id", -1);
            String name = intent.getStringExtra("recipe_name");
            String ingredients = intent.getStringExtra("recipe_ingredients");
            String steps = intent.getStringExtra("recipe_steps");

            edtRecipeName.setText(name);
            edtIngredients.setText(ingredients);
            edtSteps.setText(steps);
            btnSaveRecipe.setText("Update Custom Recipe");

            existingRecipe = db.recipeDao().getById(editRecipeId);
        }

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

        if (editRecipeId == -1) {
            // CREATE
            Recipe newRecipe = new Recipe(name, ingredients, steps);
            db.recipeDao().insert(newRecipe);
            Toast.makeText(this, "Custom recipe '" + name + "' added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // UPDATE
            if (existingRecipe != null) {
                existingRecipe.name = name;
                existingRecipe.ingredientsCsv = ingredients;
                existingRecipe.steps = steps;
                db.recipeDao().update(existingRecipe);
                Toast.makeText(this, "Custom recipe '" + name + "' updated successfully!", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}

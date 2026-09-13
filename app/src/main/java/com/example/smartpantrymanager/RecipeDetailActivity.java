package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.Recipe;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        AppDatabase db = AppDatabase.getInstance(this);
        Recipe recipe = db.recipeDao().getById(recipeId);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            getSupportActionBar().setTitle(""); // Name is shown in large text below
        }

        TextView txtName = findViewById(R.id.txtRecipeName);
        TextView txtIngredients = findViewById(R.id.txtIngredients);
        TextView txtSteps = findViewById(R.id.txtSteps);

        if (recipe != null) {
            txtName.setText(recipe.name);
            txtIngredients.setText(recipe.ingredientsCsv.replace(",", "\n"));
            txtSteps.setText(recipe.steps);
        }
    }
}
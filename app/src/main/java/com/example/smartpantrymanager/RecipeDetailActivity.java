package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.PantryRepository;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

/** Shows the full ingredient list and method for a single selected recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbarRecipeDetail);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        PantryRepository repository = new PantryRepository(this);
        Recipe recipe = repository.getRecipeById(recipeId);

        TextView textIngredients = findViewById(R.id.textIngredientsList);
        TextView textSteps = findViewById(R.id.textSteps);

        if (recipe == null) {
            setTitle(R.string.title_recipe_detail);
            textIngredients.setText(R.string.recipe_not_found);
            return;
        }

        setTitle(recipe.getName());

        StringBuilder ingredientsText = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredientsText.append("\u2022 ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(" ")
                    .append(ingredient.getName())
                    .append("\n");
        }
        textIngredients.setText(ingredientsText.toString().trim());
        textSteps.setText(recipe.getSteps());
    }

    private String formatQuantity(double q) {
        if (q == Math.floor(q)) return String.valueOf((long) q);
        return String.valueOf(q);
    }
}
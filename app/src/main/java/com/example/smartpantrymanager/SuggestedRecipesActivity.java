package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.db.PantryRepository;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeMatchResult;
import com.example.smartpantrymanager.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the strict-matching rule (Section 2.3, implemented in IngredientMatcher)
 * against the current pantry and shows only the recipes the user can make
 * right now, plus an optional "Almost There" list (Section 8 stretch goal)
 * of recipes missing exactly one ingredient.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private PantryRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle(R.string.title_suggested_recipes);

        repository = new PantryRepository(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_recipes);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_recipes) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions(); // re-run matching every time the pantry may have changed
    }

    private void refreshSuggestions() {
        List<PantryItem> pantry = repository.getAllPantryItems();
        List<Recipe> recipes = repository.getAllRecipesWithIngredients();
        List<RecipeMatchResult> allResults = IngredientMatcher.matchRecipes(recipes, pantry);

        List<RecipeMatchResult> suggested = new ArrayList<>();
        List<RecipeMatchResult> almostThere = new ArrayList<>();
        for (RecipeMatchResult result : allResults) {
            if (result.getMissingIngredients().isEmpty()) {
                suggested.add(result);
            } else if (result.getMissingIngredients().size() == 1) {
                almostThere.add(result);
            }
        }

        RecyclerView recyclerSuggested = findViewById(R.id.recyclerSuggested);
        recyclerSuggested.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggested.setAdapter(new RecipeAdapter(suggested, false, this::openRecipeDetail));

        View emptyView = findViewById(R.id.textEmptySuggestions);
        emptyView.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);

        RecyclerView recyclerAlmost = findViewById(R.id.recyclerAlmostThere);
        recyclerAlmost.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlmost.setAdapter(new RecipeAdapter(almostThere, true, this::openRecipeDetail));

        View almostHeader = findViewById(R.id.textAlmostThereHeader);
        int visibility = almostThere.isEmpty() ? View.GONE : View.VISIBLE;
        almostHeader.setVisibility(visibility);
        recyclerAlmost.setVisibility(visibility);
    }

    private void openRecipeDetail(long recipeId) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        startActivity(intent);
    }
}
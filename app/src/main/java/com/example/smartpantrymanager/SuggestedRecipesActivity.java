package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View txtNoMatch;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        recyclerView = findViewById(R.id.recyclerSuggested);
        txtNoMatch = findViewById(R.id.txtNoMatch);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        db = AppDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomNav.setSelectedItemId(R.id.nav_suggested);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_suggested) {
                return true;
            }
            return false;
        });

        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        List<PantryItem> pantry = db.pantryDao().getAll();
        List<Recipe> allRecipes = db.recipeDao().getAll();
        List<Recipe> suggested = getStrictMatchingRecipes(allRecipes, pantry);

        if (suggested.isEmpty()) {
            txtNoMatch.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtNoMatch.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            RecipeAdapter adapter = new RecipeAdapter(this, suggested, recipe -> {
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipe.id);
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    // CORE LOGIC - EXPLAIN THIS IN VIDEO WITH CODE OPEN
    private List<Recipe> getStrictMatchingRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            boolean canMake = true;
            for (String rawIng : recipe.getIngredients()) {
                String required = normalize(rawIng);
                boolean found = false;
                for (PantryItem p : pantry) {
                    String pantryName = normalize(p.getName());
                    if (pantryName.equals(required) && p.getQuantity() >= 1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    canMake = false;
                    break;
                }
            }
            if (canMake) result.add(recipe);
        }
        return result;
    }

    // Robust matching - handles tomato/tomatoes, case, spaces
    private String normalize(String s) {
        if (s == null) return "";
        s = s.toLowerCase().trim();
        if (s.endsWith("s") && s.length() > 3) s = s.substring(0, s.length() - 1);
        return s;
    }
}
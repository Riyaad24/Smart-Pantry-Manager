package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.net.TheMealDbClient;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View txtNoMatch;
    private AppDatabase db;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        recyclerView = findViewById(R.id.recyclerSuggested);
        txtNoMatch = findViewById(R.id.txtNoMatch);
        edtSearch = findViewById(R.id.edtRecipeSearch);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        db = AppDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomNav.setSelectedItemId(R.id.nav_suggested);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_suggested) {
                return true;
            }
            return false;
        });

        // Initialize high fidelity search trigger
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = edtSearch.getText().toString();
                if (!query.trim().isEmpty()) {
                    performLiveApiSearch(query);
                }
                return true;
            }
            return false;
        });

        loadSuggestedRecipes();
    }

    private void performLiveApiSearch(String query) {
        TheMealDbClient.fetchFeaturedRecommendations(query, (meals, error) -> {
            if (meals != null && !meals.isEmpty()) {
                updateRecipeList(meals);
            }
        });
    }

    private void loadSuggestedRecipes() {
        List<PantryItem> pantry = db.pantryDao().getAll();
        List<Recipe> allRecipes = db.recipeDao().getAll();
        
        // Build query keywords from primary inventory parameters
        String initialQuery = pantry.isEmpty() ? "Pasta" : pantry.get(0).getName();
        
        TheMealDbClient.fetchFeaturedRecommendations(initialQuery, (meals, error) -> {
            List<Recipe> combined = new ArrayList<>(getStrictMatchingRecipes(allRecipes, pantry));
            if (meals != null) {
                combined.addAll(meals);
            }
            updateRecipeList(combined);
        });
    }

    private void updateRecipeList(List<Recipe> list) {
        runOnUiThread(() -> {
            if (list == null || list.isEmpty()) {
                txtNoMatch.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                txtNoMatch.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                RecipeAdapter adapter = new RecipeAdapter(this, list, recipe -> {
                    Intent intent = new Intent(this, RecipeDetailActivity.class);
                    if (recipe.mealIdApi != null && !recipe.mealIdApi.isEmpty()) {
                        intent.putExtra("meal_id_api", recipe.mealIdApi);
                    } else {
                        intent.putExtra("recipe_id", recipe.id);
                    }
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private List<Recipe> getStrictMatchingRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (calculateMatchPercentage(recipe, pantry) >= 100) {
                result.add(recipe);
            }
        }
        return result;
    }

    // High fidelity parameter matching logic
    private int calculateMatchPercentage(Recipe recipe, List<PantryItem> pantry) {
        List<String> required = recipe.getIngredients();
        if (required.isEmpty()) return 0;
        
        int matchCount = 0;
        for (String r : required) {
            String normReq = normalize(r);
            for (PantryItem p : pantry) {
                if (normReq.contains(normalize(p.getName())) || normalize(p.getName()).contains(normReq)) {
                    matchCount++;
                    break;
                }
            }
        }
        return (int) ((matchCount / (float) required.size()) * 100);
    }

    private String normalize(String s) {
        if (s == null) return "";
        s = s.toLowerCase().trim();
        if (s.endsWith("s") && s.length() > 3) s = s.substring(0, s.length() - 1);
        return s;
    }
}
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
import com.example.smartpantrymanager.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View txtNoMatch;
    private AppDatabase db;
    private SessionManager sessionManager;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_suggested);

        recyclerView = findViewById(R.id.recyclerSuggested);
        txtNoMatch = findViewById(R.id.txtNoMatch);
        edtSearch = findViewById(R.id.edtRecipeSearch);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        db = AppDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomNav.setSelectedItemId(R.id.nav_suggested);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, PantryListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_suggested) {
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
        int userId = sessionManager.getUserId();
        List<PantryItem> pantry = db.pantryDao().getAllForUser(userId);
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

    private List<Recipe> getStrictMatchingRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> matched = new ArrayList<>();
        List<String> pantryNames = new ArrayList<>();
        for (PantryItem item : pantry) {
            pantryNames.add(item.getName().toLowerCase().trim());
        }

        for (Recipe recipe : allRecipes) {
            if (recipe.ingredientsCsv == null) continue;
            String[] reqs = recipe.ingredientsCsv.toLowerCase().split(",");
            boolean hasAll = true;
            for (String req : reqs) {
                String cleanReq = req.trim();
                boolean found = false;
                for (String pName : pantryNames) {
                    if (pName.contains(cleanReq) || cleanReq.contains(pName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    hasAll = false;
                    break;
                }
            }
            if (hasAll && !reqs[0].isEmpty()) {
                matched.add(recipe);
            }
        }
        return matched;
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
                    Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("recipe_name", recipe.name);
                    intent.putExtra("recipe_steps", recipe.steps);
                    intent.putExtra("recipe_ingredients", recipe.ingredientsCsv);
                    intent.putExtra("meal_id_api", recipe.mealIdApi);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        loadSuggestedRecipes();
    }
}

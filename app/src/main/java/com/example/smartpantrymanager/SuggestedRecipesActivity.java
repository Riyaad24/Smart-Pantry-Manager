package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.net.TheMealDbClient;
import com.example.smartpantrymanager.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerReadyToCook, recyclerSuggested;
    private View txtNoMatch;
    private LinearLayout containerPantryChips;
    private MaterialCardView cardCategoryQuick, cardCategoryDinner, cardCategoryDessert;
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

        recyclerReadyToCook = findViewById(R.id.recyclerReadyToCook);
        recyclerSuggested = findViewById(R.id.recyclerSuggested);
        txtNoMatch = findViewById(R.id.txtNoMatch);
        containerPantryChips = findViewById(R.id.containerPantryChips);
        edtSearch = findViewById(R.id.edtRecipeSearch);
        
        cardCategoryQuick = findViewById(R.id.cardCategoryQuick);
        cardCategoryDinner = findViewById(R.id.cardCategoryDinner);
        cardCategoryDessert = findViewById(R.id.cardCategoryDessert);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        db = AppDatabase.getInstance(this);

        recyclerReadyToCook.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggested.setLayoutManager(new LinearLayoutManager(this));

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

        // Search action
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

        // Category clicks
        cardCategoryQuick.setOnClickListener(v -> performLiveApiSearch("quick"));
        cardCategoryDinner.setOnClickListener(v -> performLiveApiSearch("chicken"));
        cardCategoryDessert.setOnClickListener(v -> performLiveApiSearch("dessert"));

        loadDiscoverData();
    }

    private void loadDiscoverData() {
        int userId = sessionManager.getUserId();
        List<PantryItem> pantry = db.pantryDao().getAllForUser(userId);
        List<Recipe> allRecipes = db.recipeDao().getAll();

        // 1. Populate Ready to Cook Now (Strict Matching)
        List<Recipe> readyToCook = getStrictMatchingRecipes(allRecipes, pantry);
        if (readyToCook.isEmpty()) {
            recyclerReadyToCook.setVisibility(View.GONE);
        } else {
            recyclerReadyToCook.setVisibility(View.VISIBLE);
            RecipeAdapter readyAdapter = new RecipeAdapter(this, readyToCook, this::openRecipeDetail);
            recyclerReadyToCook.setAdapter(readyAdapter);
        }

        // 2. Populate Based On Your Pantry Chips
        populatePantryChips(pantry);

        // 3. Populate Online Suggestions
        String initialQuery = pantry.isEmpty() ? "Pasta" : pantry.get(0).getName();
        TheMealDbClient.fetchFeaturedRecommendations(initialQuery, (meals, error) -> {
            if (meals != null && !meals.isEmpty()) {
                runOnUiThread(() -> {
                    RecipeAdapter onlineAdapter = new RecipeAdapter(this, meals, this::openRecipeDetail);
                    recyclerSuggested.setAdapter(onlineAdapter);
                });
            }
        });
    }

    private void populatePantryChips(List<PantryItem> pantry) {
        containerPantryChips.removeAllViews();
        if (pantry.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Add items to your pantry to unlock ingredient filters");
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_medium_contrast));
            tv.setTextSize(13);
            containerPantryChips.addView(tv);
            return;
        }

        for (PantryItem item : pantry) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            card.setLayoutParams(params);
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface_soft_charcoal));
            card.setRadius(20);
            card.setStrokeWidth(1);
            card.setStrokeColor(ContextCompat.getColor(this, R.color.stroke_subtle));
            card.setClickable(true);
            card.setFocusable(true);

            TextView tv = new TextView(this);
            tv.setPadding(20, 12, 20, 12);
            tv.setText("🍗 " + item.getName());
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_high_contrast));
            tv.setTextSize(13);
            card.addView(tv);

            card.setOnClickListener(v -> performLiveApiSearch(item.getName()));
            containerPantryChips.addView(card);
        }
    }

    private void performLiveApiSearch(String query) {
        TheMealDbClient.fetchFeaturedRecommendations(query, (meals, error) -> {
            if (meals != null && !meals.isEmpty()) {
                runOnUiThread(() -> {
                    RecipeAdapter adapter = new RecipeAdapter(this, meals, this::openRecipeDetail);
                    recyclerSuggested.setAdapter(adapter);
                    txtNoMatch.setVisibility(View.GONE);
                });
            } else {
                runOnUiThread(() -> txtNoMatch.setVisibility(View.VISIBLE));
            }
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

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
        intent.putExtra("recipe_name", recipe.name);
        intent.putExtra("recipe_steps", recipe.steps);
        intent.putExtra("recipe_ingredients", recipe.ingredientsCsv);
        intent.putExtra("meal_id_api", recipe.mealIdApi);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        loadDiscoverData();
    }
}

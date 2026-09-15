package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.net.NetworkImageLoader;
import com.example.smartpantrymanager.net.TheMealDbClient;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView imgHero;
    private TextView txtName, txtIngredients, txtSteps, txtLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        imgHero = findViewById(R.id.imgRecipeHero);
        txtName = findViewById(R.id.txtRecipeName);
        txtIngredients = findViewById(R.id.txtIngredients);
        txtSteps = findViewById(R.id.txtSteps);
        txtLoading = findViewById(R.id.txtLoadingDetails);

        // Map primary navigation controls
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
            getSupportActionBar().setTitle(""); 
        }

        // Dual source parameter extraction: Local DB ID (int) vs Web API ID (String)
        String apiId = getIntent().getStringExtra("meal_id_api");
        int localId = getIntent().getIntExtra("recipe_id", -1);

        if (apiId != null && !apiId.isEmpty()) {
            loadRecipeFromApi(apiId);
        } else if (localId != -1) {
            loadRecipeFromLocal(localId);
        }
    }

    private void loadRecipeFromLocal(int id) {
        AppDatabase db = AppDatabase.getInstance(this);
        Recipe recipe = db.recipeDao().getById(id);
        if (recipe != null) {
            bindRecipeData(recipe);
        }
    }

    private void loadRecipeFromApi(String mealId) {
        txtLoading.setVisibility(View.VISIBLE);
        TheMealDbClient.fetchRecipeDetails(mealId, (recipe, error) -> {
            txtLoading.setVisibility(View.GONE);
            if (recipe != null) {
                bindRecipeData(recipe);
            } else {
                txtName.setText("Error Synchronizing");
                txtSteps.setText(error != null ? error : "Unavailable");
            }
        });
    }

    private void bindRecipeData(Recipe recipe) {
        txtName.setText(recipe.name);
        
        // Clean ingredients formatting for high density rendering
        String ings = recipe.ingredientsCsv;
        if (ings != null) {
            // Replace comma separators with bullet points for premium list look
            String formatted = "• " + ings.replace(", ", "\n• ").replace(",", "\n• ");
            txtIngredients.setText(formatted);
        }

        txtSteps.setText(recipe.steps);

        // Dispatch asynchronous image stream decoding
        if (recipe.imageUrl != null && !recipe.imageUrl.isEmpty()) {
            NetworkImageLoader.displayImage(recipe.imageUrl, imgHero);
        }
    }
}
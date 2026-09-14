package com.example.smartpantrymanager.net;

import android.os.Handler;
import android.os.Looper;
import com.example.smartpantrymanager.model.Recipe;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * High-fidelity, isolated REST networking client for TheMealDB API.
 * Uses native standard streams parsing to handle live remote suggestions asynchronously.
 */
public class TheMealDbClient {

    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface MealSuggestionsCallback {
        void onResponse(List<Recipe> meals, String networkError);
    }

    /**
     * Fetch featured meal recommendations via matching search phrases asynchronously.
     */
    public static void fetchFeaturedRecommendations(final String keyword, final MealSuggestionsCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                String encodedKeyword = (keyword == null || keyword.trim().isEmpty()) ? "pasta" : keyword.trim().replace(" ", "%20");
                URL url = new URL(BASE_URL + "search.php?s=" + encodedKeyword);
                
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    List<Recipe> meals = new ArrayList<>();
                    
                    if (!jsonResponse.isNull("meals")) {
                        JSONArray mealsArray = jsonResponse.getJSONArray("meals");
                        // Limit to top 5 prominent suggestions for clean layout density
                        int limit = Math.min(mealsArray.length(), 5);
                        for (int i = 0; i < limit; i++) {
                            JSONObject obj = mealsArray.getJSONObject(i);
                            String name = obj.optString("strMeal", "Unnamed Meal");
                            String instructions = obj.optString("strInstructions", "");
                            String thumbUrl = obj.optString("strMealThumb", "");
                            
                            // Map custom temporary recipe reference holding thumb URL in instruction csv spacer bounds
                            Recipe r = new Recipe(name, "TheMealDB", instructions);
                            // Store the thumb URL cleanly inside custom model fields if needed or pass as metadata
                            r.ingredientsCsv = thumbUrl; 
                            meals.add(r);
                        }
                    }
                    
                    final List<Recipe> finalMeals = meals;
                    mainHandler.post(() -> callback.onResponse(finalMeals, null));
                } else {
                    mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "Server returned HTTP status " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "Network unavailable. Check link parameters."));
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception ignored) {}
            }
        });
    }

    /**
     * Query remote recipes filtered dynamically by available ingredient parameters tokens.
     */
    public static void fetchRecipesByIngredient(final String ingredient, final MealSuggestionsCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                if (ingredient == null || ingredient.trim().isEmpty()) {
                    mainHandler.post(() -> callback.onResponse(new ArrayList<>(), null));
                    return;
                }
                String param = ingredient.trim().toLowerCase().replace(" ", "%20");
                URL url = new URL(BASE_URL + "filter.php?i=" + param);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    List<Recipe> meals = new ArrayList<>();

                    if (!jsonResponse.isNull("meals")) {
                        JSONArray mealsArray = jsonResponse.getJSONArray("meals");
                        int limit = Math.min(mealsArray.length(), 6);
                        for (int i = 0; i < limit; i++) {
                            JSONObject obj = mealsArray.getJSONObject(i);
                            String name = obj.optString("strMeal", "Matched Meal");
                            String thumbUrl = obj.optString("strMealThumb", "");
                            
                            Recipe r = new Recipe(name, "Live Match", "See details in Discover tab overview.");
                            r.ingredientsCsv = thumbUrl;
                            meals.add(r);
                        }
                    }

                    final List<Recipe> finalMeals = meals;
                    mainHandler.post(() -> callback.onResponse(finalMeals, null));
                } else {
                    mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "HTTP error code " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "API link parameters error."));
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception ignored) {}
            }
        });
    }
}
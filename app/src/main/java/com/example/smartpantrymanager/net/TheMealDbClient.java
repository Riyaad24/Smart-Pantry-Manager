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

    public interface MealDetailsCallback {
        void onResponse(Recipe recipe, String error);
    }

    public interface CategoriesCallback {
        void onResponse(List<String> categories, String error);
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
                        int limit = Math.min(mealsArray.length(), 5);
                        for (int i = 0; i < limit; i++) {
                            JSONObject obj = mealsArray.getJSONObject(i);
                            Recipe r = parseRecipeJson(obj);
                            meals.add(r);
                        }
                    }
                    
                    final List<Recipe> finalMeals = meals;
                    mainHandler.post(() -> callback.onResponse(finalMeals, null));
                } else {
                    mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "Server Error: " + responseCode));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "Network unavailable."));
            } finally {
                cleanup(connection, reader);
            }
        });
    }

    /**
     * Fetch full recipe details including instructions and ingredients by Meal ID.
     */
    public static void fetchRecipeDetails(final String mealId, final MealDetailsCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(BASE_URL + "lookup.php?i=" + mealId);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);

                    JSONObject json = new JSONObject(response.toString());
                    if (!json.isNull("meals")) {
                        JSONObject obj = json.getJSONArray("meals").getJSONObject(0);
                        Recipe r = parseRecipeJson(obj);
                        mainHandler.post(() -> callback.onResponse(r, null));
                    } else {
                        mainHandler.post(() -> callback.onResponse(null, "Recipe not found."));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResponse(null, "Error fetching details."));
            } finally {
                cleanup(connection, reader);
            }
        });
    }

    /**
     * Query remote recipes filtered dynamically by available ingredient.
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
                URL url = new URL(BASE_URL + "filter.php?i=" + ingredient.trim().toLowerCase().replace(" ", "%20"));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);

                    JSONObject json = new JSONObject(response.toString());
                    List<Recipe> meals = new ArrayList<>();
                    if (!json.isNull("meals")) {
                        JSONArray array = json.getJSONArray("meals");
                        int limit = Math.min(array.length(), 10);
                        for (int i = 0; i < limit; i++) {
                            JSONObject obj = array.getJSONObject(i);
                            Recipe r = parseRecipeJson(obj);
                            meals.add(r);
                        }
                    }
                    final List<Recipe> finalMeals = meals;
                    mainHandler.post(() -> callback.onResponse(finalMeals, null));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "API error."));
            } finally {
                cleanup(connection, reader);
            }
        });
    }

    public static void fetchCategories(final CategoriesCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(BASE_URL + "categories.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);

                    JSONObject json = new JSONObject(sb.toString());
                    List<String> categories = new ArrayList<>();
                    if (!json.isNull("categories")) {
                        JSONArray array = json.getJSONArray("categories");
                        for (int i = 0; i < array.length(); i++) {
                            categories.add(array.getJSONObject(i).getString("strCategory"));
                        }
                    }
                    mainHandler.post(() -> callback.onResponse(categories, null));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResponse(new ArrayList<>(), "Error"));
            } finally {
                cleanup(connection, reader);
            }
        });
    }

    private static Recipe parseRecipeJson(JSONObject obj) throws Exception {
        String id = obj.optString("idMeal", "");
        String name = obj.optString("strMeal", "Unnamed Meal");
        String thumb = obj.optString("strMealThumb", "");
        String instructions = obj.optString("strInstructions", "");
        
        // Build ingredients list from API fields (strIngredient1..20)
        StringBuilder ingBuilder = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            String ing = obj.optString("strIngredient" + i, "");
            String measure = obj.optString("strMeasure" + i, "");
            if (!ing.trim().isEmpty()) {
                if (ingBuilder.length() > 0) ingBuilder.append(", ");
                ingBuilder.append(measure).append(" ").append(ing);
            }
        }

        Recipe r = new Recipe(name, ingBuilder.toString(), instructions);
        r.mealIdApi = id;
        r.imageUrl = thumb;
        return r;
    }

    private static void cleanup(HttpURLConnection conn, BufferedReader r) {
        try {
            if (r != null) r.close();
            if (conn != null) conn.disconnect();
        } catch (Exception ignored) {}
    }
}
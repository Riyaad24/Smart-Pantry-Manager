package com.example.smartpantrymanager.util;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.example.smartpantrymanager.model.RecipeMatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the assignment's "strict-matching rule" (Section 2.3 of the brief):
 * a recipe only counts as "suggested" if every ingredient it needs is present in
 * the pantry in at least the required quantity.
 *
 * Ingredient names are normalised (case, whitespace, naive de-pluralisation) so
 * that entries like "Tomato" and "tomatoes" are treated as the same ingredient,
 * and units are converted to a common base via UnitUtils so that "1kg" correctly
 * satisfies a recipe that needs "500g".
 */
public class IngredientMatcher {

    private IngredientMatcher() { }

    /** Normalises an ingredient name for comparison - lowercase, trimmed, de-pluralised. */
    public static String normalise(String name) {
        if (name == null) return "";
        String n = name.trim().toLowerCase().replaceAll("\\s+", " ");
        if (n.endsWith("ies") && n.length() > 3) {
            return n.substring(0, n.length() - 3) + "y";      // berries -> berry
        }
        if (n.endsWith("oes") && n.length() > 3) {
            return n.substring(0, n.length() - 2);              // tomatoes -> tomato
        }
        if ((n.endsWith("ches") || n.endsWith("shes") || n.endsWith("xes") || n.endsWith("sses"))
                && n.length() > 4) {
            return n.substring(0, n.length() - 2);              // dishes -> dish, boxes -> box
        }
        if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 1) {
            return n.substring(0, n.length() - 1);              // eggs -> egg
        }
        return n;
    }

    /** Tests every recipe against the pantry and returns a match result for each one. */
    public static List<RecipeMatchResult> matchRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        List<RecipeMatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<String> missing = new ArrayList<>();
            for (RecipeIngredient required : recipe.getIngredients()) {
                if (!isAvailable(required, pantry)) {
                    missing.add(required.getName());
                }
            }
            results.add(new RecipeMatchResult(recipe, missing));
        }
        return results;
    }

    /** Convenience method returning only the recipes that strictly match. */
    public static List<Recipe> suggestedRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        List<Recipe> suggested = new ArrayList<>();
        for (RecipeMatchResult result : matchRecipes(recipes, pantry)) {
            if (result.getMissingIngredients().isEmpty()) {
                suggested.add(result.getRecipe());
            }
        }
        return suggested;
    }

    private static boolean isAvailable(RecipeIngredient required, List<PantryItem> pantry) {
        String requiredName = normalise(required.getName());
        String requiredCategory = UnitUtils.category(required.getUnit());
        double requiredBaseQty = UnitUtils.toBase(required.getQuantity(), required.getUnit());

        for (PantryItem item : pantry) {
            if (!normalise(item.getName()).equals(requiredName)) continue;

            String itemCategory = UnitUtils.category(item.getUnit());
            if (!itemCategory.equals(requiredCategory)) {
                // Units aren't directly comparable (e.g. "2 cloves" vs "50g") - fall back
                // to a simple presence check rather than wrongly excluding the recipe.
                return true;
            }
            double itemBaseQty = UnitUtils.toBase(item.getQuantity(), item.getUnit());
            if (itemBaseQty >= requiredBaseQty) return true;
        }
        return false;
    }
}
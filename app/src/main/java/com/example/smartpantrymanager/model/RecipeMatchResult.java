package com.example.smartpantrymanager.model;

import java.util.List;

/**
 * The outcome of testing one recipe against the pantry: the recipe itself,
 * plus the names of any required ingredients that are missing. An empty
 * missingIngredients list means the recipe strictly matches (Section 2.3
 * of the brief) and should appear in Suggested Recipes.
 */
public class RecipeMatchResult {

    private final Recipe recipe;
    private final List<String> missingIngredients;

    public RecipeMatchResult(Recipe recipe, List<String> missingIngredients) {
        this.recipe = recipe;
        this.missingIngredients = missingIngredients;
    }

    public Recipe getRecipe() { return recipe; }
    public List<String> getMissingIngredients() { return missingIngredients; }
}
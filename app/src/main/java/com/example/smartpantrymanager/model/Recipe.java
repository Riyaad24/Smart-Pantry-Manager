package com.example.smartpantrymanager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Arrays;
import java.util.List;

/** A recipe made up of a name, method steps, and its list of required ingredients. */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String ingredientsCsv; // store as "egg, bread, milk"
    public String steps;
    
    // Extended fields for API support and high-fidelity rendering
    public String mealIdApi;
    public String imageUrl;

    public Recipe(String name, String ingredientsCsv, String steps) {
        this.name = name;
        this.ingredientsCsv = ingredientsCsv;
        this.steps = steps;
    }

    public List<String> getIngredients() {
        if (ingredientsCsv == null || ingredientsCsv.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(ingredientsCsv.split(","));
    }
}
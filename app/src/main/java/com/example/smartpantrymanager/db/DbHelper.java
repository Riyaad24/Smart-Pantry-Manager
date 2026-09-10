package com.example.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates and manages the SQLite database for Smart Pantry Manager.
 * Three tables: pantry_items (user's own data), and recipes / recipe_ingredients
 * (pre-seeded with 20 starter recipes on first run - see seedRecipes()).
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_P_ID = "_id";
    public static final String COL_P_NAME = "name";
    public static final String COL_P_QTY = "quantity";
    public static final String COL_P_UNIT = "unit";
    public static final String COL_P_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_R_ID = "_id";
    public static final String COL_R_NAME = "name";
    public static final String COL_R_STEPS = "steps";

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_NAME + " TEXT NOT NULL, " +
                COL_P_QTY + " REAL NOT NULL, " +
                COL_P_UNIT + " TEXT NOT NULL, " +
                COL_P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_NAME + " TEXT NOT NULL, " +
                COL_R_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_R_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    private long insertRecipe(SQLiteDatabase db, String name, String steps) {
        ContentValues cv = new ContentValues();
        cv.put(COL_R_NAME, name);
        cv.put(COL_R_STEPS, steps);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    private void ing(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RI_RECIPE_ID, recipeId);
        cv.put(COL_RI_NAME, name);
        cv.put(COL_RI_QTY, qty);
        cv.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
    }

    /** Pre-loads 20 simple starter recipes so the app is immediately testable. */
    private void seedRecipes(SQLiteDatabase db) {
        long id;

        id = insertRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk the eggs with the milk.\n2. Melt the butter in a pan over medium heat.\n" +
                "3. Pour in the eggs and stir gently until softly set.\n4. Toast the bread and spoon the eggs on top.");
        ing(db, id, "eggs", 2, "unit");
        ing(db, id, "bread", 2, "unit");
        ing(db, id, "butter", 10, "g");
        ing(db, id, "milk", 30, "ml");

        id = insertRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until al dente.\n2. Fry chopped garlic in olive oil.\n" +
                "3. Add chopped tomatoes and simmer into a sauce.\n4. Toss the drained pasta through the sauce.");
        ing(db, id, "pasta", 200, "g");
        ing(db, id, "tomato", 3, "unit");
        ing(db, id, "garlic", 2, "unit");
        ing(db, id, "olive oil", 30, "ml");

        id = insertRecipe(db, "Vegetable Fried Rice",
                "1. Scramble the eggs and set aside.\n2. Fry diced onion and carrot until soft.\n" +
                "3. Add the cooked rice and soy sauce, stir-fry for 3-4 minutes.\n4. Mix in the eggs and serve.");
        ing(db, id, "rice", 300, "g");
        ing(db, id, "egg", 2, "unit");
        ing(db, id, "carrot", 1, "unit");
        ing(db, id, "onion", 1, "unit");
        ing(db, id, "soy sauce", 30, "ml");

        id = insertRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n2. Place cheese between the unbuttered sides.\n" +
                "3. Grill in a pan until golden on both sides and the cheese has melted.");
        ing(db, id, "bread", 2, "unit");
        ing(db, id, "cheese", 50, "g");
        ing(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Banana Pancakes",
                "1. Mash the bananas and whisk with the egg and milk.\n2. Stir in the flour to a smooth batter.\n" +
                "3. Cook spoonfuls in a hot pan for 2 minutes per side.");
        ing(db, id, "banana", 2, "unit");
        ing(db, id, "flour", 150, "g");
        ing(db, id, "egg", 1, "unit");
        ing(db, id, "milk", 100, "ml");

        id = insertRecipe(db, "Chicken Stir Fry",
                "1. Slice the chicken and fry until browned.\n2. Add chopped onion, garlic and carrot.\n" +
                "3. Stir in soy sauce and cook until the chicken is done through.");
        ing(db, id, "chicken", 300, "g");
        ing(db, id, "onion", 1, "unit");
        ing(db, id, "garlic", 2, "unit");
        ing(db, id, "soy sauce", 30, "ml");
        ing(db, id, "carrot", 1, "unit");

        id = insertRecipe(db, "Vegetable Soup",
                "1. Chop the potato, carrot and onion.\n2. Simmer in the stock for 20 minutes until soft.\n" +
                "3. Blend or serve chunky, as preferred.");
        ing(db, id, "potato", 2, "unit");
        ing(db, id, "carrot", 2, "unit");
        ing(db, id, "onion", 1, "unit");
        ing(db, id, "vegetable stock", 500, "ml");

        id = insertRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs with a pinch of salt.\n2. Melt butter in a pan and pour in the eggs.\n" +
                "3. Sprinkle cheese over one half, fold and cook until set.");
        ing(db, id, "egg", 3, "unit");
        ing(db, id, "cheese", 30, "g");
        ing(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Garlic Bread",
                "1. Mix crushed garlic into softened butter.\n2. Spread over sliced bread.\n" +
                "3. Grill or bake until golden and crisp.");
        ing(db, id, "bread", 1, "unit");
        ing(db, id, "garlic", 3, "unit");
        ing(db, id, "butter", 40, "g");

        id = insertRecipe(db, "Tuna Sandwich",
                "1. Mix the tuna with mayonnaise.\n2. Spread onto a slice of bread.\n3. Top with the second slice and serve.");
        ing(db, id, "bread", 2, "unit");
        ing(db, id, "tuna", 100, "g");
        ing(db, id, "mayonnaise", 20, "ml");

        id = insertRecipe(db, "Potato Salad",
                "1. Boil the potatoes until tender, then cool and dice.\n" +
                "2. Mix with chopped onion and mayonnaise.\n3. Season and chill before serving.");
        ing(db, id, "potato", 4, "unit");
        ing(db, id, "mayonnaise", 50, "ml");
        ing(db, id, "onion", 1, "unit");

        id = insertRecipe(db, "Fruit Salad",
                "1. Chop the banana, apple and orange into bite-sized pieces.\n2. Mix together in a bowl and serve chilled.");
        ing(db, id, "banana", 1, "unit");
        ing(db, id, "apple", 1, "unit");
        ing(db, id, "orange", 1, "unit");

        id = insertRecipe(db, "Peanut Butter Toast",
                "1. Toast the bread.\n2. Spread peanut butter over each slice while warm.");
        ing(db, id, "bread", 2, "unit");
        ing(db, id, "peanut butter", 30, "g");

        id = insertRecipe(db, "Chicken Soup",
                "1. Simmer the chicken in the stock until cooked through.\n" +
                "2. Add chopped carrot and onion and cook until tender.\n3. Shred the chicken and return to the pot.");
        ing(db, id, "chicken", 200, "g");
        ing(db, id, "carrot", 1, "unit");
        ing(db, id, "onion", 1, "unit");
        ing(db, id, "vegetable stock", 500, "ml");

        id = insertRecipe(db, "Rice and Beans",
                "1. Cook the rice according to packet instructions.\n" +
                "2. Fry chopped onion, then stir in the beans and heat through.\n3. Serve the beans over the rice.");
        ing(db, id, "rice", 200, "g");
        ing(db, id, "beans", 200, "g");
        ing(db, id, "onion", 1, "unit");

        id = insertRecipe(db, "Egg Fried Noodles",
                "1. Cook the noodles and drain.\n2. Scramble the egg in a hot pan and push to one side.\n" +
                "3. Add the noodles, carrot and soy sauce, and toss together.");
        ing(db, id, "noodles", 200, "g");
        ing(db, id, "egg", 2, "unit");
        ing(db, id, "soy sauce", 20, "ml");
        ing(db, id, "carrot", 1, "unit");

        id = insertRecipe(db, "Cheese and Tomato Toast",
                "1. Toast the bread lightly.\n2. Top with sliced tomato and grated cheese.\n3. Grill until the cheese melts.");
        ing(db, id, "bread", 2, "unit");
        ing(db, id, "cheese", 40, "g");
        ing(db, id, "tomato", 1, "unit");

        id = insertRecipe(db, "Apple Porridge",
                "1. Simmer the oats in milk until thick and creamy.\n2. Stir in diced apple and cook for 2 more minutes.");
        ing(db, id, "oats", 100, "g");
        ing(db, id, "milk", 250, "ml");
        ing(db, id, "apple", 1, "unit");

        id = insertRecipe(db, "Simple Guacamole",
                "1. Mash the avocado in a bowl.\n2. Stir in finely chopped onion and tomato.\n3. Season to taste and serve.");
        ing(db, id, "avocado", 2, "unit");
        ing(db, id, "onion", 1, "unit");
        ing(db, id, "tomato", 1, "unit");

        id = insertRecipe(db, "Beef Stir Fry",
                "1. Slice the beef thinly and fry until browned.\n2. Add chopped onion and garlic and cook until soft.\n" +
                "3. Stir in soy sauce and cook for a further 2 minutes.");
        ing(db, id, "beef", 250, "g");
        ing(db, id, "onion", 1, "unit");
        ing(db, id, "garlic", 2, "unit");
        ing(db, id, "soy sauce", 30, "ml");
    }
}
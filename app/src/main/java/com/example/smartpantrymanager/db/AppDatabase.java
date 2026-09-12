package com.example.smartpantrymanager.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {PantryItem.class, Recipe.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();
    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "pantry_db")
                    .allowMainThreadQueries() // for simplicity, use AsyncTask in real app
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                List<Recipe> seed = Arrays.asList(
                                        new Recipe("Toast", "bread, butter", "1. Toast bread. 2. Spread butter."),
                                        new Recipe("Scrambled Eggs", "egg, butter, salt", "1. Beat eggs. 2. Fry with butter."),
                                        new Recipe("Omelette", "egg, cheese, salt, oil", "1. Beat eggs. 2. Add cheese. 3. Fry."),
                                        new Recipe("Boiled Egg", "egg, water, salt", "1. Boil water. 2. Add egg for 10 mins."),
                                        new Recipe("Milk Tea", "milk, tea bag, sugar, water", "1. Boil water. 2. Add tea bag and milk."),
                                        new Recipe("Cheese Sandwich", "bread, cheese, butter", "1. Butter bread. 2. Add cheese. 3. Toast."),
                                        new Recipe("Tomato Salad", "tomato, onion, salt, oil", "1. Chop tomato and onion. 2. Mix with salt and oil."),
                                        new Recipe("Garlic Bread", "bread, garlic, butter", "1. Mix garlic and butter. 2. Spread on bread. 3. Toast."),
                                        new Recipe("Fried Rice", "rice, egg, oil, salt, onion", "1. Cook rice. 2. Fry with egg and onion."),
                                        new Recipe("Pancakes", "flour, egg, milk, sugar, oil", "1. Mix all. 2. Fry small portions."),
                                        new Recipe("Tomato Soup", "tomato, onion, water, salt, oil", "1. Boil tomato and onion. 2. Blend."),
                                        new Recipe("Rice and Beans", "rice, beans, water, salt, oil", "1. Boil beans. 2. Cook rice. 3. Mix."),
                                        new Recipe("Butter Rice", "rice, butter, salt, water", "1. Cook rice with butter and salt."),
                                        new Recipe("Egg Fried Bread", "bread, egg, oil, salt", "1. Dip bread in egg. 2. Fry."),
                                        new Recipe("Onion Rings", "onion, flour, oil, salt, water", "1. Make batter. 2. Dip onion. 3. Fry."),
                                        new Recipe("Milk Rice", "rice, milk, sugar, water", "1. Cook rice in milk and sugar."),
                                        new Recipe("Cheese Omelette", "egg, cheese, butter, salt", "1. Make omelette with cheese."),
                                        new Recipe("Garlic Rice", "rice, garlic, oil, salt", "1. Fry garlic. 2. Add cooked rice."),
                                        new Recipe("Sugar Toast", "bread, sugar, butter", "1. Toast bread with butter and sugar."),
                                        new Recipe("Tomato Omelette", "egg, tomato, onion, oil, salt", "1. Chop veg. 2. Mix with egg. 3. Fry.")
                                );
                                getInstance(context).recipeDao().insertAll(seed);
                            });
                        }
                    }).build();
        }
        return INSTANCE;
    }
}
package com.example.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Single access point for all database reads and writes. Wraps DbHelper so
 * that Activities never touch SQL directly - they just call plain Java
 * methods like addPantryItem() or getAllRecipesWithIngredients().
 */
public class PantryRepository {

    private final DbHelper dbHelper;

    public PantryRepository(Context context) {
        dbHelper = new DbHelper(context.getApplicationContext());
    }

    // ---------- Pantry CRUD ----------

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DbHelper.TABLE_PANTRY, null, toContentValues(item));
        db.close();
        return id;
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.update(DbHelper.TABLE_PANTRY, toContentValues(item),
                DbHelper.COL_P_ID + "=?", new String[]{String.valueOf(item.getId())});
        db.close();
        return rows;
    }

    public void deletePantryItem(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_PANTRY, DbHelper.COL_P_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DbHelper.TABLE_PANTRY, null, null, null, null, null, DbHelper.COL_P_NAME + " ASC");
        while (c.moveToNext()) {
            items.add(cursorToPantryItem(c));
        }
        c.close();
        db.close();
        return items;
    }

    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DbHelper.TABLE_PANTRY, null, DbHelper.COL_P_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) item = cursorToPantryItem(c);
        c.close();
        db.close();
        return item;
    }

    // ---------- Recipes (read-only, pre-seeded in DbHelper) ----------

    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor rc = db.query(DbHelper.TABLE_RECIPES, null, null, null, null, null, DbHelper.COL_R_NAME + " ASC");
        while (rc.moveToNext()) {
            long id = rc.getLong(rc.getColumnIndexOrThrow(DbHelper.COL_R_ID));
            String name = rc.getString(rc.getColumnIndexOrThrow(DbHelper.COL_R_NAME));
            String steps = rc.getString(rc.getColumnIndexOrThrow(DbHelper.COL_R_STEPS));
            recipes.add(new Recipe(id, name, steps, getIngredientsForRecipe(db, id)));
        }
        rc.close();
        db.close();
        return recipes;
    }

    public Recipe getRecipeById(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor rc = db.query(DbHelper.TABLE_RECIPES, null, DbHelper.COL_R_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        Recipe recipe = null;
        if (rc.moveToFirst()) {
            String name = rc.getString(rc.getColumnIndexOrThrow(DbHelper.COL_R_NAME));
            String steps = rc.getString(rc.getColumnIndexOrThrow(DbHelper.COL_R_STEPS));
            recipe = new Recipe(recipeId, name, steps, getIngredientsForRecipe(db, recipeId));
        }
        rc.close();
        db.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor ic = db.query(DbHelper.TABLE_RECIPE_INGREDIENTS, null, DbHelper.COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        while (ic.moveToNext()) {
            long id = ic.getLong(ic.getColumnIndexOrThrow(DbHelper.COL_RI_ID));
            String name = ic.getString(ic.getColumnIndexOrThrow(DbHelper.COL_RI_NAME));
            double qty = ic.getDouble(ic.getColumnIndexOrThrow(DbHelper.COL_RI_QTY));
            String unit = ic.getString(ic.getColumnIndexOrThrow(DbHelper.COL_RI_UNIT));
            list.add(new RecipeIngredient(id, recipeId, name, qty, unit));
        }
        ic.close();
        return list;
    }

    // ---------- Helpers ----------

    private ContentValues toContentValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_P_NAME, item.getName());
        cv.put(DbHelper.COL_P_QTY, item.getQuantity());
        cv.put(DbHelper.COL_P_UNIT, item.getUnit());
        cv.put(DbHelper.COL_P_EXPIRY, item.getExpiryDate());
        return cv;
    }

    private PantryItem cursorToPantryItem(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(DbHelper.COL_P_ID));
        String name = c.getString(c.getColumnIndexOrThrow(DbHelper.COL_P_NAME));
        double qty = c.getDouble(c.getColumnIndexOrThrow(DbHelper.COL_P_QTY));
        String unit = c.getString(c.getColumnIndexOrThrow(DbHelper.COL_P_UNIT));
        String expiry = c.getString(c.getColumnIndexOrThrow(DbHelper.COL_P_EXPIRY));
        return new PantryItem(id, name, qty, unit, expiry);
    }
}
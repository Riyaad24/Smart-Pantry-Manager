package com.example.smartpantrymanager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartpantrymanager.model.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {
    @Query("SELECT * FROM recipes")
    List<Recipe> getAll();

    @Query("SELECT * FROM recipes WHERE id = :id")
    Recipe getById(int id);

    @Insert
    void insert(Recipe recipe);

    @Insert
    void insertAll(List<Recipe> recipes);

    @Update
    void update(Recipe recipe);

    @Delete
    void delete(Recipe recipe);
}

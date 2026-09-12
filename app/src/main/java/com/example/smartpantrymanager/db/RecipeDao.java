package com.example.smartpantrymanager.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartpantrymanager.model.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {
    @Query("SELECT * FROM recipes")
    List<Recipe> getAll();

    @Query("SELECT * FROM recipes WHERE id = :id")
    Recipe getById(int id);

    @Insert
    void insertAll(List<Recipe> recipes);
}
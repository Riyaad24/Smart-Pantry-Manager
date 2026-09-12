package com.example.smartpantrymanager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

@Dao
public interface PantryDao {
    @Query("SELECT * FROM pantry_items")
    List<PantryItem> getAll();

    @Insert
    void insert(PantryItem item);

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);
}
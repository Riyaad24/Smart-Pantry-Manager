package com.example.smartpantrymanager.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartpantrymanager.model.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User findById(int id);

    @Insert
    long insert(User user);

    @Update
    void update(User user);
}

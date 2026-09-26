package com.example.smartpantrymanager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Represents a single ingredient the user currently has in their pantry. */
@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String name;
    public double quantity;
    public String unit; // e.g kg, pcs, ml
    public String expiryDate;

    public PantryItem(int userId, String name, double quantity, String unit, String expiryDate) {
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }
}

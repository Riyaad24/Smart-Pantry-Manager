package com.example.smartpantrymanager.util;

/**
 * Small helper for comparing pantry quantities against recipe requirements
 * even when they're recorded in different (but compatible) units, e.g.
 * "1 kg" in the pantry correctly satisfies a recipe that needs "500 g".
 */
public class UnitUtils {

    private UnitUtils() { }

    /** Returns which measurement "family" a unit belongs to: weight, volume, or count. */
    public static String category(String unit) {
        if (unit == null) return "unit";
        String u = unit.trim().toLowerCase();
        switch (u) {
            case "g": case "gram": case "grams": case "kg": case "kilogram": case "kilograms":
                return "g";
            case "ml": case "millilitre": case "millilitres": case "milliliter": case "milliliters":
            case "l": case "litre": case "litres": case "liter": case "liters":
            case "tsp": case "teaspoon": case "teaspoons":
            case "tbsp": case "tablespoon": case "tablespoons":
            case "cup": case "cups":
                return "ml";
            default:
                return "unit"; // whole items - "2 eggs", "3 onions" etc.
        }
    }

    /** Converts a quantity to a common base (grams for weight, millilitres for volume). */
    public static double toBase(double quantity, String unit) {
        if (unit == null) return quantity;
        String u = unit.trim().toLowerCase();
        switch (u) {
            case "kg": case "kilogram": case "kilograms":
                return quantity * 1000.0;
            case "l": case "litre": case "litres": case "liter": case "liters":
                return quantity * 1000.0;
            case "tbsp": case "tablespoon": case "tablespoons":
                return quantity * 15.0;
            case "tsp": case "teaspoon": case "teaspoons":
                return quantity * 5.0;
            case "cup": case "cups":
                return quantity * 250.0;
            default:
                return quantity; // already in base unit (g, ml) or a whole-item count
        }
    }
}
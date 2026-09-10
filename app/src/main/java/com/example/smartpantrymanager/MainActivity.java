package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.db.PantryRepository;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantry List screen (Section 2.2) - shows every ingredient currently in the
 * user's pantry via a RecyclerView bound to the database. Supports Create
 * (FAB -> AddEditIngredientActivity), Read (this list), and hands off to the
 * same screen for Update, with Delete available inline per row.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    public static final String EXTRA_PANTRY_ITEM_ID = "extra_pantry_item_id";

    private PantryRepository repository;
    private PantryAdapter adapter;
    private final List<PantryItem> pantryItems = new ArrayList<>();
    private View emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_pantry);

        repository = new PantryRepository(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(pantryItems, this);
        recyclerView.setAdapter(adapter);

        emptyStateView = findViewById(R.id.textEmptyPantry);

        FloatingActionButton fab = findViewById(R.id.fabAddIngredient);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                return true;
            } else if (id == R.id.nav_recipes) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems(); // refresh every time we return here after add/edit/delete
    }

    private void loadPantryItems() {
        pantryItems.clear();
        pantryItems.addAll(repository.getAllPantryItems());
        adapter.notifyDataSetChanged();
        emptyStateView.setVisibility(pantryItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_PANTRY_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove \"" + item.getName() + "\" from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deletePantryItem(item.getId());
                    loadPantryItems();
                    Toast.makeText(this, "Ingredient removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
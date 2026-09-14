package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

public class PantryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private TextView lblIngredientsCount;
    private View containerDashboardHub;
    private View containerInventoryList;
    
    private PantryAdapter adapter;
    private AppDatabase db;
    private List<PantryItem> pantryList;
    private boolean isShowingInventoryList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        // Map fresh view handles cleanly
        containerDashboardHub = findViewById(R.id.containerDashboardHub);
        containerInventoryList = findViewById(R.id.containerInventoryList);
        lblIngredientsCount = findViewById(R.id.lblIngredientsCount);
        
        recyclerView = findViewById(R.id.recyclerPantry);
        txtEmpty = findViewById(R.id.txtEmptyPantry);
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        View btnViewInventoryList = findViewById(R.id.btnViewInventoryList);

        db = AppDatabase.getInstance(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            startActivity(new Intent(PantryListActivity.this, AddEditActivity.class));
        });

        // Toggle state view layers fluidly on user request click
        btnViewInventoryList.setOnClickListener(v -> {
            exposeInventoryListView(true);
        });

        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_suggested) {
                startActivity(new Intent(PantryListActivity.this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(PantryListActivity.this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_pantry) {
                if (isShowingInventoryList) {
                    exposeInventoryListView(false);
                }
                return true;
            }
            return false;
        });

        // Register secure AndroidX predictive back callback handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isShowingInventoryList) {
                    exposeInventoryListView(false);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry(); // Refresh counts parameters dynamically
    }

    private void exposeInventoryListView(boolean showList) {
        isShowingInventoryList = showList;
        if (showList) {
            containerDashboardHub.setVisibility(View.GONE);
            containerInventoryList.setVisibility(View.VISIBLE);
        } else {
            containerDashboardHub.setVisibility(View.VISIBLE);
            containerInventoryList.setVisibility(View.GONE);
        }
    }

    private void loadPantry() {
        pantryList = db.pantryDao().getAll();
        
        // Dynamically update primary kitchen summary indicator string parameters
        if (pantryList.isEmpty()) {
            lblIngredientsCount.setText("No ingredients available");
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            lblIngredientsCount.setText(pantryList.size() + " ingredients available");
            txtEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new PantryAdapter(this, pantryList, db);
            recyclerView.setAdapter(adapter);
        }
    }
}
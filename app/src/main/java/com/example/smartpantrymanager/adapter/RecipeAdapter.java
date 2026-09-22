package com.example.smartpantrymanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.SuggestedRecipesActivity;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.net.NetworkImageLoader;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClick {
        void onClick(Recipe recipe);
    }

    private List<Recipe> recipes;
    private Context context;
    private OnRecipeClick listener;

    public RecipeAdapter(Context c, List<Recipe> r, OnRecipeClick l) {
        this.context = c;
        this.recipes = r;
        this.listener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recipe, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Recipe r = recipes.get(pos);
        h.name.setText(r.name);
        
        // Visual indicators for strict matching logic
        if (r.mealIdApi != null) {
            // For API results, we show they are live online suggestions
            h.count.setText("Live Match • Online Selection");
            h.count.setBackgroundResource(R.color.accent_indigo_dim);
            h.count.setTextColor(ContextCompat.getColor(context, R.color.accent_indigo));
        } else {
            // For local seeded recipes, they appear ONLY if they are a 100% match (Strict Logic)
            h.count.setText("100% Match • Ready to cook");
            h.count.setBackgroundResource(R.color.success_green_bg);
            h.count.setTextColor(ContextCompat.getColor(context, R.color.success_green));
        }

        // Asynchronously fetch and bind live food image thumbnail via custom network loader
        if (r.imageUrl != null && !r.imageUrl.isEmpty()) {
            NetworkImageLoader.displayImage(r.imageUrl, h.imgThumb);
        } else if (r.ingredientsCsv != null && r.ingredientsCsv.startsWith("http")) {
            NetworkImageLoader.displayImage(r.ingredientsCsv, h.imgThumb);
        } else {
            h.imgThumb.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, count;
        ImageView imgThumb;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.txtRecipeName);
            count = v.findViewById(R.id.txtRecipeCount);
            imgThumb = v.findViewById(R.id.imgRecipeThumb);
        }
    }
}
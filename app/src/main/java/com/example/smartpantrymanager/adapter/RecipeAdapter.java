package com.example.smartpantrymanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Recipe;

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
        h.count.setText(r.getIngredients().size() + " ingredients");
        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, count;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.txtRecipeName);
            count = v.findViewById(R.id.txtRecipeCount);
        }
    }
}
package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.RecipeMatchResult;

import java.util.List;

/**
 * Binds a list of RecipeMatchResult to a RecyclerView. Used for both the
 * strict "Suggested Recipes" list (showMissing = false) and the optional
 * "Almost There" list (showMissing = true, shows what's still needed).
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(long recipeId);
    }

    private final List<RecipeMatchResult> results;
    private final boolean showMissing;
    private final Listener listener;

    public RecipeAdapter(List<RecipeMatchResult> results, boolean showMissing, Listener listener) {
        this.results = results;
        this.showMissing = showMissing;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeMatchResult result = results.get(position);
        holder.name.setText(result.getRecipe().getName());

        List<String> missing = result.getMissingIngredients();
        if (showMissing && !missing.isEmpty()) {
            StringBuilder sb = new StringBuilder("Missing: ");
            for (int i = 0; i < missing.size(); i++) {
                sb.append(missing.get(i));
                if (i < missing.size() - 1) sb.append(", ");
            }
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(sb.toString());
        } else {
            holder.subtitle.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(result.getRecipe().getId()));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textRecipeName);
            subtitle = itemView.findViewById(R.id.textRecipeSubtitle);
        }
    }
}
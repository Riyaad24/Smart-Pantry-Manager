package com.example.smartpantrymanager.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.AddEditActivity;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.db.AppDatabase;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private Context context;
    private List<PantryItem> items;
    private AppDatabase db;

    public PantryAdapter(Context context, List<PantryItem> items, AppDatabase db) {
        this.context = context;
        this.items = items;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.txtName.setText(item.getName());
        holder.txtQty.setText(item.getQuantity() + " " + item.getUnit());

        holder.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(context, AddEditActivity.class);
            i.putExtra("pantry_id", item.getId());
            i.putExtra("pantry_name", item.getName());
            i.putExtra("pantry_qty", item.getQuantity());
            i.putExtra("pantry_unit", item.getUnit());
            i.putExtra("pantry_expiry", item.getExpiryDate());
            context.startActivity(i);
        });

        holder.btnDelete.setOnClickListener(v -> {
            db.pantryDao().delete(item);
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, items.size());
                Toast.makeText(context, "Deleted " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtQty;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPantryName);
            txtQty = itemView.findViewById(R.id.txtPantryQty);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
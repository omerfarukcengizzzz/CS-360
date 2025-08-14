package com.omercengiz.warehousepro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> inventoryList;
    private Context context;
    private DatabaseHelper databaseHelper;
    private OnItemUpdateListener updateListener;

    // Interface for callbacks to activity
    public interface OnItemUpdateListener {
        void onItemUpdated();
        void onQuantityZero(String itemName);
    }

    public InventoryAdapter(Context context, List<InventoryItem> inventoryList,
                            OnItemUpdateListener listener) {
        this.context = context;
        this.inventoryList = inventoryList;
        this.databaseHelper = new DatabaseHelper(context);
        this.updateListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = inventoryList.get(position);

        // Set item data
        holder.itemName.setText(item.getName());
        holder.itemWeight.setText(item.getWeight());
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));
        holder.itemNotes.setText(item.getNotes());

        // Handle minus button click
        holder.btnMinus.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 0) {
                int newQuantity = currentQuantity - 1;

                // Update database
                if (databaseHelper.updateItemQuantity(item.getId(), newQuantity)) {
                    // Update item in list
                    item.setQuantity(newQuantity);
                    holder.itemQuantity.setText(String.valueOf(newQuantity));

                    // Check if quantity reached zero
                    if (newQuantity == 0) {
                        updateListener.onQuantityZero(item.getName());
                        Toast.makeText(context, item.getName() + " is out of stock!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Handle plus button click
        holder.btnPlus.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            int newQuantity = currentQuantity + 1;

            // Update database
            if (databaseHelper.updateItemQuantity(item.getId(), newQuantity)) {
                // Update item in list
                item.setQuantity(newQuantity);
                holder.itemQuantity.setText(String.valueOf(newQuantity));
            }
        });

        // Handle delete button click
        holder.btnDelete.setOnClickListener(v -> {
            // Delete from database
            if (databaseHelper.deleteItem(item.getId())) {
                // Remove from list
                inventoryList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, inventoryList.size());

                Toast.makeText(context, item.getName() + " deleted", Toast.LENGTH_SHORT).show();

                // Notify activity to refresh if needed
                updateListener.onItemUpdated();
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemWeight, itemQuantity, itemNotes;
        ImageButton btnMinus, btnPlus, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemName = itemView.findViewById(R.id.itemName);
            itemWeight = itemView.findViewById(R.id.itemWeight);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            itemNotes = itemView.findViewById(R.id.itemNotes);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
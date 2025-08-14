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
import java.util.ArrayList;
import java.util.List;

/**
 * InventoryAdapter - RecyclerView adapter for displaying inventory items
 * Handles the display and interaction of inventory items in a list/grid format
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private Context context;
    private List<InventoryItem> inventoryItems;
    private List<InventoryItem> filteredItems; // For search functionality
    private OnItemActionListener listener;
    private DatabaseHelper databaseHelper;

    /**
     * Interface for handling item actions (callbacks to parent activity)
     */
    public interface OnItemActionListener {
        void onQuantityChanged(InventoryItem item, int newQuantity);
        void onItemDeleted(InventoryItem item);
        void onItemClicked(InventoryItem item);
        void onZeroQuantityReached(InventoryItem item);
    }

    /**
     * Constructor for InventoryAdapter
     * @param context Activity context
     * @param inventoryItems List of inventory items to display
     * @param listener Callback listener for item actions
     */
    public InventoryAdapter(Context context, List<InventoryItem> inventoryItems, OnItemActionListener listener) {
        this.context = context;
        this.inventoryItems = inventoryItems != null ? inventoryItems : new ArrayList<>();
        this.filteredItems = new ArrayList<>(this.inventoryItems);
        this.listener = listener;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = filteredItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    /**
     * ViewHolder class for individual inventory items
     */
    public class InventoryViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName;
        private TextView itemWeight;
        private TextView itemQuantity;
        private TextView itemNotes;
        private ImageButton btnMinus;
        private ImageButton btnPlus;
        private ImageButton btnDelete;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views from item_inventory.xml
            itemName = itemView.findViewById(R.id.itemName);
            itemWeight = itemView.findViewById(R.id.itemWeight);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            itemNotes = itemView.findViewById(R.id.itemNotes);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        /**
         * Bind data to the view holder
         * @param item InventoryItem to display
         */
        public void bind(InventoryItem item) {
            // Set basic item information
            itemName.setText(item.getName());
            itemWeight.setText(item.getFormattedWeight());
            itemQuantity.setText(String.valueOf(item.getQuantity()));
            itemNotes.setText(item.getDisplayNotes());

            // Set quantity text color based on stock status
            updateQuantityDisplay(item);

            // Set click listeners
            setupClickListeners(item);
        }

        /**
         * Update quantity display with appropriate styling
         * @param item InventoryItem to check status
         */
        private void updateQuantityDisplay(InventoryItem item) {
            int quantity = item.getQuantity();
            itemQuantity.setText(String.valueOf(quantity));

            // Change text color based on stock status
            if (item.isOutOfStock()) {
                itemQuantity.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                itemQuantity.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (item.isLowStock()) {
                itemQuantity.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                itemQuantity.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                itemQuantity.setTextColor(context.getResources().getColor(android.R.color.black));
                itemQuantity.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }

        /**
         * Setup click listeners for all interactive elements
         * @param item InventoryItem for this row
         */
        private void setupClickListeners(InventoryItem item) {

            // Plus button - increase quantity
            btnPlus.setOnClickListener(v -> {
                int currentQuantity = item.getQuantity();
                int newQuantity = currentQuantity + 1;

                // Update database
                if (databaseHelper.updateItemQuantity(item.getId(), newQuantity)) {
                    item.setQuantity(newQuantity);
                    updateQuantityDisplay(item);

                    // Notify listener
                    if (listener != null) {
                        listener.onQuantityChanged(item, newQuantity);
                    }

                    showToast("Quantity increased to " + newQuantity);
                } else {
                    showToast("Failed to update quantity");
                }
            });

            // Minus button - decrease quantity
            btnMinus.setOnClickListener(v -> {
                int currentQuantity = item.getQuantity();

                if (currentQuantity > 0) {
                    int newQuantity = currentQuantity - 1;

                    // Update database
                    if (databaseHelper.updateItemQuantity(item.getId(), newQuantity)) {
                        item.setQuantity(newQuantity);
                        updateQuantityDisplay(item);

                        // Notify listener
                        if (listener != null) {
                            listener.onQuantityChanged(item, newQuantity);

                            // Check if quantity reached zero (for SMS notification)
                            if (newQuantity == 0) {
                                listener.onZeroQuantityReached(item);
                            }
                        }

                        if (newQuantity == 0) {
                            showToast("⚠️ " + item.getName() + " is now out of stock!");
                        } else {
                            showToast("Quantity decreased to " + newQuantity);
                        }
                    } else {
                        showToast("Failed to update quantity");
                    }
                } else {
                    showToast("Quantity cannot be less than 0");
                }
            });

            // Delete button - remove item
            btnDelete.setOnClickListener(v -> {
                // Show confirmation before deleting
                showDeleteConfirmation(item);
            });

            // Item click - show details or edit
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClicked(item);
                }
            });

            // Long click for additional actions
            itemView.setOnLongClickListener(v -> {
                showToast("Item: " + item.getName() + "\nStatus: " + item.getStatusText());
                return true;
            });
        }

        /**
         * Show delete confirmation dialog
         * @param item InventoryItem to delete
         */
        private void showDeleteConfirmation(InventoryItem item) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete '" + item.getName() + "'?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteItem(item);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        /**
         * Delete item from database and update UI
         * @param item InventoryItem to delete
         */
        private void deleteItem(InventoryItem item) {
            if (databaseHelper.deleteInventoryItem(item.getId())) {
                // Remove from both lists
                inventoryItems.remove(item);
                filteredItems.remove(item);

                // Notify adapter
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, filteredItems.size());
                }

                // Notify listener
                if (listener != null) {
                    listener.onItemDeleted(item);
                }

                showToast("Item deleted: " + item.getName());
            } else {
                showToast("Failed to delete item");
            }
        }

        /**
         * Show toast message
         * @param message Message to display
         */
        private void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    // ================== ADAPTER METHODS ==================

    /**
     * Update the entire item list and refresh display
     * @param newItems New list of inventory items
     */
    public void updateItems(List<InventoryItem> newItems) {
        this.inventoryItems.clear();
        this.inventoryItems.addAll(newItems);
        this.filteredItems.clear();
        this.filteredItems.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Add a new item to the list
     * @param item InventoryItem to add
     */
    public void addItem(InventoryItem item) {
        inventoryItems.add(item);
        filteredItems.add(item);
        notifyItemInserted(filteredItems.size() - 1);
    }

    /**
     * Filter items based on search query
     * @param query Search query string
     */
    public void filter(String query) {
        filteredItems.clear();

        if (query == null || query.trim().isEmpty()) {
            // Show all items if query is empty
            filteredItems.addAll(inventoryItems);
        } else {
            // Filter items that contain the query in their name
            String lowerCaseQuery = query.toLowerCase().trim();
            for (InventoryItem item : inventoryItems) {
                if (item.getName().toLowerCase().contains(lowerCaseQuery) ||
                        item.getNotes().toLowerCase().contains(lowerCaseQuery)) {
                    filteredItems.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Get item at specific position
     * @param position Position in filtered list
     * @return InventoryItem at position
     */
    public InventoryItem getItem(int position) {
        if (position >= 0 && position < filteredItems.size()) {
            return filteredItems.get(position);
        }
        return null;
    }

    /**
     * Check if adapter has any items
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return filteredItems.isEmpty();
    }

    /**
     * Get count of out-of-stock items
     * @return number of items with zero quantity
     */
    public int getOutOfStockCount() {
        int count = 0;
        for (InventoryItem item : inventoryItems) {
            if (item.isOutOfStock()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get count of low-stock items
     * @return number of items with low stock
     */
    public int getLowStockCount() {
        int count = 0;
        for (InventoryItem item : inventoryItems) {
            if (item.isLowStock() && !item.isOutOfStock()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get total inventory count
     * @return total number of items
     */
    public int getTotalItemCount() {
        return inventoryItems.size();
    }

    /**
     * Refresh data from database
     */
    public void refreshData() {
        List<InventoryItem> freshData = databaseHelper.getAllInventoryItems();
        updateItems(freshData);
    }
}
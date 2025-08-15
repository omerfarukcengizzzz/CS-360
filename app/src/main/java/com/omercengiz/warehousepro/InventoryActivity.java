package com.omercengiz.warehousepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnItemActionListener {

    private RecyclerView inventoryRecyclerView;
    private EditText searchBar;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddItem;

    private DatabaseHelper databaseHelper;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> inventoryItems;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Get username from intent
        currentUsername = getIntent().getStringExtra("USERNAME");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearchBar();

        // Setup FAB click listener
        setupFAB();

        // Load inventory data
        loadInventoryData();
    }

    private void initializeViews() {
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        emptyState = findViewById(R.id.emptyState);
        fabAddItem = findViewById(R.id.fabAddItem);
    }

    private void setupRecyclerView() {
        // Set layout manager
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        inventoryItems = databaseHelper.getAllInventoryItems();
        inventoryAdapter = new InventoryAdapter(this, inventoryItems, this);

        // Set adapter to RecyclerView
        inventoryRecyclerView.setAdapter(inventoryAdapter);
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter inventory items as user types
                if (inventoryAdapter != null) {
                    inventoryAdapter.filter(s.toString());
                    updateEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFAB() {
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to AddItemActivity
                Intent intent = new Intent(InventoryActivity.this, AddItemActivity.class);
                intent.putExtra("USERNAME", currentUsername);
                startActivityForResult(intent, 100); // Request code 100 for adding items
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void loadInventoryData() {
        try {
            // Get all inventory items from database
            inventoryItems = databaseHelper.getAllInventoryItems();

            // Update adapter
            if (inventoryAdapter != null) {
                inventoryAdapter.updateItems(inventoryItems);
            }

            // Update empty state
            updateEmptyState();

            // Show welcome message
            if (currentUsername != null) {
                Toast.makeText(this, "Welcome to Inventory, " + currentUsername + "!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading inventory: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateEmptyState() {
        if (inventoryAdapter.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            inventoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            inventoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // ================== InventoryAdapter.OnItemActionListener Implementation ==================

    @Override
    public void onQuantityChanged(InventoryItem item, int newQuantity) {
        // This is handled automatically by the adapter
        // We can add additional logic here if needed
        updateEmptyState();
    }

    @Override
    public void onItemDeleted(InventoryItem item) {
        // This is handled automatically by the adapter
        // Update empty state after deletion
        updateEmptyState();
        Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClicked(InventoryItem item) {
        // Handle item click - could open edit dialog or detail view
        showItemDetails(item);
    }

    @Override
    public void onZeroQuantityReached(InventoryItem item) {
        // Handle zero quantity reached - trigger SMS notification
        triggerLowStockNotification(item);
    }

    // ================== Helper Methods ==================

    private void showItemDetails(InventoryItem item) {
        // Show item details in a toast for now
        // Later this could open an edit dialog or detail activity
        String details = "Item: " + item.getName() +
                "\nWeight: " + item.getFormattedWeight() +
                "\nQuantity: " + item.getQuantity() +
                "\nStatus: " + item.getStatusText() +
                "\nNotes: " + item.getDisplayNotes();

        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }

    private void triggerLowStockNotification(InventoryItem item) {
        // Check if we should send SMS notification
        // This will integrate with your SMS functionality
        String message = "⚠️ LOW STOCK ALERT: " + item.getName() + " quantity is now 0!";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // TODO: Integrate with SMS notification system
        // For now, just show a prominent notification
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Stock Alert!")
                .setMessage(item.getName() + " is now out of stock. Consider reordering immediately.")
                .setPositiveButton("Got it", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // ================== Activity Lifecycle ==================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // New item was added, refresh the inventory
            loadInventoryData();
            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadInventoryData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        // Override back button to go back to login
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
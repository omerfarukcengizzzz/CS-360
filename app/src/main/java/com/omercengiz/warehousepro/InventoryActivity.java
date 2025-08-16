package com.omercengiz.warehousepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
    private SharedPreferences preferences;

    // Keys for tracking user actions
    private static final String PREFS_NAME = "WarehouseProPrefs";
    private static final String KEY_FIRST_LOGIN = "first_login_";
    private static final String KEY_LAST_WELCOME_SHOWN = "last_welcome_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Get username from intent
        currentUsername = getIntent().getStringExtra("USERNAME");

        // Initialize preferences
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

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

        // Show welcome message only on first login
        showWelcomeMessageIfNeeded();
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

        } catch (Exception e) {
            Toast.makeText(this, "Error loading inventory: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show welcome message only when appropriate
     */
    private void showWelcomeMessageIfNeeded() {
        if (currentUsername == null) return;

        // Check if this is the user's first login
        boolean isFirstLogin = preferences.getBoolean(KEY_FIRST_LOGIN + currentUsername, true);

        // Check when we last showed a welcome (to avoid spam)
        long lastWelcomeShown = preferences.getLong(KEY_LAST_WELCOME_SHOWN, 0);
        long currentTime = System.currentTimeMillis();
        long timeSinceLastWelcome = currentTime - lastWelcomeShown;

        // Only show welcome if:
        // 1. It's first login, OR
        // 2. It's been more than 24 hours since last welcome
        if (isFirstLogin || timeSinceLastWelcome > 24 * 60 * 60 * 1000) {
            String message = isFirstLogin ?
                    "Welcome to Warehouse Pro, " + currentUsername + "!" :
                    "Welcome back, " + currentUsername + "!";

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // Update preferences
            preferences.edit()
                    .putBoolean(KEY_FIRST_LOGIN + currentUsername, false)
                    .putLong(KEY_LAST_WELCOME_SHOWN, currentTime)
                    .apply();
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
        // Removed the "Item deleted successfully" toast since adapter already shows it
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
        String details = "📦 " + item.getName() +
                "\n⚖️ " + item.getFormattedWeight() +
                "\n📊 Qty: " + item.getQuantity() +
                "\n📝 " + item.getDisplayNotes() +
                "\n🔔 " + item.getStatusText();

        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }

    private void triggerLowStockNotification(InventoryItem item) {
        // Check if we should send SMS notification
        // This will integrate with SMS functionality
        String message = "⚠️ OUT OF STOCK: " + item.getName();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Show SMS confirmation dialog (less intrusive than before)
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Stock Alert")
                .setMessage(item.getName() + " is out of stock. Send SMS alert?")
                .setPositiveButton("Send", (dialog, which) -> {
                    sendSMSNotification(item);
                })
                .setNegativeButton("Skip", null)
                .setIcon(android.R.drawable.ic_dialog_info) // Less alarming icon
                .show();
    }

    private void sendSMSNotification(InventoryItem item) {
        SMSManagerHelper smsManager = new SMSManagerHelper(this);

        // Check SMS permission first
        if (!smsManager.isSMSPermissionGranted()) {
            Toast.makeText(this, "SMS permission needed for alerts", Toast.LENGTH_SHORT).show();

            // Navigate to SMS permission screen
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
            return;
        }

        // Send SMS in background to avoid blocking UI
        AsyncTask.execute(() -> {
            boolean smsResult = smsManager.sendLowStockAlert(item);

            // Update UI on main thread
            runOnUiThread(() -> {
                if (smsResult) {
                    Toast.makeText(this, "✅ SMS sent for " + item.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "❌ SMS failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ================== Activity Lifecycle ==================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // New item was added, refresh the inventory
            loadInventoryData();
            // Show brief success message (less intrusive)
            Toast.makeText(this, "✅ Item added", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity (but no welcome message)
        loadInventoryData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (inventoryAdapter != null) {
            inventoryAdapter.cleanup();
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
package com.omercengiz.warehousepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnItemUpdateListener {

    private RecyclerView inventoryRecyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> inventoryList;
    private DatabaseHelper databaseHelper;
    private LinearLayout emptyState;
    private EditText searchBar;
    private List<InventoryItem> fullInventoryList; // For search functionality
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize
        databaseHelper = new DatabaseHelper(this);
        inventoryList = new ArrayList<>();
        fullInventoryList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Setup views
        setupViews();

        // Setup toolbar
        setupToolbar();

        // Load inventory data
        loadInventoryData();

        // Setup search functionality
        setupSearch();
    }

    private void setupViews() {
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        searchBar = findViewById(R.id.searchBar);

        // Setup RecyclerView
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inventoryAdapter = new InventoryAdapter(this, inventoryList, this);
        inventoryRecyclerView.setAdapter(inventoryAdapter);

        // Setup FAB
        FloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, AddItemActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Inventory Management");

        // Add logout functionality to toolbar
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> {
            // Clear session and logout
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Go back to login
            Intent intent = new Intent(InventoryActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadInventoryData() {
        inventoryList.clear();
        fullInventoryList.clear();

        // Get all items from database
        Cursor cursor = databaseHelper.getAllItems();

        if (cursor != null && cursor.moveToFirst()) {
            // Get column indices safely
            int idIndex = cursor.getColumnIndexOrThrow("item_id");
            int nameIndex = cursor.getColumnIndexOrThrow("item_name");
            int weightIndex = cursor.getColumnIndexOrThrow("weight");
            int quantityIndex = cursor.getColumnIndexOrThrow("quantity");
            int notesIndex = cursor.getColumnIndexOrThrow("notes");

            do {
                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String weight = cursor.getString(weightIndex);
                int quantity = cursor.getInt(quantityIndex);
                String notes = cursor.getString(notesIndex);

                InventoryItem item = new InventoryItem(id, name, weight, quantity, notes);
                inventoryList.add(item);
                fullInventoryList.add(item);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Update UI
        updateEmptyState();
        inventoryAdapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterInventory(s.toString());
            }
        });
    }

    private void filterInventory(String searchText) {
        inventoryList.clear();

        if (searchText.isEmpty()) {
            inventoryList.addAll(fullInventoryList);
        } else {
            String searchLower = searchText.toLowerCase();
            for (InventoryItem item : fullInventoryList) {
                if (item.getName().toLowerCase().contains(searchLower) ||
                        item.getNotes().toLowerCase().contains(searchLower)) {
                    inventoryList.add(item);
                }
            }
        }

        inventoryAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (inventoryList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            inventoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            inventoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from add item screen
        loadInventoryData();
    }

    @Override
    public void onItemUpdated() {
        // Refresh empty state if needed
        updateEmptyState();
    }

    @Override
    public void onQuantityZero(String itemName) {
        // Check if SMS permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            // Get SMS notification preference
            SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
            boolean smsEnabled = prefs.getBoolean("sms_enabled", false);

            if (smsEnabled) {
                // Send SMS notification
                sendSMSNotification(itemName);
            }
        }
    }

    private void sendSMSNotification(String itemName) {
        try {
            // Get phone number from preferences (you might want to add a settings screen for this)
            SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
            String phoneNumber = prefs.getString("phone_number", "");

            if (!phoneNumber.isEmpty()) {
                String message = "Warehouse Pro Alert: " + itemName + " is now out of stock!";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);

                Toast.makeText(this, "SMS alert sent", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS alert", Toast.LENGTH_SHORT).show();
        }
    }
}
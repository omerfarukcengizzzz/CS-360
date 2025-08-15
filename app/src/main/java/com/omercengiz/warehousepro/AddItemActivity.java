package com.omercengiz.warehousepro;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "AddItemActivity";

    private TextInputEditText itemNameInput;
    private TextInputEditText weightInput;
    private TextInputEditText quantityInput;
    private TextInputEditText notesInput;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    private DatabaseHelper databaseHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Log.d(TAG, "AddItemActivity onCreate started");

        // Get username from intent
        currentUsername = getIntent().getStringExtra("USERNAME");
        Log.d(TAG, "Current username: " + currentUsername);

        // Initialize database helper
        try {
            databaseHelper = new DatabaseHelper(this);
            Log.d(TAG, "Database helper initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database helper: " + e.getMessage(), e);
        }

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        itemNameInput = findViewById(R.id.itemNameInput);
        weightInput = findViewById(R.id.weightInput);
        quantityInput = findViewById(R.id.quantityInput);
        notesInput = findViewById(R.id.notesInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        Log.d(TAG, "Views initialized");
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Save button clicked");
                handleSaveItem();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Cancel button clicked");
                handleCancel();
            }
        });
    }

    /**
     * Handle saving the new inventory item
     */
    private void handleSaveItem() {
        Log.d(TAG, "handleSaveItem started");

        try {
            // Get input values
            String itemName = itemNameInput.getText().toString().trim();
            String weightStr = weightInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();

            Log.d(TAG, "Input values - Name: '" + itemName + "', Weight: '" + weightStr +
                    "', Quantity: '" + quantityStr + "', Notes: '" + notes + "'");

            // Validate inputs
            if (!validateInputs(itemName, weightStr, quantityStr)) {
                Log.d(TAG, "Input validation failed");
                return; // Validation failed
            }

            // Parse numeric values
            double weight;
            int quantity;

            try {
                weight = Double.parseDouble(weightStr);
                quantity = Integer.parseInt(quantityStr);
                Log.d(TAG, "Parsed values - Weight: " + weight + ", Quantity: " + quantity);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Number parsing error: " + e.getMessage());
                Toast.makeText(this, "Please enter valid numbers for weight and quantity.", Toast.LENGTH_LONG).show();
                return;
            }

            // Check database helper
            if (databaseHelper == null) {
                Log.e(TAG, "Database helper is null!");
                Toast.makeText(this, "Database error. Please restart the app.", Toast.LENGTH_LONG).show();
                return;
            }

            // Create new inventory item for validation
            InventoryItem newItem = new InventoryItem(itemName, weight, quantity, notes);
            Log.d(TAG, "Created InventoryItem: " + newItem.toString());

            // Validate the item
            if (!newItem.isValid()) {
                Log.e(TAG, "InventoryItem validation failed");
                Toast.makeText(this, "Invalid item data. Please check your inputs.", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "About to call databaseHelper.addInventoryItem()");

            // Save to database
            boolean saveResult = databaseHelper.addInventoryItem(itemName, weight, quantity, notes);
            Log.d(TAG, "Database save result: " + saveResult);

            if (saveResult) {
                // Success
                Log.d(TAG, "Item saved successfully");
                Toast.makeText(this, "Item '" + itemName + "' added successfully!", Toast.LENGTH_SHORT).show();

                // Set result and finish
                setResult(RESULT_OK);
                finish();

            } else {
                // Failed to save
                Log.e(TAG, "Database save returned false");
                Toast.makeText(this, "Failed to add item. Database error occurred.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception in handleSaveItem: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving item: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handle cancel button - go back without saving
     */
    private void handleCancel() {
        Log.d(TAG, "handleCancel called");

        // Check if user has entered any data
        if (hasUnsavedChanges()) {
            Log.d(TAG, "Has unsaved changes, showing confirmation dialog");
            // Show confirmation dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to go back?")
                    .setPositiveButton("Discard", (dialog, which) -> {
                        Log.d(TAG, "User chose to discard changes");
                        setResult(RESULT_CANCELED);
                        finish();
                    })
                    .setNegativeButton("Continue Editing", null)
                    .show();
        } else {
            Log.d(TAG, "No unsaved changes, going back");
            // No changes, safe to go back
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs(String itemName, String weightStr, String quantityStr) {
        Log.d(TAG, "validateInputs called");
        boolean isValid = true;

        // Clear previous errors
        clearErrors();

        // Validate item name
        if (itemName.isEmpty()) {
            Log.d(TAG, "Validation failed: Item name is empty");
            itemNameInput.setError("Item name is required");
            if (isValid) itemNameInput.requestFocus();
            isValid = false;
        } else if (itemName.length() < 2) {
            Log.d(TAG, "Validation failed: Item name too short");
            itemNameInput.setError("Item name must be at least 2 characters");
            if (isValid) itemNameInput.requestFocus();
            isValid = false;
        }

        // Validate weight
        if (weightStr.isEmpty()) {
            Log.d(TAG, "Validation failed: Weight is empty");
            weightInput.setError("Weight is required");
            if (isValid) weightInput.requestFocus();
            isValid = false;
        } else {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight <= 0) {
                    Log.d(TAG, "Validation failed: Weight <= 0");
                    weightInput.setError("Weight must be greater than 0");
                    if (isValid) weightInput.requestFocus();
                    isValid = false;
                } else if (weight > 10000) {
                    Log.d(TAG, "Validation failed: Weight too large");
                    weightInput.setError("Weight seems too large. Please check.");
                    if (isValid) weightInput.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                Log.d(TAG, "Validation failed: Weight not a valid number");
                weightInput.setError("Please enter a valid number");
                if (isValid) weightInput.requestFocus();
                isValid = false;
            }
        }

        // Validate quantity
        if (quantityStr.isEmpty()) {
            Log.d(TAG, "Validation failed: Quantity is empty");
            quantityInput.setError("Quantity is required");
            if (isValid) quantityInput.requestFocus();
            isValid = false;
        } else {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    Log.d(TAG, "Validation failed: Quantity < 0");
                    quantityInput.setError("Quantity cannot be negative");
                    if (isValid) quantityInput.requestFocus();
                    isValid = false;
                } else if (quantity > 100000) {
                    Log.d(TAG, "Validation failed: Quantity too large");
                    quantityInput.setError("Quantity seems too large. Please check.");
                    if (isValid) quantityInput.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                Log.d(TAG, "Validation failed: Quantity not a valid number");
                quantityInput.setError("Please enter a valid whole number");
                if (isValid) quantityInput.requestFocus();
                isValid = false;
            }
        }

        Log.d(TAG, "Validation result: " + isValid);

        // Show general error message if validation failed
        if (!isValid) {
            Toast.makeText(this, "Please fix the errors and try again.", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    /**
     * Clear all input field errors
     */
    private void clearErrors() {
        itemNameInput.setError(null);
        weightInput.setError(null);
        quantityInput.setError(null);
        notesInput.setError(null);
    }

    /**
     * Check if user has entered any data
     */
    private boolean hasUnsavedChanges() {
        String itemName = itemNameInput.getText().toString().trim();
        String weight = weightInput.getText().toString().trim();
        String quantity = quantityInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        return !itemName.isEmpty() || !weight.isEmpty() || !quantity.isEmpty() || !notes.isEmpty();
    }

    @Override
    public void onBackPressed() {
        // Handle back button same as cancel
        handleCancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AddItemActivity onDestroy");
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
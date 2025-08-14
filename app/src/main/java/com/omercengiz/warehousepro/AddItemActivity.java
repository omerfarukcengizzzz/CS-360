package com.omercengiz.warehousepro;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddItemActivity extends AppCompatActivity {

    private TextInputEditText itemNameInput;
    private TextInputEditText weightInput;
    private TextInputEditText quantityInput;
    private TextInputEditText notesInput;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        itemNameInput = findViewById(R.id.itemNameInput);
        weightInput = findViewById(R.id.weightInput);
        quantityInput = findViewById(R.id.quantityInput);
        notesInput = findViewById(R.id.notesInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to inventory screen
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void saveItem() {
        // Get input values
        String itemName = itemNameInput.getText().toString().trim();
        String weight = weightInput.getText().toString().trim();
        String quantityStr = quantityInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        // Validate inputs
        if (itemName.isEmpty()) {
            itemNameInput.setError("Item name is required");
            itemNameInput.requestFocus();
            return;
        }

        if (weight.isEmpty()) {
            weightInput.setError("Weight is required");
            weightInput.requestFocus();
            return;
        }

        if (quantityStr.isEmpty()) {
            quantityInput.setError("Quantity is required");
            quantityInput.requestFocus();
            return;
        }

        // Parse quantity
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                quantityInput.setError("Quantity cannot be negative");
                quantityInput.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            quantityInput.setError("Please enter a valid number");
            quantityInput.requestFocus();
            return;
        }

        // If notes is empty, set a default value
        if (notes.isEmpty()) {
            notes = "No notes";
        }

        // Add weight unit if not included
        if (!weight.contains("lb") && !weight.contains("kg") && !weight.contains("oz")) {
            weight = weight + " lbs"; // Default to pounds
        }

        // Save to database
        boolean success = databaseHelper.addItem(itemName, weight, quantity, notes);

        if (success) {
            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();

            // Check if quantity is 0 and notify
            if (quantity == 0) {
                Toast.makeText(this, "Warning: " + itemName + " has 0 quantity!",
                        Toast.LENGTH_LONG).show();
            }

            // Clear form for adding another item
            clearForm();

            // Or go back to inventory
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            Toast.makeText(this, "Failed to add item. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void clearForm() {
        itemNameInput.setText("");
        weightInput.setText("");
        quantityInput.setText("");
        notesInput.setText("");
        itemNameInput.requestFocus();
    }
}
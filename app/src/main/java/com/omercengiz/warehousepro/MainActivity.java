package com.omercengiz.warehousepro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private Button createAccountButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Set click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Create account button click listener
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCreateAccount();
            }
        });
    }

    /**
     * Handle login process with validation and database authentication
     */
    private void handleLogin() {
        // Get input values
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (!validateLoginInputs(username, password)) {
            return; // Validation failed, error message already shown
        }

        // Authenticate with database
        if (databaseHelper.authenticateUser(username, password)) {
            // Login successful
            Toast.makeText(MainActivity.this,
                    "Welcome back, " + username + "!",
                    Toast.LENGTH_SHORT).show();

            // Navigate to Inventory screen
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            intent.putExtra("USERNAME", username); // Pass username to next activity
            startActivity(intent);

            // Add smooth transition
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            // Clear input fields for security
            clearInputFields();

        } else {
            // Login failed
            Toast.makeText(MainActivity.this,
                    "Invalid username or password. Please try again.",
                    Toast.LENGTH_LONG).show();

            // Clear password field for security
            passwordInput.setText("");
            passwordInput.requestFocus();
        }
    }

    /**
     * Handle create account process - navigate to registration
     */
    private void handleCreateAccount() {
        // Create intent for registration activity (we'll create this next)
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);

        // Add fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Validate login inputs
     * @param username Username input
     * @param password Password input
     * @return true if valid, false otherwise
     */
    private boolean validateLoginInputs(String username, String password) {
        // Check if username is empty
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password is empty
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check minimum password length
        if (password.length() < 3) {
            passwordInput.setError("Password must be at least 3 characters");
            passwordInput.requestFocus();
            Toast.makeText(this, "Password is too short", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Clear any previous errors
        usernameInput.setError(null);
        passwordInput.setError(null);

        return true;
    }

    /**
     * Clear input fields for security
     */
    private void clearInputFields() {
        usernameInput.setText("");
        passwordInput.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear password field when returning to login screen
        passwordInput.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
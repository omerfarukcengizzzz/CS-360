package com.omercengiz.warehousepro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton createAccountButton;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize SharedPreferences for session management
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            // User is already logged in, go to inventory
            navigateToInventory();
            return;
        }

        // Initialize views
        initializeViews();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);
    }

    private void setClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });
    }

    private void performLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Check credentials against database
        if (databaseHelper.checkUser(username, password)) {
            // Login successful
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Save login session
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("username", username);
            editor.apply();

            // Navigate to inventory screen
            navigateToInventory();
        } else {
            // Login failed
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_LONG).show();
            passwordInput.setText(""); // Clear password field
        }
    }

    private void performRegistration() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Check if username is at least 3 characters
        if (username.length() < 3) {
            usernameInput.setError("Username must be at least 3 characters");
            usernameInput.requestFocus();
            return;
        }

        // Check if password is at least 4 characters
        if (password.length() < 4) {
            passwordInput.setError("Password must be at least 4 characters");
            passwordInput.requestFocus();
            return;
        }

        // Check if username already exists
        if (databaseHelper.checkUsername(username)) {
            usernameInput.setError("Username already exists");
            usernameInput.requestFocus();
            Toast.makeText(this, "Username already taken. Please choose another.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Register new user
        if (databaseHelper.addUser(username, password)) {
            Toast.makeText(this, "Account created successfully! Please login.",
                    Toast.LENGTH_LONG).show();

            // Clear the password field for security
            passwordInput.setText("");

            // Focus on login button
            loginButton.requestFocus();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToInventory() {
        Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
        startActivity(intent);
        finish(); // Prevent going back to login screen
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
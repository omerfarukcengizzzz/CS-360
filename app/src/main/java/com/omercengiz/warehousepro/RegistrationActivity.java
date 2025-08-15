package com.omercengiz.warehousepro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button createAccountButton;
    private Button cancelButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.regUsernameInput);
        emailInput = findViewById(R.id.regEmailInput);
        passwordInput = findViewById(R.id.regPasswordInput);
        confirmPasswordInput = findViewById(R.id.regConfirmPasswordInput);
        createAccountButton = findViewById(R.id.regCreateAccountButton);
        cancelButton = findViewById(R.id.regCancelButton);
    }

    private void setupClickListeners() {
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login screen
            }
        });
    }

    /**
     * Handle user registration process
     */
    private void handleRegistration() {
        // Get input values
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (!validateRegistrationInputs(username, email, password, confirmPassword)) {
            return; // Validation failed
        }

        // Check if username already exists
        if (databaseHelper.userExists(username)) {
            usernameInput.setError("Username already exists");
            usernameInput.requestFocus();
            Toast.makeText(this, "Username '" + username + "' is already taken", Toast.LENGTH_LONG).show();
            return;
        }

        // Create new user
        if (databaseHelper.createUser(username, password, email)) {
            // Registration successful
            Toast.makeText(this, "Account created successfully! Welcome, " + username + "!", Toast.LENGTH_LONG).show();

            // Navigate to SMS permissions screen
            Intent intent = new Intent(RegistrationActivity.this, NotificationActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("FROM_REGISTRATION", true);
            startActivity(intent);

            // Close registration activity
            finish();

        } else {
            // Registration failed
            Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validate registration inputs
     */
    private boolean validateRegistrationInputs(String username, String email, String password, String confirmPassword) {
        // Clear previous errors
        clearErrors();

        boolean isValid = true;

        // Validate username
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            if (isValid) usernameInput.requestFocus();
            isValid = false;
        } else if (username.length() < 3) {
            usernameInput.setError("Username must be at least 3 characters");
            if (isValid) usernameInput.requestFocus();
            isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameInput.setError("Username can only contain letters, numbers, and underscores");
            if (isValid) usernameInput.requestFocus();
            isValid = false;
        }

        // Validate email
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            if (isValid) emailInput.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email address");
            if (isValid) emailInput.requestFocus();
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            if (isValid) passwordInput.requestFocus();
            isValid = false;
        } else if (password.length() < 4) {
            passwordInput.setError("Password must be at least 4 characters");
            if (isValid) passwordInput.requestFocus();
            isValid = false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm your password");
            if (isValid) confirmPasswordInput.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            if (isValid) confirmPasswordInput.requestFocus();
            isValid = false;
        }

        // Show toast for first error found
        if (!isValid) {
            Toast.makeText(this, "Please fix the errors and try again", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    /**
     * Clear all input field errors
     */
    private void clearErrors() {
        usernameInput.setError(null);
        emailInput.setError(null);
        passwordInput.setError(null);
        confirmPasswordInput.setError(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Set click listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Please enter username and password",
                            Toast.LENGTH_SHORT).show();
                } else if (username.equals("admin") && password.equals("1234")) {
                    // Hardcoded credentials for testing
                    Toast.makeText(MainActivity.this,
                            "Login successful!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to Inventory screen
                    Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } else {
                    Toast.makeText(MainActivity.this,
                            "Invalid username or password",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Notification/SMS permissions screen
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
                // Add fade transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }
}
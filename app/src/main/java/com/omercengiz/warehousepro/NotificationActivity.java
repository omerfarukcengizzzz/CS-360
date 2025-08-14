package com.omercengiz.warehousepro;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private Button grantPermissionButton;
    private Button skipButton;
    private TextView permissionStatus;
    private SharedPreferences notificationPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialize preferences
        notificationPrefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        // Initialize views
        grantPermissionButton = findViewById(R.id.grantPermissionButton);
        skipButton = findViewById(R.id.skipButton);
        permissionStatus = findViewById(R.id.permissionStatus);

        // Check current permission status
        updatePermissionStatus();

        // Set click listeners
        grantPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSmsPermission();
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save preference that SMS is not enabled
                SharedPreferences.Editor editor = notificationPrefs.edit();
                editor.putBoolean("sms_enabled", false);
                editor.apply();

                Toast.makeText(NotificationActivity.this,
                        "SMS notifications disabled. You can enable them later in settings.",
                        Toast.LENGTH_LONG).show();

                finish();
            }
        });
    }

    private void updatePermissionStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            permissionStatus.setText("SMS Permission: Granted");
            permissionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            permissionStatus.setVisibility(View.VISIBLE);
            grantPermissionButton.setText("CONFIGURE SMS SETTINGS");

            // Change button behavior to configure phone number
            grantPermissionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPhoneNumberDialog();
                }
            });
        } else {
            permissionStatus.setText("SMS Permission: Not Granted");
            permissionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            permissionStatus.setVisibility(View.VISIBLE);
        }
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Show rationale if needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
                // Explain why we need the permission
                new AlertDialog.Builder(this)
                        .setTitle("SMS Permission Needed")
                        .setMessage("SMS permission is required to send you instant alerts when " +
                                "inventory items reach zero quantity. This helps prevent stockouts " +
                                "and ensures smooth warehouse operations.")
                        .setPositiveButton("Grant Permission", (dialog, which) -> {
                            ActivityCompat.requestPermissions(NotificationActivity.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    SMS_PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // Request the permission directly
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show();

                // Save preference
                SharedPreferences.Editor editor = notificationPrefs.edit();
                editor.putBoolean("sms_enabled", true);
                editor.apply();

                updatePermissionStatus();

                // Show phone number dialog
                showPhoneNumberDialog();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS Permission Denied. You won't receive inventory alerts.",
                        Toast.LENGTH_LONG).show();

                // Save preference
                SharedPreferences.Editor editor = notificationPrefs.edit();
                editor.putBoolean("sms_enabled", false);
                editor.apply();

                updatePermissionStatus();
            }
        }
    }

    private void showPhoneNumberDialog() {
        EditText phoneInput = new EditText(this);
        phoneInput.setHint("Enter phone number for alerts");
        phoneInput.setText(notificationPrefs.getString("phone_number", ""));

        new AlertDialog.Builder(this)
                .setTitle("Configure SMS Alerts")
                .setMessage("Enter the phone number where you want to receive inventory alerts:")
                .setView(phoneInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String phoneNumber = phoneInput.getText().toString().trim();
                    if (!phoneNumber.isEmpty()) {
                        // Save phone number
                        SharedPreferences.Editor editor = notificationPrefs.edit();
                        editor.putString("phone_number", phoneNumber);
                        editor.putBoolean("sms_enabled", true);
                        editor.apply();

                        Toast.makeText(this, "SMS alerts configured successfully!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Please enter a valid phone number",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
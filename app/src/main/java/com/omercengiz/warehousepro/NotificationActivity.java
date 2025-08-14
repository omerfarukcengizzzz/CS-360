package com.omercengiz.warehousepro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private Button grantPermissionButton;
    private Button skipButton;
    private TextView permissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

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
                // Navigate back or to main screen
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
            grantPermissionButton.setText("PERMISSION ALREADY GRANTED");
            grantPermissionButton.setEnabled(false);
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
                // User has previously denied permission
                Toast.makeText(this,
                        "SMS permission is needed to send inventory alerts when items reach zero quantity.",
                        Toast.LENGTH_LONG).show();
            }

            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS Permission Granted! You'll receive inventory alerts.",
                        Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS Permission Denied. You won't receive inventory alerts.",
                        Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            }
        }
    }
}
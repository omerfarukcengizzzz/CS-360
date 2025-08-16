package com.omercengiz.warehousepro;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

/**
 * SMSManager - Handles SMS sending functionality for inventory notifications
 * Manages SMS permissions and sending low stock alerts
 */
public class SMSManagerHelper {

    private static final String TAG = "SMSManagerHelper";
    private Context context;
    private DatabaseHelper databaseHelper;

    // Default notification settings - in production, these could be user configurable
    private static final String DEFAULT_PHONE_NUMBER = "1234567890"; // Replace with actual number
    private static final boolean SMS_ENABLED = true;

    public SMSManagerHelper(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    /**
     * Check if SMS permission is granted
     * @return true if permission granted, false otherwise
     */
    public boolean isSMSPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Send low stock notification SMS
     * @param item InventoryItem that reached zero quantity
     * @param phoneNumber Phone number to send SMS to
     * @return true if SMS sent successfully, false otherwise
     */
    public boolean sendLowStockAlert(InventoryItem item, String phoneNumber) {
        if (!SMS_ENABLED) {
            Log.d(TAG, "SMS notifications are disabled");
            return false;
        }

        if (!isSMSPermissionGranted()) {
            Log.w(TAG, "SMS permission not granted, cannot send notification");
            return false;
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.e(TAG, "Phone number is empty, cannot send SMS");
            return false;
        }

        try {
            // Create the SMS message
            String message = createLowStockMessage(item);

            Log.d(TAG, "Sending SMS to " + phoneNumber + ": " + message);

            // Get SMS manager and send message
            SmsManager smsManager = SmsManager.getDefault();

            // For long messages, divide into parts
            ArrayList<String> messageParts = smsManager.divideMessage(message);

            if (messageParts.size() == 1) {
                // Single SMS
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            } else {
                // Multiple SMS parts
                smsManager.sendMultipartTextMessage(phoneNumber, null, messageParts, null, null);
            }

            Log.d(TAG, "SMS sent successfully for item: " + item.getName());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send low stock alert using default phone number
     * @param item InventoryItem that reached zero quantity
     * @return true if SMS sent successfully, false otherwise
     */
    public boolean sendLowStockAlert(InventoryItem item) {
        return sendLowStockAlert(item, DEFAULT_PHONE_NUMBER);
    }

    /**
     * Create formatted SMS message for low stock alert
     * @param item InventoryItem that reached zero quantity
     * @return formatted SMS message
     */
    private String createLowStockMessage(InventoryItem item) {
        StringBuilder message = new StringBuilder();

        message.append("🚨 WAREHOUSE ALERT 🚨\n");
        message.append("ITEM OUT OF STOCK!\n\n");
        message.append("Item: ").append(item.getName()).append("\n");
        message.append("Quantity: 0\n");
        message.append("Weight: ").append(item.getFormattedWeight()).append("\n");

        if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
            message.append("Notes: ").append(item.getNotes()).append("\n");
        }

        message.append("\nAction Required: Reorder immediately\n");
        message.append("Time: ").append(getCurrentTimestamp());
        message.append("\n\n- Warehouse Pro System");

        return message.toString();
    }

    /**
     * Get current timestamp for SMS
     * @return formatted timestamp string
     */
    private String getCurrentTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm",
                java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    /**
     * Send multiple alerts for all zero quantity items
     * @param phoneNumber Phone number to send alerts to
     * @return number of successful SMS sent
     */
    public int sendAllZeroQuantityAlerts(String phoneNumber) {
        if (!isSMSPermissionGranted()) {
            Log.w(TAG, "SMS permission not granted for bulk alerts");
            return 0;
        }

        try {
            // Get all zero quantity items from database
            java.util.List<InventoryItem> zeroItems = databaseHelper.getZeroQuantityItems();

            if (zeroItems.isEmpty()) {
                Log.d(TAG, "No zero quantity items found");
                return 0;
            }

            int successCount = 0;

            for (InventoryItem item : zeroItems) {
                if (sendLowStockAlert(item, phoneNumber)) {
                    successCount++;

                    // Small delay between messages to avoid spam detection
                    try {
                        Thread.sleep(1000); // 1 second delay
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Sleep interrupted: " + e.getMessage());
                    }
                }
            }

            Log.d(TAG, "Sent " + successCount + " out of " + zeroItems.size() + " SMS alerts");
            return successCount;

        } catch (Exception e) {
            Log.e(TAG, "Error sending bulk SMS alerts: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Test SMS functionality with a simple test message
     * @param phoneNumber Phone number to send test to
     * @return true if test SMS sent successfully
     */
    public boolean sendTestSMS(String phoneNumber) {
        if (!isSMSPermissionGranted()) {
            Log.w(TAG, "SMS permission not granted for test");
            return false;
        }

        try {
            String testMessage = "📱 Warehouse Pro SMS Test\n\nThis is a test message to verify SMS functionality is working correctly.\n\nTime: " + getCurrentTimestamp();

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, testMessage, null, null);

            Log.d(TAG, "Test SMS sent successfully to " + phoneNumber);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to send test SMS: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validate phone number format
     * @param phoneNumber Phone number to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Remove common formatting characters
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-\\(\\)\\+]", "");

        // Check if it's all digits and reasonable length (7-15 digits)
        return cleanNumber.matches("\\d{7,15}");
    }

    /**
     * Format phone number for display
     * @param phoneNumber Raw phone number
     * @return formatted phone number
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";

        String cleanNumber = phoneNumber.replaceAll("[^\\d]", "");

        if (cleanNumber.length() == 10) {
            // US format: (123) 456-7890
            return String.format("(%s) %s-%s",
                    cleanNumber.substring(0, 3),
                    cleanNumber.substring(3, 6),
                    cleanNumber.substring(6));
        }

        return phoneNumber; // Return as-is if not standard format
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
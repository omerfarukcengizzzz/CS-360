package com.omercengiz.warehousepro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database constants
    private static final String DATABASE_NAME = "WarehousePro.db";
    private static final int DATABASE_VERSION = 3; //
    private static final String TAG = "DatabaseHelper";

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_CREATED_DATE = "created_date";

    // Inventory table
    private static final String TABLE_INVENTORY = "inventory";
    private static final String COLUMN_ITEM_ID = "item_id";
    private static final String COLUMN_ITEM_NAME = "item_name";
    private static final String COLUMN_ITEM_WEIGHT = "item_weight";
    private static final String COLUMN_ITEM_QUANTITY = "item_quantity";
    private static final String COLUMN_ITEM_NOTES = "item_notes";
    private static final String COLUMN_LAST_UPDATED = "last_updated";

    // Create tables SQL
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_CREATED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    private static final String CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + TABLE_INVENTORY + " (" +
                    COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                    COLUMN_ITEM_WEIGHT + " REAL NOT NULL, " +
                    COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_ITEM_NOTES + " TEXT, " +
                    COLUMN_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Creating database tables...");

            // Create tables
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_INVENTORY_TABLE);

            Log.d(TAG, "Tables created successfully");

            // Insert default admin user
            insertDefaultUser(db);

            // Insert sample inventory items for testing
            insertSampleData(db);

            Log.d(TAG, "Database setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop existing tables and create new ones
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);

        // Recreate tables
        onCreate(db);

        Log.d(TAG, "Database upgrade completed");
    }

    // Helper method to insert default admin user
    private void insertDefaultUser(SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, "admin");
            values.put(COLUMN_PASSWORD, hashPassword("1234"));
            values.put(COLUMN_EMAIL, "admin@warehousepro.com");

            long result = db.insert(TABLE_USERS, null, values);
            if (result != -1) {
                Log.d(TAG, "Default admin user created successfully");
            } else {
                Log.e(TAG, "Failed to create default admin user");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating default user: " + e.getMessage(), e);
        }
    }

    // Helper method to insert sample inventory data
    private void insertSampleData(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Inserting sample inventory data...");

            // Sample items
            String[][] sampleItems = {
                    {"Cardboard Boxes", "2.5", "50", "Standard shipping boxes"},
                    {"Bubble Wrap Roll", "1.2", "25", "Protective packaging material"},
                    {"Packing Tape", "0.8", "100", "Heavy duty sealing tape"},
                    {"Shipping Labels", "0.1", "500", "Adhesive shipping labels"},
                    {"Warehouse Trolley", "15.0", "5", "Heavy duty transport trolley"}
            };

            for (String[] item : sampleItems) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ITEM_NAME, item[0]);
                values.put(COLUMN_ITEM_WEIGHT, Double.parseDouble(item[1]));
                values.put(COLUMN_ITEM_QUANTITY, Integer.parseInt(item[2]));
                values.put(COLUMN_ITEM_NOTES, item[3]);

                long result = db.insert(TABLE_INVENTORY, null, values);
                if (result != -1) {
                    Log.d(TAG, "Sample item added: " + item[0]);
                }
            }

            Log.d(TAG, "Sample data insertion completed");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting sample data: " + e.getMessage(), e);
        }
    }

    // Password hashing for security
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password: " + e.getMessage());
            return password; // Fallback to plain text (not recommended for production)
        }
    }

    // ================== USER OPERATIONS ==================

    // Create new user account
    public boolean createUser(String username, String password, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, hashPassword(password));
            values.put(COLUMN_EMAIL, email);

            long result = db.insert(TABLE_USERS, null, values);

            if (result != -1) {
                Log.d(TAG, "User created successfully: " + username);
                return true;
            } else {
                Log.e(TAG, "Failed to create user: " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // Authenticate user login
    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String hashedPassword = hashPassword(password);
            String query = "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS +
                    " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{username, hashedPassword});

            boolean isAuthenticated = cursor.getCount() > 0;
            cursor.close();

            Log.d(TAG, "Authentication for " + username + ": " + isAuthenticated);
            return isAuthenticated;

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // Check if username already exists
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS +
                    " WHERE " + COLUMN_USERNAME + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{username});
            boolean exists = cursor.getCount() > 0;
            cursor.close();

            return exists;

        } catch (Exception e) {
            Log.e(TAG, "Error checking if user exists: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // ================== INVENTORY OPERATIONS ==================

    // Add new inventory item
    public boolean addInventoryItem(String name, double weight, int quantity, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Log.d(TAG, "Adding inventory item: " + name + ", Weight: " + weight + ", Quantity: " + quantity + ", Notes: " + notes);

            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_NAME, name);
            values.put(COLUMN_ITEM_WEIGHT, weight);
            values.put(COLUMN_ITEM_QUANTITY, quantity);
            values.put(COLUMN_ITEM_NOTES, notes);

            long result = db.insert(TABLE_INVENTORY, null, values);

            if (result != -1) {
                Log.d(TAG, "Inventory item added successfully: " + name + " with ID: " + result);
                return true;
            } else {
                Log.e(TAG, "Failed to add inventory item: " + name);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding inventory item: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // Get all inventory items
    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_INVENTORY + " ORDER BY " + COLUMN_ITEM_NAME;
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    InventoryItem item = new InventoryItem();
                    item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME)));
                    item.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ITEM_WEIGHT)));
                    item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY)));
                    item.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NOTES)));
                    item.setLastUpdated(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_UPDATED)));

                    items.add(item);
                } while (cursor.moveToNext());
            }

            cursor.close();
            Log.d(TAG, "Retrieved " + items.size() + " inventory items");

        } catch (Exception e) {
            Log.e(TAG, "Error getting inventory items: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return items;
    }

    // Update item quantity
    public boolean updateItemQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_QUANTITY, newQuantity);
            values.put(COLUMN_LAST_UPDATED, "datetime('now')");

            int rowsAffected = db.update(TABLE_INVENTORY, values,
                    COLUMN_ITEM_ID + " = ?",
                    new String[]{String.valueOf(itemId)});

            if (rowsAffected > 0) {
                Log.d(TAG, "Item quantity updated - ID: " + itemId + ", New Quantity: " + newQuantity);
                return true;
            } else {
                Log.e(TAG, "Failed to update item quantity - ID: " + itemId);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating item quantity: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // Delete inventory item
    public boolean deleteInventoryItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int rowsAffected = db.delete(TABLE_INVENTORY,
                    COLUMN_ITEM_ID + " = ?",
                    new String[]{String.valueOf(itemId)});

            if (rowsAffected > 0) {
                Log.d(TAG, "Inventory item deleted - ID: " + itemId);
                return true;
            } else {
                Log.e(TAG, "Failed to delete inventory item - ID: " + itemId);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting inventory item: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // Get items with zero quantity (for notifications)
    public List<InventoryItem> getZeroQuantityItems() {
        List<InventoryItem> zeroItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_INVENTORY +
                    " WHERE " + COLUMN_ITEM_QUANTITY + " = 0";

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    InventoryItem item = new InventoryItem();
                    item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME)));
                    item.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ITEM_WEIGHT)));
                    item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY)));
                    item.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NOTES)));

                    zeroItems.add(item);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error getting zero quantity items: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return zeroItems;
    }

    // Search inventory items by name
    public List<InventoryItem> searchInventoryItems(String searchQuery) {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_INVENTORY +
                    " WHERE " + COLUMN_ITEM_NAME + " LIKE ? ORDER BY " + COLUMN_ITEM_NAME;

            Cursor cursor = db.rawQuery(query, new String[]{"%" + searchQuery + "%"});

            if (cursor.moveToFirst()) {
                do {
                    InventoryItem item = new InventoryItem();
                    item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME)));
                    item.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ITEM_WEIGHT)));
                    item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY)));
                    item.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NOTES)));

                    items.add(item);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error searching inventory items: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return items;
    }
}
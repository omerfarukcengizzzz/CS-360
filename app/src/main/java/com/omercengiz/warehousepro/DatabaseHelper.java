package com.omercengiz.warehousepro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "WarehousePro.db";

    // User table name
    private static final String TABLE_USERS = "users";

    // User Table Columns names
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    // Inventory table name
    private static final String TABLE_INVENTORY = "inventory";

    // Inventory Table columns
    private static final String COL_ITEM_ID = "item_id";
    private static final String COL_ITEM_NAME = "item_name";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_NOTES = "notes";

    // Create table SQL query
    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_USERNAME + " TEXT UNIQUE,"
            + COL_PASSWORD + " TEXT" + ")";

    private String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + "("
            + COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_ITEM_NAME + " TEXT,"
            + COL_WEIGHT + " TEXT,"
            + COL_QUANTITY + " INTEGER,"
            + COL_NOTES + " TEXT" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);

        // Create tables again
        onCreate(db);
    }

    // Add new user
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        // Inserting Row
        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result != -1; // returns true if insert successful
    }

    // Check user credentials
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    // Check if username exists
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    // Add inventory item
    public boolean addItem(String itemName, String weight, int quantity, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_WEIGHT, weight);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_NOTES, notes);

        long result = db.insert(TABLE_INVENTORY, null, values);
        db.close();

        return result != -1;
    }

    // Get all inventory items
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_INVENTORY;
        return db.rawQuery(query, null);
    }

    // Update item quantity
    public boolean updateItemQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_QUANTITY, newQuantity);

        int result = db.update(TABLE_INVENTORY, values, COL_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)});
        db.close();

        return result > 0;
    }

    // Delete item
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_INVENTORY, COL_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)});
        db.close();

        return result > 0;
    }
}
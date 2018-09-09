/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the Inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ("
                + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT DEFAULT \"Unknown Supplier\", "
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER + " TEXT DEFAULT \"Unknown Phone\");";

        // To CREATE inventory table use:
        // CREATE TABLE inventory (_id INTEGER PRIMARY KEY AUTOINCREMENT, product_name TEXT NOT NULL, product_price INTEGER NOT NULL, product_quantity INTEGER NOT NULL DEFAULT 0, supplier_name TEXT DEFAULT "Unknown Supplier", supplier_phone_number TEXT DEFAULT "Unknown Phone");

        // To ADD to inventory table use:
        // INSERT INTO inventory (_id, product_name, product_price, product_quantity, supplier_name, supplier_phone_number) VALUES(1, "Silver Pen", 19.95, 1, "Microsoft", "(360)514-1234");

        // Execute the SQL statement
        Log.d(TAG, "" + SQL_CREATE_INVENTORY_TABLE);
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
} 
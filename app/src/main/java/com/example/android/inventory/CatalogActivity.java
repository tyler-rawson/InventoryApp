package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventory.data.InventoryContract;
import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Displays list of inventory that was entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int INVENTORY_LOADER = 0;
    private InventoryCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the inventory data
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        // Set up adapter to create list item for each row of inventory data in the cursor
        adapter = new InventoryCursorAdapter(this, null);

        // Set up OnItemClickListener to respond to the user's selected item
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // Form content uri that represents which item was selected.
                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });
        // Attach adapter to listView
        inventoryListView.setAdapter(adapter);

        // Initialize the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    private void insertItem() {
        // Create a ContentValues object where column names are the keys,
        // and Galaxy Smartwatch attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummy_data_product_name));
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, getString(R.string.dummy_data_product_price));
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.dummy_data_product_quantity));
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, getString(R.string.dummy_data_product_supplier_name));
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, getString(R.string.dummy_data_product_supplier_phone_number));

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to indicate that we want to insert
        // into the inventory database table.
        // Receive the new content URI that will allow us to access this data in the future.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all inventory in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                insertItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };


        // Perform a query on the provider using ContentResolver
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,             // The content URI
                projection,                       // The columns to return for each row
                null,                     // Selection criteria
                null,                 // Selection criteria
                null);                  // Sort order for returned columns
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update with this new cursor containing updated inventory data.
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when data needs to be deleted.
        adapter.swapCursor(null);
    }
}
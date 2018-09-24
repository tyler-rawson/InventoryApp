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
package com.example.android.inventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;
import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private boolean itemHasChanged = false;
    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mItemHasChanged boolean to true.

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    private Uri currentItemUri = null;
    /**
     * EditText field to enter the product name
     */
    private EditText editTextProductName;

    /**
     * EditText field to enter the product price
     */
    private EditText editTextProductPrice;

    /**
     * EditText field to enter the quantity.
     */
    private EditText editTextProductQuantity;

    /**
     * EditText field to enter the supplier name
     */
    private EditText editTextSupplierName;

    /**
     * EditText field to enter the supplier phone
     */
    private EditText editTextSupplierPhone;

    /**
     * Button to decrement the quantity
     */
    private Button buttonDecrement;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Find all relevant views that we will need to read user input from
        editTextProductName = (EditText) findViewById(R.id.edit_product_name);
        editTextProductPrice = (EditText) findViewById(R.id.edit_product_price);
        editTextProductQuantity = (EditText) findViewById(R.id.edit_text_product_quantity);
        editTextSupplierName = (EditText) findViewById(R.id.edit_text_supplier_name);
        editTextSupplierPhone = (EditText) findViewById(R.id.edit_text_supplier_phone);
        editTextSupplierPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        Button buttonIncrement = (Button) findViewById(R.id.buttonIncrement); // Button to increment the quantity
        buttonDecrement = (Button) findViewById(R.id.buttonDecrement);
        Button buttonDeleteItem = (Button) findViewById(R.id.buttonDeleteItem);// Button to delete the current item
        Button buttonOrderItems = (Button) findViewById(R.id.buttonOrderItems); // Button to place a phone call to order more items from supplier

        // Examine intent to see if we're going to be adding or editing an item.
        Intent intent = getIntent();
        currentItemUri = intent.getData();

        // If there's no URI stored in the currentItemUri then we must be adding an item. Otherwise, we're editing an item.
        if (currentItemUri == null) { // adding
            buttonDeleteItem.setVisibility(View.GONE);
            buttonOrderItems.setVisibility(View.GONE);
            setTitle(getString(R.string.editor_activity_title_new_item));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else { // editing
            buttonDeleteItem.setVisibility(View.VISIBLE);
            buttonOrderItems.setVisibility(View.VISIBLE);
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(CatalogActivity.INVENTORY_LOADER, null, this);
        }

        editTextProductName.setOnTouchListener(mTouchListener);
        editTextProductPrice.setOnTouchListener(mTouchListener);
        editTextProductQuantity.setOnTouchListener(mTouchListener);
        editTextSupplierName.setOnTouchListener(mTouchListener);
        editTextSupplierPhone.setOnTouchListener(mTouchListener);
        buttonIncrement.setOnTouchListener(mTouchListener);
        buttonDecrement.setOnTouchListener(mTouchListener);
    }

    /**
     * Get user input from editor and save new item into database.
     */
    private void saveItem() {
        if (currentItemUri == null) { // if the user is adding an item
            // Read from input fields
            // Use trim to eliminate leading or trailing white space
            String productNameString = editTextProductName.getText().toString().trim();
            String productPriceString = editTextProductPrice.getText().toString().trim();
            String productQuantityString = editTextProductQuantity.getText().toString().trim();
            String productSupplierName = editTextSupplierName.getText().toString().trim();
            String productSupplierPhone = editTextSupplierPhone.getText().toString().trim();

            if (currentItemUri == null &&
                    TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(productPriceString) &&
                    TextUtils.isEmpty(productQuantityString) && TextUtils.isEmpty(productSupplierName)
                    && TextUtils.isEmpty(productSupplierPhone)) {
                return;
            }

            // Create a ContentValues object where column names are the keys,
            // and item attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, productSupplierName);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, productSupplierPhone);

            double price = 0.0f;
            if (!TextUtils.isEmpty(productPriceString)) { // if user enters a price value > 0
                price = Double.parseDouble(productPriceString); // parse int and store as double price
            }
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

            // Insert a new item into the provider, returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else

        { // if the user is editing an item
            // Read from input fields
            // Use trim to eliminate leading or trailing white space
            String productNameString = editTextProductName.getText().toString().trim();
            String productPriceString = editTextProductPrice.getText().toString().trim();
            String productQuantityString = editTextProductQuantity.getText().toString().trim();
            String productSupplierName = editTextSupplierName.getText().toString().trim();
            String productSupplierPhone = editTextSupplierPhone.getText().toString().trim();

            if (currentItemUri == null &&
                    TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(productPriceString) &&
                    TextUtils.isEmpty(productQuantityString) && TextUtils.isEmpty(productSupplierName)
                    && TextUtils.isEmpty(productSupplierPhone)) {
                return;
            }

            // Create a ContentValues object where column names are the keys,
            // and item attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, productSupplierName);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, productSupplierPhone);

            double price = 0.0f;
            if (!TextUtils.isEmpty(productPriceString)) { // if user enters a price value > 0
                price = Double.parseDouble(productPriceString); // parse int and store as double price
            }
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

            // Save an existing item into the provider, returning the content URI for the new item.
            int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // save item to database via saveItem()
                saveItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int columnIndexProductName = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int columnIndexProductPrice = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int columnIndexProductQuantity = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int columnIndexSupplierName = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int columnIndexSupplierPhone = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(columnIndexProductName);
            double productPrice = cursor.getDouble(columnIndexProductPrice);
            int productQuantity = cursor.getInt(columnIndexProductQuantity);
            String supplierName = cursor.getString(columnIndexSupplierName);
            String supplierPhone = cursor.getString(columnIndexSupplierPhone);

            // Update the views on the screen with the values from the database
            editTextProductName.setText(productName);
            editTextProductPrice.setText(String.valueOf(productPrice));
            editTextProductQuantity.setText(String.valueOf(productQuantity));
            editTextSupplierName.setText(supplierName);
            editTextSupplierPhone.setText(supplierPhone);

            if (currentItemUri != null) { // editing an item because there is a uri.
                if (editTextProductQuantity.length() == 0 || editTextProductQuantity.getText() == null) {
                    //Do nothing
                } else {
                    int currentQuantity = Integer.parseInt(editTextProductQuantity.getText().toString());

                    if (currentQuantity > 0) { // check to see if the quantity is greater than zero before enabling the decrement button.
                        buttonDecrement.setEnabled(true);
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        editTextProductName.setText(null);
        editTextProductPrice.setText(null);
        editTextProductQuantity.setText(null);
        editTextSupplierName.setText(null);
        editTextSupplierPhone.setText(null);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (currentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the currentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }

    public void buttonIncrement(View view) {
        if (editTextProductQuantity.length() == 0 || editTextProductQuantity.getText() == null) { // If editTextProductQuantity is null
            editTextProductQuantity.setText(String.valueOf(0)); // set it to zero
        }
        buttonDecrement.setEnabled(true);
        int currentQuantity = Integer.parseInt(editTextProductQuantity.getText().toString());
        Log.d("EditorActivity", "buttonIncrement: current quantity:" + currentQuantity);
        int newQuantity = currentQuantity + 1;
        Log.d("EditorActivity", "buttonIncrement: newQuantity:" + newQuantity);
        editTextProductQuantity.setText(String.valueOf(newQuantity));
    }


    @SuppressWarnings("StatementWithEmptyBody")
    public void buttonDecrement(@SuppressWarnings("unused") View view) {
        if (editTextProductQuantity.length() == 0 || editTextProductQuantity.getText() == null) {
            // Do nothing if null or zero - we don't want the user to decrement below zero or attempt to decrement a null value.
        } else {
            int currentQuantity = Integer.parseInt(editTextProductQuantity.getText().toString());

            if (currentQuantity == 1) {
                buttonDecrement.setEnabled(false); // If quantity contains one item, enable buttonDecrement.
            }

            if (currentQuantity > 0) { // Ensure we won't go negative.
                int newQuantity = currentQuantity - 1; // Subtract from current quantity.
                editTextProductQuantity.setText(String.valueOf(newQuantity)); // Set the new value to the product quantity text field.
            } else {
                // This won't ever evaluate to true due to buttonDecrement being disabled when the value is 1 (above).
                // This line is only here to prevent errors in the future when editing the app.
                Toast.makeText(this, R.string.quantityBelowZero, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void callSupplier(@SuppressWarnings("unused") View view) {
        try {
            String number = editTextSupplierPhone.getText().toString().trim();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void deleteEntry(@SuppressWarnings("unused") View view) {
        showDeleteConfirmationDialog();
    }

}
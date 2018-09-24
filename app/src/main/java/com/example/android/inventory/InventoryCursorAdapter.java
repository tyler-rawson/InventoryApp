package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find the views
        final TextView textViewName = (TextView) view.findViewById(R.id.name);
        final TextView textViewPrice = (TextView) view.findViewById(R.id.price);
        final TextView textViewQuantity = (TextView) view.findViewById(R.id.quantity);
        Button buttonSellItem = (Button) view.findViewById(R.id.buttonSale);

        // Find the columns of item attributes we are interested in
        final int productID = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry._ID));
        int productNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the attributes from the cursor for the current item
        final String productName = cursor.getString(productNameColumnIndex);
        double productPrice = cursor.getDouble(productPriceColumnIndex);
        final int productQuantity = cursor.getInt(productQuantityColumnIndex);

        // Build the messages for setText method
        final String stringPrice = (context.getString(R.string.InventoryCursorAdapter_priceInUSD) + productPrice);
        String stringQuantity = (context.getString(R.string.InventoryCursorAdapter_quantity) + productQuantity);

        // Update the textViews with attributes from current item
        textViewName.setText(productName);
        textViewPrice.setText(stringPrice);
        textViewQuantity.setText(stringQuantity);

        if (productQuantity > 0) { // make sure the quantity is at least 1.
            buttonSellItem.setEnabled(true);
            buttonSellItem.setText(R.string.buttonSellItem);
            // Create onClickListener to monitor when sale button is pressed.
            buttonSellItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // when the sale button is pressed
                    int newQuantity = productQuantity - 1; // subtract 1 from currentQuantity
                    ContentValues values = new ContentValues(); // create contentValues object
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity); // store the new quantity in values object
                    Uri newUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, productID); // create newUri, pass in product id
                    context.getContentResolver().update(newUri, values, null, null); // update database with new quantity
                }
            });
        } else { // quantity is less than 0
            // When quantity is completely depleted...
            buttonSellItem.setEnabled(false); // disable the button
            buttonSellItem.setText(R.string.buttonNoStock); // set button text to "NO STOCK".
        }
    }
}
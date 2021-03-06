package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.MovieContract.MovieEntry;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Allows user to create a new movie or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PICK_IMAGE_REQUEST = 0;

    /** Identifier for the movie data loader */
    private static final int EXISTING_MOVIE_LOADER = 0;

    /** Content URI for the existing movie (null if it's a new movie) */
    private Uri mCurrentMovieUri;

    /** EditText field to enter the movie's name */
    private EditText mNameEditText;

    /** Variable to keep track of the movie's name */
    String mName;

    /** EditText field to enter the movie's price */
    private EditText mPriceEditText;

    /** Variable to keep track of the movie's price */
    float mPrice;

    /** TextView field to enter the movie's price */
    private TextView mQuantityTextView;

    /** Variable to keep track of the quantity */
    int mQuantity;

    /** EditText field to enter the movie's supplier */
    private EditText mSupplierEditText;

    /** Variable to keep track of the movie's supplier */
    String mSupplier;

    /** Uri for the image */
    private Uri mImageUri;

    /** Boolean flag that keeps track of whether the movie has been edited (true) or not (false) */
    private boolean mMovieHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mMovieHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMovieHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new movie or editing an existing one.
        Intent intent = getIntent();
        mCurrentMovieUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_movie_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_movie_price);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_available);
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier);
        Button imageButton = (Button) findViewById(R.id.button_get_photo);

        // If the intent DOES NOT contain a movie content URI, then we know that we are
        // creating a new movie.
        if (mCurrentMovieUri == null) {
            // This is a new movie, so change the app bar to say "Add a Movie"
            setTitle(getString(R.string.editor_activity_title_new_movie));

            mQuantity = 0;
            mQuantityTextView.setText(Integer.toString(mQuantity));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a movie that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing movie, so change app bar to say "Edit movie"
            setTitle(getString(R.string.editor_activity_title_edit_movie));

            // Initialize a loader to read the movie data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_MOVIE_LOADER, null, this);
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        imageButton.setOnTouchListener(mTouchListener);

        // Open the phone's gallery image selector when the button is clicked
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                openImageSelector();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            mImageUri = selectedImage;
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.image);

            Bitmap bmp = null;
            try {
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(bmp);
        }
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    /**
     * Get user input from editor and save movie into database.
     */
    private void saveMovie() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        Integer quantityInt = Integer.parseInt(mQuantityTextView.getText().toString().trim());
        String supplierString = mSupplierEditText.getText().toString().trim();

        // Check if this is supposed to be a new movie
        // and check if all the fields in the editor are blank
        if (TextUtils.isEmpty(nameString)) {
            // Since no fields were modified, we can return early without creating a new movie.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and movie attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_MOVIE_NAME, nameString);

        values.put(MovieEntry.COLUMN_MOVIE_QUANTITY, quantityInt);
        values.put(MovieEntry.COLUMN_MOVIE_SUPPLIER, supplierString);
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE_URL, mImageUri.toString());
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        Float price = 0.00f;
        if (!TextUtils.isEmpty(priceString)) {
            price = Float.parseFloat(priceString);
        }
        values.put(MovieEntry.COLUMN_MOVIE_PRICE, price);

        // Determine if this is a new or existing movie by checking if mCurrentMovieUri is null or not
        if (mCurrentMovieUri == null) {
            // This is a NEW movie, so insert a new movie into the provider,
            // returning the content URI for the new movie.
            Uri newUri = getContentResolver().insert(MovieEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_movie_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_movie_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING movie, so update the movie with content URI: mCurrentMovieUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentMovieUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentMovieUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_movie_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_movie_successful),
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

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new movie, hide the "Delete" menu item.
        if (mCurrentMovieUri == null) {
            MenuItem orderMenuItem = menu.findItem(R.id.action_order_more);
            orderMenuItem.setVisible(false);
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save movie to database
                saveMovie();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Order More" menu option
            case R.id.action_order_more:
                // Start email intent
                orderMore();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the movie hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mMovieHasChanged) {
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

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the movie hasn't changed, continue with handling back button press
        if (!mMovieHasChanged) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all movie attributes, define a projection that contains
        // all columns from the movie table
        String[] projection = {
                MovieEntry._ID,
                MovieEntry.COLUMN_MOVIE_NAME,
                MovieEntry.COLUMN_MOVIE_PRICE,
                MovieEntry.COLUMN_MOVIE_QUANTITY,
                MovieEntry.COLUMN_MOVIE_SUPPLIER,
                MovieEntry.COLUMN_MOVIE_IMAGE_URL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentMovieUri,         // Query the content URI for the current movie
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of movie attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_NAME);
            int priceColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_SUPPLIER);
            int imageUrlColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_IMAGE_URL);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String imageUrl = cursor.getString(imageUrlColumnIndex);

            mName = name;
            mPrice = price;
            mQuantity = quantity;
            mSupplier = supplier;
            mImageUri = Uri.parse(imageUrl);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Float.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);

            ImageView imageView = (ImageView) findViewById(R.id.image);

            Bitmap bmp = null;
            try {
                bmp = getBitmapFromUri(Uri.parse(imageUrl));
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(bmp);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("");
        mSupplierEditText.setText("");
        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(null);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
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
                // and continue editing the movie.
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
     * Prompt the user to confirm that they want to delete this movie.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the movie.
                deleteMovie();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the movie.
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
     * Open an email app to order more copies of the movie.
     */
    private void orderMore() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");

        String text = "Confirming purchase order of another copy of: " + mName;
        text += "\nName: " + mName;
        text += "\nPrice: $" + Float.toString(mPrice);
        text += "\nCurrent Quantity: " + mQuantity;
        text += "\nSupplier: " + mSupplier;
        intent.putExtra(Intent.EXTRA_SUBJECT, "Purchase Order of " + mName);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_STREAM, mImageUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Perform the deletion of the movie in the database.
     */
    private void deleteMovie() {
        // Only perform the delete if this is an existing movie.
        if (mCurrentMovieUri != null) {
            // Call the ContentResolver to delete the movie at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentMovieUri
            // content URI already identifies the movie that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentMovieUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_movie_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_movie_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    public void incrementQuantity(View view) {
        mQuantity++;
        mQuantityTextView.setText(Integer.toString(mQuantity));
    }

    public void decrementQuantity(View view) {
        if (mQuantity > 0) {
            mQuantity--;
            mQuantityTextView.setText(Integer.toString(mQuantity));
        }
    }
}

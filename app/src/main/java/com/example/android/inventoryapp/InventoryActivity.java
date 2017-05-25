package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
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

import com.example.android.inventoryapp.data.MovieContract.MovieEntry;

/**
 * Displays list of movies that were entered and stored in the app.
 */
public class InventoryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the movie data loader */
    private static final int MOVIE_LOADER = 0;

    /** Adapter for the ListView */
    MovieCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the movie data
        ListView movieListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_title_text);
        movieListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of movie data in the Cursor.
        // There is no movie data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new MovieCursorAdapter(this, null);
        movieListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific movie that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link MovieEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventoryapp/movies/2"
                // if the movies with ID 2 was clicked on.
                Uri currentMovieUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentMovieUri);

                // Launch the {@link EditorActivity} to display the data for the current movie.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    /**
     * Method to delete all movies in the database.
     */
    private void deleteAllMovies() {
        int rowsDeleted = getContentResolver().delete(MovieEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from movie database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_inventory.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        if (item.getItemId() == R.id.action_delete_all_entries) {
            // Respond to a click on the "Delete all entries" menu option
            deleteAllMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                MovieEntry._ID,
                MovieEntry.COLUMN_MOVIE_NAME,
                MovieEntry.COLUMN_MOVIE_PRICE,
                MovieEntry.COLUMN_MOVIE_QUANTITY,
                MovieEntry.COLUMN_MOVIE_IMAGE_URL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                MovieEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link MovieCursorAdapter} with this new cursor containing updated movie data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}

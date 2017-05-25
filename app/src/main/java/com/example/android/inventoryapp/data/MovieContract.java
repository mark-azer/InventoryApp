package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public final class MovieContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private MovieContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/movies/ is a valid path for
     * looking at movie data. content://com.example.android.inventory/shows/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "shows".
     */
    public static final String PATH_MOVIES = "movies";

    /**
     * Inner class that defines constant values for the movies database table.
     * Each entry in the table represents a single movie.
     */
    public static final class MovieEntry implements BaseColumns {

        /** The content URI to access the movie data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of movies.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single movie.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        /** Name of database table for movies */
        public final static String TABLE_NAME = "movies";

        /**
         * Unique ID number for the movie (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the movie.
         *
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_NAME ="name";

        /**
         * Price of the movie.
         *
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_PRICE = "price";

        /**
         * Quantity available of the movie.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_MOVIE_QUANTITY = "quantity";

        /**
         * Supplier of the movie.
         *
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_SUPPLIER = "supplier";

        /**
         * Image URL of the movie.
         *
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_IMAGE_URL = "image";
    }
}

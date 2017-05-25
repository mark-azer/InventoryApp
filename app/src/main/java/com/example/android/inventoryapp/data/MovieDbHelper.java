package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventoryapp.data.MovieContract.MovieEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "movies.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link MovieDbHelper}.
     *
     * @param context of the app
     */
    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the movies table
        String SQL_CREATE_MOVIES_TABLE =  "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MovieEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_MOVIE_PRICE + " REAL NOT NULL DEFAULT 0.00, "
                + MovieEntry.COLUMN_MOVIE_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + MovieEntry.COLUMN_MOVIE_SUPPLIER + " TEXT, "
                + MovieEntry.COLUMN_MOVIE_IMAGE_URL + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to be done here.
    }
}

package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.MovieContract;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * {@link MovieCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of movie data as its data source. This adapter knows
 * how to create list items for each row of movie data in the {@link Cursor}.
 */
public class MovieCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link MovieCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public MovieCursorAdapter(Context context, Cursor c) {
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
     * This method binds the movie data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current movie can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);

        // Find the columns of movie attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_PRICE);
        int imageUrlColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE_URL);

        // Read the movie attributes from the Cursor for the current movie
        String movieName = cursor.getString(nameColumnIndex);
        final int movieQuantity = cursor.getInt(quantityColumnIndex);
        Float moviePrice = cursor.getFloat(priceColumnIndex);
        String movieImage = cursor.getString(imageUrlColumnIndex);

        Bitmap bmp = null;
        try {
            bmp = getBitmapFromUri(context, Uri.parse(movieImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bmp);

        // Update the TextView with the attributes for the current movie
        nameTextView.setText(movieName);
        quantityTextView.setText("Quantity: " + String.valueOf(movieQuantity));
        priceTextView.setText("$" + Float.toString(moviePrice));
    }

    private Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}

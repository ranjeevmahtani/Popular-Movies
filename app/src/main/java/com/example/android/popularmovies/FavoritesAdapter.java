package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by ranjeevmahtani on 8/17/15.
 */
public class FavoritesAdapter extends CursorAdapter {

    public FavoritesAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.discovery_grid_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = (ImageView) view;

        int posterPath_index = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);

        String posterPath = cursor.getString(posterPath_index);

        String posterUrlStr = getPosterUrlStr(posterPath);

        Picasso.with(context).load(posterUrlStr).into(imageView);
    }

    public String getPosterUrlStr (String posterPath) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185");

        return builder.build().toString() + posterPath;
    }
}


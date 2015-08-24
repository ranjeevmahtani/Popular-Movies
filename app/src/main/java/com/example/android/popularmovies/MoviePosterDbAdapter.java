package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
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
public class MoviePosterDbAdapter extends CursorAdapter {

    public MoviePosterDbAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.discovery_grid_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = (ImageView) view;

        int posterOnDiskUrlStr_index = cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_POSTER_FILE_ON_DISK_URL);

        String posterOnDiskUrlStr = cursor.getString(posterOnDiskUrlStr_index);


        Picasso.with(context).load(posterOnDiskUrlStr).into(imageView);
    }
}


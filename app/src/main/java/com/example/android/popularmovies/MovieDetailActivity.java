package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class MovieDetailActivity extends ActionBarActivity {

    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {

            Intent intent = getIntent();
            Bundle arguments = new Bundle();
            if (intent != null && intent.hasExtra(MovieDetailFragment.MOVIE_PARCELABLE_KEY)) {
                // this intent was sent with a movie object. pass the movie object in the bundle
                Log.v(LOG_TAG, "getting mMovie from intent");
                Movie movie = intent.getParcelableExtra(MovieDetailFragment.MOVIE_PARCELABLE_KEY);
                arguments.putParcelable(MovieDetailFragment.MOVIE_PARCELABLE_KEY, movie);


            } else if (intent != null && intent.getData() !=null) {
                // this intent was sent with a movie Uri, and is therefore from the favorites table.
                // pass the movie Uri in the bundle.
                Uri movieUri = intent.getData();
                arguments.putParcelable(MovieDetailFragment.MOVIE_URI_KEY,movieUri);
            }

            MovieDetailFragment detailFragment = new MovieDetailFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, detailFragment)
                    .commit();
        }
    }

}
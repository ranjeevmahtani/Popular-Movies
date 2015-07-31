package com.example.android.popularmovies;

import android.content.Intent;
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
            if (intent != null && intent.hasExtra("movie")) {

                Log.v(LOG_TAG, "getting mMovie from intent");
                Movie movie = intent.getParcelableExtra("movie");

                Bundle arguments = new Bundle();
                arguments.putParcelable("movie", movie);

                MovieDetailFragment detailFragment = new MovieDetailFragment();
                detailFragment.setArguments(arguments);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_detail_container, detailFragment)
                        .commit();
            }
        }
    }
}
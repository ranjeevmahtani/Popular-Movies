package com.example.android.popularmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ranjeevmahtani on 8/14/15.
 */

public class FetchMoviesTask extends AsyncTask<String, Void, Integer> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private final int EVERYTHING_IS_GOOD_CODE = 100;
    private final int NO_INTERNET_CONNECTION_CODE = 200;
    private final int NO_MOVIES_RETURNED_FROM_SERVER_CODE = 300;
    private final int JSON_ERROR_CODE = 400;

    private Context mContext;
    private MoviePosterAdapter mMoviePosterAdapter;
    private ArrayList<Movie> mMovieArrayList;

    public FetchMoviesTask(Context context, MoviePosterAdapter moviePosterAdapter, ArrayList<Movie> movieArrayList) {
        mContext = context;
        mMoviePosterAdapter = moviePosterAdapter;
        mMovieArrayList = movieArrayList;
    }

    @Override
    protected Integer doInBackground(String... sortOption) {

        URL queryURL = Utility.getDiscoveryQueryUrl(mContext, sortOption[0]);

        if (Utility.isOnline(mContext)) {
            String moviesJsonStr = Utility.requestDataFromApi(queryURL);
            Movie[] movies;

            try {
                movies = Utility.getMovieArrayFromJsonStr(moviesJsonStr);

                if (movies != null && movies.length >0) {
                    mMovieArrayList.clear();
                    mMovieArrayList.addAll(Arrays.asList(movies));
                    return EVERYTHING_IS_GOOD_CODE;
                } else {
                    return NO_MOVIES_RETURNED_FROM_SERVER_CODE;
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return JSON_ERROR_CODE;
            }
        } else {
            return NO_INTERNET_CONNECTION_CODE;
        }
    }

    @Override
    protected void onPostExecute(Integer resultCode) {

        if (resultCode == EVERYTHING_IS_GOOD_CODE && mMovieArrayList != null) {
            mMoviePosterAdapter.clear();
            mMoviePosterAdapter.addAll(mMovieArrayList);
            mMoviePosterAdapter.notifyDataSetChanged();
        }
        else {
            final String NO_INTERNET_CONNECTION_MESSAGE = "Can you has interwebs?";
            final String NO_MOVIES_FOUND_MESSAGE = "Couldn't find any movies :(";
            final String JSON_ERROR_MESSAGE = "Server be sending us some bogusness...";
            final String UNKNOWN_ERROR_MESSAGE = "Someone farted";
            Toast toast = Toast.makeText(mContext, UNKNOWN_ERROR_MESSAGE, Toast.LENGTH_SHORT);

            switch (resultCode) {
                case NO_INTERNET_CONNECTION_CODE: {
                    toast.setText(NO_INTERNET_CONNECTION_MESSAGE);
                    break;
                }
                case JSON_ERROR_CODE: {
                    toast.setText(JSON_ERROR_MESSAGE);
                    break;
                }
                case NO_MOVIES_RETURNED_FROM_SERVER_CODE: {
                    toast.setText(NO_MOVIES_FOUND_MESSAGE);
                    break;
                }
            }
            toast.show();
        }
    }
}

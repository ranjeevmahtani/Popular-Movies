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

public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private Context mContext;

    private MoviePosterAdapter mMoviePosterAdapter;

    public FetchMoviesTask(Context context, MoviePosterAdapter moviePosterAdapter) {
        mContext = context;
        mMoviePosterAdapter = moviePosterAdapter;
    }

    @Override
    protected Movie[] doInBackground(String... sortOption) {

        // Log.v(LOG_TAG, "doing in background...");
        // Log.v(LOG_TAG, "Sort Option: " + sortOption[0]);

        URL queryURL = Utility.getDiscoveryQueryUrl(mContext, sortOption[0]);
        // Log.v(LOG_TAG, "queryURL:" + queryURL.toString());

        if (Utility.isOnline(mContext)) {
            String moviesJsonStr = Utility.requestDataFromApi(queryURL);
            Movie[] movies;

            try {
                movies = Utility.getMovieArrayFromJsonStr(moviesJsonStr);
                return movies;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
        } else {
            Log.d(LOG_TAG, "Internet connection not available.");
            return null;
        }
    }



    @Override
    protected void onPostExecute(Movie[] movies) {

        // Log.v(LOG_TAG, "entered onPostExecute");

        if (movies != null) {
            //Log.v(LOG_TAG, "movies != null");
            ArrayList<Movie> movieArrayList = new ArrayList<Movie>(Arrays.asList(movies));
            mMoviePosterAdapter.clear();
            mMoviePosterAdapter.addAll(movieArrayList);
            mMoviePosterAdapter.notifyDataSetChanged();

            // Log.v(LOG_TAG, "mMovieArrayList item 0: " + mMovieArrayList.get(0).getMovieTitle() + ", " + mMovieArrayList.get(0).getVideos());
            // Log.v(LOG_TAG, "mMoviePosterAdapter item 0: " + mMoviePosterAdapter.getItem(0).getMovieTitle() + ", " + mMoviePosterAdapter.getItem(0).getVideos());
        }
        else {
            String offlineMessage = "You aren't connected to the interwebs. Try again when you are connected.";
            Toast toast = Toast.makeText(mContext, offlineMessage, Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}

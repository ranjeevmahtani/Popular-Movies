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

public class FetchMoviesTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private Context mContext;
    private MoviePosterAdapter mMoviePosterAdapter;
    private ArrayList<Movie> mMovieArrayList;

    public FetchMoviesTask(Context context, MoviePosterAdapter moviePosterAdapter, ArrayList<Movie> movieArrayList) {
        mContext = context;
        mMoviePosterAdapter = moviePosterAdapter;
        mMovieArrayList = movieArrayList;
    }

    @Override
    protected Void doInBackground(String... sortOption) {

        URL queryURL = Utility.getDiscoveryQueryUrl(mContext, sortOption[0]);

        if (Utility.isOnline(mContext)) {
            String moviesJsonStr = Utility.requestDataFromApi(queryURL);
            Movie[] movies;

            try {
                movies = Utility.getMovieArrayFromJsonStr(moviesJsonStr);

                if (movies != null && movies.length >0) {
                    mMovieArrayList.clear();
                    mMovieArrayList.addAll(Arrays.asList(movies));
                }

                return null;
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
    protected void onPostExecute(Void result) {

        if (mMovieArrayList != null) {
            mMoviePosterAdapter.clear();
            mMoviePosterAdapter.addAll(mMovieArrayList);
            mMoviePosterAdapter.notifyDataSetChanged();
        }
        else {
            String offlineMessage = "You aren't connected to the interwebs. Try again when you are connected.";
            Toast toast = Toast.makeText(mContext, offlineMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}

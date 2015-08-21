package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by ranjeevmahtani on 8/14/15.
 */

public class FetchMoviesIntoDbTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMoviesIntoDbTask.class.getSimpleName();

    private Context mContext;

    public FetchMoviesIntoDbTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... sortOption) {

        // Log.v(LOG_TAG, "doing in background...");
        // Log.v(LOG_TAG, "Sort Option: " + sortOption[0]);

        URL queryURL = getDiscoveryQueryUrl(sortOption[0]);
        // Log.v(LOG_TAG, "queryURL:" + queryURL.toString());

        String moviesJsonStr = requestDataFromApi(queryURL);

        try {
            loadMoviesFromJsonStrToDb(moviesJsonStr);
            // saveMovieVideoInfo(movies);
            // Log.v(LOG_TAG, movies[0].getMovieTitle() + ", " + movies[0].getVideos());
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private URL getDiscoveryQueryUrl(String sortOption) {

        final String LOG_TAG = "getDiscoveryQueryUrl";

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter(mContext.getString(R.string.API_query_sort_by), sortOption)
                    .appendQueryParameter(mContext.getString(R.string.API_query_key), mContext.getString(R.string.API_param_key));

            //Log.v(LOG_TAG, builder.build().toString());

            return new URL(builder.build().toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }

    //Make API Call and read input
    //A lot of code here copied from Sunshine app
    private String requestDataFromApi(URL queryURL) {

        final String LOG_TAG = "requestDataFromApi()";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr;

        try {
            // Create the request to themovieDB, and open the connection
            urlConnection = (HttpURLConnection) queryURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            moviesJsonStr = buffer.toString();

            //Log.v(LOG_TAG, moviesJsonStr);

            return moviesJsonStr;

        } catch (IOException e) {

            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movies data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void loadMoviesFromJsonStrToDb(String moviesDataStr)
            throws JSONException {

        final int resultCount = 20; //20 results shown per page: initial setting

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_MOVIES_LIST = "results";
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_PLOT_SYNOPSIS = "overview";
        final String TMDB_USER_RATING = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";

        try {
            JSONObject moviesJsonResult = new JSONObject(moviesDataStr);
            JSONArray moviesJsonArray = moviesJsonResult.getJSONArray(TMDB_MOVIES_LIST);

            Vector<ContentValues> contentValuesVector = new Vector<ContentValues>();

            //for each movie in the JSON array, create a contentValues set and put the relevant values
            for (int i = 0; i < moviesJsonArray.length(); i++) {

                ContentValues movieContentValues = new ContentValues();

                // Get the JSON object representing the movie
                JSONObject movieJson = moviesJsonArray.getJSONObject(i);

                //Set movie details to the movie object
                movieContentValues.put(MovieContract.MovieEntry.COLUMN_TMDB_ID, movieJson.getInt(TMDB_MOVIE_ID));
                movieContentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movieJson.getString(TMDB_TITLE));
                movieContentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movieJson.getString(TMDB_POSTER_PATH));
                movieContentValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, movieJson.getString(TMDB_PLOT_SYNOPSIS));
                movieContentValues.put(MovieContract.MovieEntry.COLUMN_RATING, movieJson.getDouble(TMDB_USER_RATING));
                movieContentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movieJson.getString(TMDB_RELEASE_DATE));
                contentValuesVector.add(movieContentValues);
            }

            int inserted = 0;
            // add those movies to the DB
            if (contentValuesVector.size() > 0) {
                ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(contentValuesArray);
                inserted = mContext.getContentResolver().bulkInsert(
                        MovieContract.MovieEntry.CONTENT_URI,
                        contentValuesArray);
                Log.v(LOG_TAG, inserted + " movies inserted into the movies DB.");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

// this method commented out:
// helper method for saveMovieVideoIDs
//    public URL getVideoQueryURL(long movieId) {
//        final String LOG_TAG = "getVideoQueryUrl";
//
//        try {
//            Uri.Builder builder = new Uri.Builder();
//            builder.scheme("http")
//                    .authority("api.themoviedb.org")
//                    .appendPath("3")
//                    .appendPath("movie")
//                    .appendPath(String.valueOf(movieId))
//                    .appendPath("videos")
//                    .appendQueryParameter(mContext.getString(R.string.API_query_key), mContext.getString(R.string.API_param_key));
//
//            // Log.v(LOG_TAG, builder.build().toString());
//
//            return new URL(builder.build().toString());
//
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error ", e);
//            return null;
//        }
//    }

// this method commented out:
// unnecessary resource waste to run this for all movies while in discovery
//    public void saveMovieVideoIds(Movie[] movies) throws JSONException {
//        try {
//
//            if (movies !=null) {
//
//                final String TMDB_VIDEOS_LIST = "results";
//                final String TMDB_VIDEO_KEY = "key";
//                final String TMDB_VIDEO_NAME = "name";
//
//                for (Movie movie : movies) {
//
//                    long movieID = movie.getMovieID();
//                    URL videoQueryURL = getVideoQueryURL(movieID);
//                    String videosQueryResponseStr = requestDataFromApi(videoQueryURL);
//
//                    JSONObject videosQueryResponseJson = new JSONObject(videosQueryResponseStr);
//                    JSONArray videosJsonArray = videosQueryResponseJson.getJSONArray(TMDB_VIDEOS_LIST);
//
//                    for (int i = 0; i < videosJsonArray.length(); i++ ) {
//
//                        JSONObject video = videosJsonArray.getJSONObject(i);
//
//                        String[] videoInfo = {
//                                video.getString(TMDB_VIDEO_KEY),
//                                video.getString(TMDB_VIDEO_NAME)
//                        };
//
//                        movie.addVideo(videoInfo);
//
//                        // Log.v(LOG_TAG, movie.getMovieTitle());
//                        // Log.v(LOG_TAG, movie.getVideos());
//                    }
//                }
//            }
//
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, "Error ", e);
//        }
//    }
}

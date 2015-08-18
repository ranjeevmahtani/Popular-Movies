package com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

        URL queryURL = getDiscoveryQueryUrl(sortOption[0]);
        // Log.v(LOG_TAG, "queryURL:" + queryURL.toString());

        String moviesJsonStr = requestDataFromApi(queryURL);
        Movie[] movies;

        try {
            movies = getMovieArrayFromJsonStr(moviesJsonStr);
            // saveMovieVideoInfo(movies);
            // Log.v(LOG_TAG, movies[0].getMovieTitle() + ", " + movies[0].getVideos());
            return movies;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
            Log.v(LOG_TAG, "movies was null???");
        }
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

    private Movie[] getMovieArrayFromJsonStr(String moviesDataStr)
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

        JSONObject moviesJsonResult = new JSONObject(moviesDataStr);
        JSONArray moviesJsonArray = moviesJsonResult.getJSONArray(TMDB_MOVIES_LIST);


        //Create an array of movie objects to store relevant details from the JSON results
        Movie[] moviesObjectArray = new Movie[resultCount];


        //for each movie in the JSON array, create a Movie object and store the relevant details
        for (int i = 0; i < moviesJsonArray.length(); i++) {

            Movie movie = new Movie();

            // Get the JSON object representing the movie
            JSONObject movieJson = moviesJsonArray.getJSONObject(i);

            //Set movie details to the movie object
            movie.setMovieId(movieJson.getInt(TMDB_MOVIE_ID));
            movie.setMovieTitle(movieJson.getString(TMDB_TITLE));
            movie.setMoviePosterPath(movieJson.getString(TMDB_POSTER_PATH));
            movie.setMovieSynopsis(movieJson.getString(TMDB_PLOT_SYNOPSIS));
            movie.setMovieUserRating(movieJson.getDouble(TMDB_USER_RATING));
            movie.setMovieReleaseDate(movieJson.getString(TMDB_RELEASE_DATE));

            moviesObjectArray[i] = movie;

            //Log.v(LOG_TAG,"Movie " + i + ": " + moviesObjectArray[i].getMovieTitle());

        }

        return moviesObjectArray;
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

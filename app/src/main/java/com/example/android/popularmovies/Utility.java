package com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
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

/**
 * Created by ranjeevmahtani on 7/27/15.
 */
public class Utility {

    final static String LOG_TAG = Utility.class.getSimpleName();

    public static URL getVideoQueryURL(Context context, int movieId) {
        final String LOG_TAG = "getVideoQueryURL(...)";

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(String.valueOf(movieId))
                    .appendPath("videos")
                    .appendQueryParameter(context.getString(R.string.API_query_key), context.getString(R.string.API_param_key));

            // Log.v(LOG_TAG, builder.build().toString());

            return new URL(builder.build().toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }

    public static void saveMovieVideoInfo(Movie movie, URL videoQueryUrl) throws JSONException {
        final String LOG_TAG = "saveMovieVideoInfo(...)";

        try {
            final String TMDB_VIDEOS_LIST = "results";
            final String TMDB_VIDEO_KEY = "key";
            final String TMDB_VIDEO_NAME = "name";

            String videosQueryResponseStr = requestDataFromApi(videoQueryUrl);

            if (videosQueryResponseStr != null && !videosQueryResponseStr.equals("")) {

                JSONObject videosQueryResponseJson = new JSONObject(videosQueryResponseStr);
                JSONArray videosJsonArray = videosQueryResponseJson.getJSONArray(TMDB_VIDEOS_LIST);

                for (int i = 0; i < videosJsonArray.length(); i++) {

                    JSONObject video = videosJsonArray.getJSONObject(i);

                    String[] videoInfo = {
                            video.getString(TMDB_VIDEO_KEY),
                            video.getString(TMDB_VIDEO_NAME)
                    };

                    movie.addVideo(videoInfo);

                    // Log.v(LOG_TAG, movie.getMovieTitle());
                    // Log.v(LOG_TAG, movie.getVideos());
                }
            } else {
                Log.e(LOG_TAG, "No response from TMDB");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    public static String requestDataFromApi(URL queryURL) {

        final String LOG_TAG = "requestDataFromApi()";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

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

    public static Uri getVideoUri(String videoId) {
        final String YOUTUBE_BASE_URL = "http://youtube.com/watch?v=";
        return Uri.parse(YOUTUBE_BASE_URL + videoId);
    }


}

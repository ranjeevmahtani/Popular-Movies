package com.example.android.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by ranjeevmahtani on 7/27/15.
 */
public class Utility {

    final static String LOG_TAG = Utility.class.getSimpleName();

    public static URL getSearchQueryUrl(Context context, String query){
        try {
            String encodedQuery = URLEncoder.encode(query,"UTF-8");
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("search")
                    .appendPath("movie")
                    .appendQueryParameter("query", encodedQuery)
                    .appendQueryParameter(context.getString(R.string.API_key_query_param), context.getString(R.string.API_key));
            return new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG,e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG,e.getMessage());
        }
        return null;
    }

    public static URL getDiscoveryQueryUrl(Context context, String sortOption) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3");

            if (sortOption.equals(context.getString(R.string.API_param_now_playing))
                    || sortOption.equals(context.getString(R.string.API_param_upcoming))) {
                builder.appendPath("movie")
                        .appendPath(sortOption);
            } else {
                builder.appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter(context.getString(R.string.API_query_sort_by), sortOption)
                        .appendQueryParameter("vote_count.gte", "25"); // hard-coded minimum vote count
            }

            builder.appendQueryParameter(context.getString(R.string.API_key_query_param), context.getString(R.string.API_key));

            return new URL(builder.build().toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }

    public static URL getCastQueryUrl(Context context, long movieId) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(String.valueOf(movieId))
                    .appendPath("credits")
                    .appendQueryParameter(context.getString(R.string.API_key_query_param), context.getString(R.string.API_key));

            return new URL(builder.build().toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }


    public static URL getVideoQueryUrl(Context context, long movieId) {

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(String.valueOf(movieId))
                    .appendPath("videos")
                    .appendQueryParameter(context.getString(R.string.API_key_query_param), context.getString(R.string.API_key));

            return new URL(builder.build().toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }

    public static URL getReviewQueryUrl(Context context, long movieId) {
        final String LOG_TAG = "getReviewQueryUrl(...)";

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(String.valueOf(movieId))
                    .appendPath("reviews")
                    .appendQueryParameter(context.getString(R.string.API_key_query_param), context.getString(R.string.API_key));

            // Log.v(LOG_TAG, builder.build().toString());

            return new URL(builder.build().toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }

    public static Movie[] getMovieArrayFromJsonStr(String moviesDataStr) throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_MOVIES_LIST = "results";
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_PLOT_SYNOPSIS = "overview";
        final String TMDB_USER_RATING = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_VOTE_COUNT = "vote_count";

        JSONObject moviesJsonResult = new JSONObject(moviesDataStr);
        JSONArray moviesJsonArray = moviesJsonResult.getJSONArray(TMDB_MOVIES_LIST);

        //Create an array of movie objects to store relevant details from the JSON results
        Movie[] moviesObjectArray = new Movie[moviesJsonArray.length()];
        //for each movie in the JSON array, create a Movie object and store the relevant details
        for (int i = 0; i < moviesJsonArray.length(); i++) {
            Movie movie = new Movie();
            // Get the JSON object representing the movie
            JSONObject movieJson = moviesJsonArray.getJSONObject(i);
            //Set movie details to the movie object
            movie.setTmdbId(movieJson.getInt(TMDB_MOVIE_ID));
            movie.setMovieTitle(movieJson.getString(TMDB_TITLE));
            movie.setMoviePosterPath(movieJson.getString(TMDB_POSTER_PATH));
            movie.setMovieSynopsis(movieJson.getString(TMDB_PLOT_SYNOPSIS));
            movie.setMovieUserRating(movieJson.getDouble(TMDB_USER_RATING));
            movie.setMovieReleaseDate(movieJson.getString(TMDB_RELEASE_DATE));
            movie.setVoteCount(movieJson.getInt(TMDB_VOTE_COUNT));
            moviesObjectArray[i] = movie;
            //Log.v(LOG_TAG,"Movie " + i + ": " + moviesObjectArray[i].getMovieTitle());
        }
        return moviesObjectArray;
    }

    public static void saveMovieCastInfo(Movie movie, URL castQueryUrl) throws JSONException {
        try {
            final String TMDB_CAST_LIST = "cast";
            final String TMDB_CAST_NAME_KEY = "name";

            String castQueryResponseStr = requestDataFromApi(castQueryUrl);

            if (castQueryResponseStr != null && !castQueryResponseStr.equals("")) {

                JSONObject castQueryResponseJson = new JSONObject(castQueryResponseStr);
                JSONArray castJsonArray = castQueryResponseJson.getJSONArray(TMDB_CAST_LIST);

                if (castJsonArray.length() == 0) {
                    movie.setNoCast();
                } else {
                    int castArraySize = Math.min(3,castJsonArray.length());
                    String[] movieCast = new String[castArraySize];
                    for (int i = 0; i < castArraySize; i++) {
                        String castMember = castJsonArray.getJSONObject(i).getString(TMDB_CAST_NAME_KEY);
                        movieCast[i] = castMember;
                    }
                    movie.setCastArray(movieCast);
                }
            } else {
                movie.setNoCast();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
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

                if (videosJsonArray.length() == 0) {
                    movie.setNoVideos();
                } else {
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
                }
            } else {
                movie.setNoVideos();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    public static void saveMovieReviews(Movie movie, URL reviewsQueryUrl) throws JSONException {
        final String LOG_TAG = "saveMovieReviews(...)";

        try {
            final String TMDB_REVIEWS_LIST = "results";
            final String TMDB_REVIEW_AUTHOR = "author";
            final String TMDB_REVIEW_CONTENT = "content";

            String reviewsQueryResponseStr = requestDataFromApi(reviewsQueryUrl);

            if (reviewsQueryResponseStr != null && !reviewsQueryResponseStr.equals("")) {

                JSONObject reviewsQueryResponseJson = new JSONObject(reviewsQueryResponseStr);
                JSONArray reviewsJsonArray = reviewsQueryResponseJson.getJSONArray(TMDB_REVIEWS_LIST);

                if (reviewsJsonArray.length() == 0) {
                    movie.setNoReviews();
                } else {
                    for (int i = 0; i < reviewsJsonArray.length(); i++) {

                        JSONObject review = reviewsJsonArray.getJSONObject(i);

                        String reviewAuthor = review.getString(TMDB_REVIEW_AUTHOR);
                        if (reviewAuthor == null || reviewAuthor.equals("")) {
                            reviewAuthor = "anonymous";
                        }

                        String[] reviewInfo = {
                                reviewAuthor,
                                review.getString(TMDB_REVIEW_CONTENT)
                        };

                        movie.addReview(reviewInfo);

                        // Log.v(LOG_TAG, movie.getMovieTitle());
                        // Log.v(LOG_TAG, movie.getVideos());
                    }
                }
            } else {
                movie.setNoReviews();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    public static String requestDataFromApi(URL queryURL) {

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

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}

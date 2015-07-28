package com.example.android.popularmovies;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
 * A simple {@link Fragment} subclass.
 */
public class DiscoveryFragment extends Fragment {

    private final String LOG_TAG = DiscoveryFragment.class.getSimpleName();

    private MoviePosterAdapter mMoviePosterAdapter;

    private ArrayList<Movie> mMovieArrayList;

    public DiscoveryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);

        mMoviePosterAdapter =
                new MoviePosterAdapter(
                        getActivity(), // The current context (this activity)
                        new ArrayList<Movie>());

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Parcelable movie = mMoviePosterAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetail.class)
                        .putExtra("movie",movie);
                startActivity(intent);
            }
        };

        gridView.setOnItemClickListener(itemClickListener);

        gridView.setAdapter(mMoviePosterAdapter);
        //Log.v(LOG_TAG, "gridView.setAdapter(mMoviePosterAdapter) passed");

        if (savedInstanceState != null && savedInstanceState.containsKey("movieArray")){

            // Log.v(LOG_TAG, "savedInstanceState is not null, and it does contain a key called movieArray");
            // Log.v(LOG_TAG,"retrieving movies from saved movieArray");

            Parcelable[] movieArray = savedInstanceState.getParcelableArray("movieArray");
            mMovieArrayList = new ArrayList<Movie>();
            for (Parcelable movie : movieArray){
                mMovieArrayList.add((Movie) movie);
            }

            mMoviePosterAdapter.clear();
            mMoviePosterAdapter.addAll(mMovieArrayList);
            // Log.v(LOG_TAG, "mMovieArrayList has been added to mMoviePosterAdapter");
        } else {
            // Log.v(LOG_TAG, "savedInstance state is either null or does not contain a \"movieArray\"");
            // Log.v(LOG_TAG, "updating movies via API call");
            discoverByPopularity();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){

        if (mMovieArrayList !=null) {
            Parcelable[] movieArray = new Parcelable[mMovieArrayList.size()];

            for (int i = 0; i < mMovieArrayList.size(); i++){
                movieArray[i] = (mMovieArrayList.get(i));
            }
            outState.putParcelableArray("movieArray", movieArray);
        }

        super.onSaveInstanceState(outState);

        // Log.v(LOG_TAG, "instanceState saved");
        // Log.v(LOG_TAG, "outState.containsKey(\"movieArray\"): "
        //        + String.valueOf(outState.containsKey("movieArray")));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_discoveryfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_sort_by_popularity) {
            discoverByPopularity();
        }
        else if (id == R.id.action_sort_by_user_rating) {
            discoverByUserRating();
        }

        return super.onOptionsItemSelected(item);
    }


    // AsyncTask should:
    // get a list of movies and their image and detail URLs (doInBackground),
    // download the images (doInBackground),
    // and pass them to the adapterView (onPostExecute)
    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... sortOption) {

            // Log.v(LOG_TAG, "doing in background...");
            // Log.v(LOG_TAG, "Sort Option: " + sortOption[0]);

            URL queryURL = getDiscoveryQueryUrl(sortOption[0]);
            // Log.v(LOG_TAG, "queryURL:" + queryURL.toString());

            String moviesJsonStr = requestDataFromApi(queryURL);
            Movie[] movies = null;

            try {
                movies = getMovieArrayFromJsonStr(moviesJsonStr);
                // saveMovieVideoIds(movies);
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
                mMovieArrayList = new ArrayList<Movie>(Arrays.asList(movies));
                mMoviePosterAdapter.clear();
                mMoviePosterAdapter.addAll(mMovieArrayList);
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
                        .appendQueryParameter(getString(R.string.API_query_sort_by), sortOption)
                        .appendQueryParameter(getString(R.string.API_query_key), getString(R.string.API_param_key));

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

        public URL getVideoQueryURL(int movieId) {
            final String LOG_TAG = "getVideoQueryURL";

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(String.valueOf(movieId))
                        .appendPath("videos")
                        .appendQueryParameter(getString(R.string.API_query_key), getString(R.string.API_param_key));

                // Log.v(LOG_TAG, builder.build().toString());

                return new URL(builder.build().toString());

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
        }


        public void saveMovieVideoIds(Movie[] movies) throws JSONException {
            try {

                if (movies !=null) {

                    final String TMDB_VIDEOS_LIST = "results";
                    final String TMDB_VIDEO_KEY = "key";
                    final String TMDB_VIDEO_NAME = "name";

                    for (Movie movie : movies) {

                        int movieID = movie.getMovieID();
                        URL videoQueryURL = getVideoQueryURL(movieID);
                        String videosQueryResponseStr = requestDataFromApi(videoQueryURL);

                        JSONObject videosQueryResponseJson = new JSONObject(videosQueryResponseStr);
                        JSONArray videosJsonArray = videosQueryResponseJson.getJSONArray(TMDB_VIDEOS_LIST);

                        for (int i = 0; i < videosJsonArray.length(); i++ ) {

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
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
        }
    }

    public void discoverByUserRating(){
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute(getString(R.string.API_param_descending_rating));
    }

    public void discoverByPopularity() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute(getString(R.string.API_param_descending_popularity));
    }

    /* This was used back when movie discovery sort option was done as a sharedPrefs setting

    public void updateMovies(){
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = prefs.getString(getString(R.string.sort_by_key),
                getString(R.string.API_param_descending_popularity));
        moviesTask.execute(sortBy);
    }

    */
}


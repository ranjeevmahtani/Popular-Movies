package com.example.android.popularmovies;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONException;

import java.net.URL;


public class MainActivity extends AppCompatActivity implements DiscoveryFragment.Callback, FavoritesFragment.Callback {

    private static final String BROWSERFRAGMENT_TAG = "BFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mTwoPane = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        handleIntent(intent);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by adding or replacing the
            // detail fragment using a fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_discovery_container, new DiscoveryFragment(), BROWSERFRAGMENT_TAG)
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_discovery_container, new DiscoveryFragment(), BROWSERFRAGMENT_TAG)
                        .commit();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

//        if (id == R.id.action_search) {
//            startActivity(new Intent(this, SettingsActivity.class));
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent){
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMovieSearch(query);
        }
    }

    public void doMovieSearch(String query) {
        SearchMoviesTask searchTask = new SearchMoviesTask(this,query);
        searchTask.execute();


    }

    // Callback method to handle movie item selections in discovery fragment
    public void onItemSelected(Movie movie) {

        if (!mTwoPane) {

            Intent intent = new Intent(this, MovieDetailActivity.class)
                    .putExtra(MovieDetailFragment.MOVIE_PARCELABLE_KEY, movie);
            startActivity(intent);

        } else {

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.MOVIE_PARCELABLE_KEY, movie);

            MovieDetailFragment detailFragment = new MovieDetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
    }

    // Callback method to handle movie item selection in discoveryfragment2
    public void onItemSelected(Uri movieUri) {

        if (!mTwoPane) {

            Intent intent = new Intent(this, MovieDetailActivity.class)
                    .setData(movieUri);
            startActivity(intent);

        } else {

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.MOVIE_URI_KEY, movieUri);

            MovieDetailFragment detailFragment = new MovieDetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
    }

    // Callback method to handle showing of favorites
    public void viewFavorites() {

        if (!mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_discovery_container, new FavoritesFragment(), BROWSERFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_discovery_container, new FavoritesFragment(), BROWSERFRAGMENT_TAG)
                    .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // Callback method to handle discover command from favorites view
    public void discover(int discoveryCode){

        Bundle arguments = new Bundle();
        arguments.putInt(DiscoveryFragment.DISCOVERY_CODE_KEY, discoveryCode);

        DiscoveryFragment discoveryFragment = new DiscoveryFragment();
        discoveryFragment.setArguments(arguments);

        if (!mTwoPane) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.movie_discovery_container, discoveryFragment, BROWSERFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_discovery_container, discoveryFragment, BROWSERFRAGMENT_TAG)
                    .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public class SearchMoviesTask extends AsyncTask<String, Void, Movie[]>{
        private final String LOG_TAG = SearchMoviesTask.class.getSimpleName();

        private Context mContext;
        private String mSearchQuery;

        public SearchMoviesTask(Context context, String searchQuery){
            mContext = context;
            mSearchQuery = searchQuery;
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            URL queryURL;

            if (mSearchQuery != null && !mSearchQuery.equals("")) {
                queryURL = Utility.getSearchQueryUrl(mContext, mSearchQuery);
            } else {
                Log.d(LOG_TAG, "no search query provided");
                return null;
            }

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
        protected void onPostExecute(Movie[] movies){

            if (movies != null && movies.length >0) {
                Bundle arguments = new Bundle();
                arguments.putParcelableArray(DiscoveryFragment.BUNDLED_MOVIE_ARRAY_KEY,movies);
                DiscoveryFragment df = new DiscoveryFragment();
                df.setArguments(arguments);

                if (!mTwoPane) {
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.movie_discovery_container, df, BROWSERFRAGMENT_TAG)
                            .addToBackStack(null)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_discovery_container, df, BROWSERFRAGMENT_TAG)
                            .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                            .addToBackStack(null)
                            .commit();
                }
            } else {
                Toast toast = Toast.makeText(mContext, "Could not perform search", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}

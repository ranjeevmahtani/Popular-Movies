package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements DiscoveryFragment.Callback, DiscoveryFragment2.Callback {

    private static final String BROWSERFRAGMENT_TAG = "BFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mTwoPane = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
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
                    .replace(R.id.movie_discovery_container, new DiscoveryFragment2(), BROWSERFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_discovery_container, new DiscoveryFragment2(), BROWSERFRAGMENT_TAG)
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
}

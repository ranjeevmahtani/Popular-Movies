package com.example.android.popularmovies;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.popularmovies.data.MovieContract;


/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoveryFragment2 extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DiscoveryFragment2.class.getSimpleName();

    private MoviePosterDbAdapter mMoviePosterAdapter;

    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int DISCOVERY_LOADER = 0;

    private static final String[] DISCOVERY_COLUMNS = {
        //MovieContract.FavoritesEntry.TABLE_NAME + "." + MovieContract.FavoritesEntry._ID,
            MovieContract.FavoritesEntry._ID,
            MovieContract.FavoritesEntry.COLUMN_POSTER_PATH
    };

    private static final int MOVIE_ID_INDEX = 0;
    private static final int MOVIE_POSTER_PATH_INDEX = 1;

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri movieUri);
        void discover(int discoveryCode);
    }

    public DiscoveryFragment2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        if (id == R.id.action_discover_by_popularity) {
            ((Callback) getActivity()).discover(DiscoveryFragment.DISCOVER_BY_POPULARITY_CODE);
        }
        else if (id == R.id.action_discover_by_user_rating) {
            ((Callback) getActivity()).discover(DiscoveryFragment.DISCOVER_BY_USER_RATING_CODE);
        }

        else if (id == R.id.action_view_favorites) {
            viewFavorites();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){

            mPosition = savedInstanceState.getInt(SELECTED_KEY);

        } else {
            // Log.v(LOG_TAG, "savedInstance state is either null or does not contain a \"movieArray\"");
            // Log.v(LOG_TAG, "updating movies via API call");
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);

        mMoviePosterAdapter = new MoviePosterDbAdapter(getActivity(), null, 0);

        mGridView.setAdapter(mMoviePosterAdapter);

        // define the onItemClickListener and it's onItemClick method
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor !=null) {
                    // have the parent activity implement it's onItemSelected callback method
                    // passing it a Uri referencing the object from the table.
                    ((Callback) getActivity()).onItemSelected(
                            MovieContract.FavoritesEntry.buildFavoriteMovieUri(cursor.getLong(MOVIE_ID_INDEX)));
                }
                mPosition = position;
            }
        };

        // set the onItemClickListener on the GridView:
        mGridView.setOnItemClickListener(itemClickListener);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DISCOVERY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                MovieContract.FavoritesEntry.CONTENT_URI,
                DISCOVERY_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviePosterAdapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mMoviePosterAdapter.swapCursor(null);}

    @Override
    public void onSaveInstanceState(Bundle outState){

        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void viewFavorites() {
        //TODO: show dem favorites
    }
}


package com.example.android.popularmovies;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoveryFragment extends Fragment {

    private final String LOG_TAG = DiscoveryFragment.class.getSimpleName();

    public static final String DISCOVERY_CODE_KEY = "discoveryCode";
    public static final int DISCOVER_BY_POPULARITY_CODE = 100;
    public static final int DISCOVER_BY_USER_RATING_CODE = 101;

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
            discover(DISCOVER_BY_POPULARITY_CODE);
        }
        else if (id == R.id.action_discover_by_user_rating) {
            discover(DISCOVER_BY_USER_RATING_CODE);
        }

        else if (id == R.id.action_view_favorites) {
            ((Callback)getActivity()).viewFavorites();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);

        mMoviePosterAdapter =
                new MoviePosterAdapter(
                        getActivity(), // The current context (this activity)
                        new ArrayList<Movie>());

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Movie movie = (Movie) adapterView.getItemAtPosition(position);
                if (movie !=null) {
                    ((Callback) getActivity()).onItemSelected(movie);
                }
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
            Bundle arguments = getArguments();
            if (arguments != null && arguments.containsKey(DISCOVERY_CODE_KEY)) {
                discover(arguments.getInt(DISCOVERY_CODE_KEY));
            } else {
                discover(DISCOVER_BY_POPULARITY_CODE);
            }
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

    public void discover(int discoveryCode) {
        FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity(), mMoviePosterAdapter);

        switch (discoveryCode) {
            case DISCOVER_BY_POPULARITY_CODE: {
                moviesTask.execute(getString(R.string.API_param_descending_popularity));
                break;
            }
            case DISCOVER_BY_USER_RATING_CODE: {
                moviesTask.execute(getString(R.string.API_param_descending_rating));
                break;
            }
            default: {
                moviesTask.execute(getString(R.string.API_param_descending_popularity));
            }
        }
    }

    public interface Callback  {
        void onItemSelected(Movie movie);
        void viewFavorites();
    }
}


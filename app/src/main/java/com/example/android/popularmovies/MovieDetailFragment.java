package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private MovieVideosAdapter mMovieVideosAdapter;
    static String sPosterUrlStr;

    public MovieDetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("movie")){
            Movie movie = (intent.getParcelableExtra("movie"));

            ((TextView)(rootView.findViewById(R.id.movie_title))).setText(movie.getMovieTitle());

            ImageView imageView = (ImageView)(rootView.findViewById(R.id.movie_thumbnail));
            sPosterUrlStr = movie.getPosterURL();
            Picasso.with(getActivity()).load(sPosterUrlStr).into(imageView);

            ((TextView)(rootView.findViewById(R.id.movie_release_date))).setText(movie.getMovieReleaseDate());

            ((TextView)(rootView.findViewById(R.id.movie_rating)))
                    .setText
                            (String.valueOf(movie.getMovieUserRating()) + "/10");

            ((TextView)(rootView.findViewById(R.id.movie_synopsis))).setText(movie.getMovieSynopsis());

            mMovieVideosAdapter = new MovieVideosAdapter(getActivity(), new ArrayList<String[]>());

            AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    String videoId = mMovieVideosAdapter.getItem(position)[0];
                    Uri videoUri = Utility.getVideoUri(videoId);
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(videoUri);
                    startActivity(intent);
                }
            };

            ListView videoListView = (ListView)(rootView.findViewById(R.id.movie_videos_listview));

            videoListView.setAdapter(mMovieVideosAdapter);

            videoListView.setOnItemClickListener(itemClickListener);

            new FetchMovieVideoLinks().execute(movie);
        }

        return rootView;

    }

    public class FetchMovieVideoLinks extends AsyncTask<Movie, Void, ArrayList<String[]>> {

        private final String LOG_TAG = FetchMovieVideoLinks.class.getSimpleName();

        @Override
        protected ArrayList<String[]> doInBackground(Movie... params) {

            Movie movie = params[0];

            URL queryUrl = Utility.getVideoQueryURL(getActivity(), movie.getMovieID());

            try {
                Utility.saveMovieVideoIds(movie, queryUrl);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            return movie.getVideos();
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> videos) {
            // Log.v(LOG_TAG, "entered onPostExecute");
            mMovieVideosAdapter.clear();
            mMovieVideosAdapter.addAll(videos);
            mMovieVideosAdapter.notifyDataSetChanged();
        }
    }
}

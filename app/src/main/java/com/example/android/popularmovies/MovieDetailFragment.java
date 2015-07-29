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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static String sPosterUrlStr;

    private LinearLayout mLinearLayout;
    private ViewGroup mContainer;

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("movie")) {
            Movie movie = (intent.getParcelableExtra("movie"));

            mContainer = container;
            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
            mLinearLayout = (LinearLayout) rootView.findViewById(R.id.movie_detail_linear_layout);
            //mLinearLayout.setOnClickListener(this);

            // Run AsyncTask to query API for videoIDs, save Ids to movie object, return video URIs,
            // and populate views for videos into fragment.
            new FetchMovieVideoLinks().execute(movie);

            ((TextView) (rootView.findViewById(R.id.movie_title))).setText(movie.getMovieTitle());

            ImageView imageView = (ImageView) (rootView.findViewById(R.id.movie_thumbnail));
            sPosterUrlStr = movie.getPosterURL();
            Picasso.with(getActivity()).load(sPosterUrlStr).into(imageView);

            ((TextView) (rootView.findViewById(R.id.movie_release_date))).setText("Release Date: " + movie.getMovieReleaseDate());

            ((TextView) (rootView.findViewById(R.id.movie_rating)))
                    .setText
                            ("Viewer Rating: " + String.valueOf(movie.getMovieUserRating()) + "/10");

            ((TextView) (rootView.findViewById(R.id.movie_synopsis))).setText(movie.getMovieSynopsis());

            return rootView;
        }

        return null;
    }

    public class FetchMovieVideoLinks extends AsyncTask<Movie, Void, ArrayList<String[]>> {

        private final String LOG_TAG = FetchMovieVideoLinks.class.getSimpleName();

        @Override
        protected ArrayList<String[]> doInBackground(Movie... params) {

            Movie movie = params[0];

            URL queryUrl = Utility.getVideoQueryURL(getActivity(), movie.getMovieID());

            try {
                Utility.saveMovieVideoInfo(movie, queryUrl);
                //Log.v(LOG_TAG, movie.getVideos().get(0)[1] + ": " + movie.getVideos().get(0)[0]);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            return movie.getVideos();
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> videos) {
            // Log.v(LOG_TAG, "entered onPostExecute");

            if (videos != null && videos.size() > 0) {

                Log.v(LOG_TAG, "it's popcorn time!");

                for (String[] video : videos) {
                    mLinearLayout.addView(createVideoView(video));
                }
            }
        }
    }

    private View createVideoView(String[] video) {

        View videoView = LayoutInflater.from(getActivity()).inflate(R.layout.movie_videos_list_item, mContainer, false);

        ImageView videoIconBackground = (ImageView) videoView.findViewById(R.id.video_icon_background);
        TextView videoName = (TextView) videoView.findViewById(R.id.video_name);

        Picasso.with(getActivity()).load(sPosterUrlStr).into(videoIconBackground);
        videoName.setText(video[1]);
        videoView.setTag(video[0]);

        videoView.setOnClickListener(this);

        return videoView;
    }

    @Override
    public void onClick(View videoView) {
        String videoId = (String)videoView.getTag();
        Log.v(LOG_TAG, "item clicked: " + videoId);

        Uri videoUri = Utility.getVideoUri(videoId);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(videoUri);
        startActivity(intent);
    }
}

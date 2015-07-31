package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private Movie mMovie;

    private ShareActionProvider mShareActionProvider;
    private String mShareVideoUrlStr;

    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_movie_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareVideoUrlStr != null) {
            mShareActionProvider.setShareIntent(createShareVideoIntent());
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent createShareVideoIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareVideoUrlStr);
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(savedInstanceState == null || savedInstanceState.getParcelable("movie") == null) {

            Log.v(LOG_TAG, "savedInstanceState was null or did not contain a movie");

            Bundle arguments = getArguments();
            if(arguments!=null && arguments.getParcelable("movie") != null) {
                mMovie = arguments.getParcelable("movie");

            } else {
                Log.e(LOG_TAG, "No Movie object found when launching fragment");
                return null;
            }
        } else {

            Log.v(LOG_TAG, "savedInstanceState existed and contained a movie. Using that one as mMovie.");
            mMovie = savedInstanceState.getParcelable("movie");
        }

        mContainer = container;
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        mLinearLayout = (LinearLayout) rootView.findViewById(R.id.movie_detail_linear_layout);

        ((TextView) (rootView.findViewById(R.id.movie_title))).setText(mMovie.getMovieTitle());

        ImageView imageView = (ImageView) (rootView.findViewById(R.id.movie_thumbnail));
        sPosterUrlStr = mMovie.getPosterURL();
        Picasso.with(getActivity()).load(sPosterUrlStr).into(imageView);

        ((TextView) (rootView.findViewById(R.id.movie_release_date))).setText(
                "Release Date: " + mMovie.getMovieReleaseDate());

        ((TextView) (rootView.findViewById(R.id.movie_rating))).setText
                        ("Viewer Rating: " + String.valueOf(mMovie.getMovieUserRating()) + "/10");

        ((TextView) (rootView.findViewById(R.id.movie_synopsis))).setText(mMovie.getMovieSynopsis());

        // Run AsyncTask to query API for videos and reviews if the movie doesn't already have them,
        // and populate views for videos and reviews into fragment.

        if (!mMovie.hasVideos() || !mMovie.hasReviews()) {

            Log.v(LOG_TAG, "mMovie.hasVideos(): " + String.valueOf(mMovie.hasVideos()));
            Log.v(LOG_TAG, "mMovie.hasReviews(): " + String.valueOf(mMovie.hasReviews()));

            new FetchMovieVideosAndReviews().execute();

        } else {

            loadVideoViews(mMovie);
            loadReviewViews(mMovie);

        }
        return rootView;
    }

    public class FetchMovieVideosAndReviews extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMovieVideosAndReviews.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {

            if (!mMovie.hasVideos()) {
                URL videoQueryUrl = Utility.getVideoQueryUrl(getActivity(), mMovie.getMovieID());

                try {
                    //Log.v(LOG_TAG, "Attempting to query and save videos");
                    Utility.saveMovieVideoInfo(mMovie, videoQueryUrl);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            if (!mMovie.hasReviews()) {
                URL reviewQueryUrl = Utility.getReviewQueryUrl(getActivity(), mMovie.getMovieID());
                try {
                    //Log.v(LOG_TAG, "Attempting to query and save reviews");
                    Utility.saveMovieReviews(mMovie, reviewQueryUrl);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loadVideoViews(mMovie);
            loadReviewViews(mMovie);
        }
    }

    private void loadVideoViews(Movie movie) {
        mLinearLayout.addView(createSectionDivider("Trailers and Videos"));
        if (movie.hasVideos()) {
            ArrayList<String[]> videos = movie.getVideos();
            for (String[] video : videos) {
                mLinearLayout.addView(createVideoView(video));
            }
            mShareVideoUrlStr = (Utility.getVideoUri(videos.get(0)[0])).toString();
            mShareActionProvider.setShareIntent(createShareVideoIntent());
        } else {
            mLinearLayout.addView(createNoContentAvailableView("videos"));
        }
    }

    private void loadReviewViews(Movie movie) {
        mLinearLayout.addView(createSectionDivider("Reviews"));
        if (movie.hasReviews()) {
            ArrayList<String[]> reviews = movie.getReviews();
            for (String[] review : reviews) {
                mLinearLayout.addView(createReviewView(review));
            }
        } else {
            mLinearLayout.addView(createNoContentAvailableView("reviews"));
        }
    }

    private View createVideoView(String[] video) {

        View videoView = LayoutInflater.from(getActivity()).inflate(R.layout.movie_video_item, mContainer, false);

        ImageView videoIconBackground = (ImageView) videoView.findViewById(R.id.video_icon_background);
        TextView videoName = (TextView) videoView.findViewById(R.id.video_name);

        Picasso.with(getActivity()).load(sPosterUrlStr).into(videoIconBackground);
        videoName.setText(video[1]);
        videoView.setTag(video[0]);

        videoView.setOnClickListener(this);

        return videoView;
    }

    private View createReviewView(String[] review) {

        View reviewView = LayoutInflater.from(getActivity()).inflate(R.layout.movie_review_item, mContainer, false);

        TextView reviewAuthor = (TextView) reviewView.findViewById(R.id.review_author);
        TextView reviewContent = (TextView) reviewView.findViewById(R.id.review_content);

        reviewAuthor.setText(review[0]);
        reviewContent.setText(review[1]);

        return reviewView;
    }

    private View createSectionDivider(String sectionName) {

        View dividerView = LayoutInflater.from(getActivity()).inflate(R.layout.movie_detail_divider, mContainer, false);

        TextView sectionHeader = (TextView) dividerView.findViewById(R.id.section_header);
        sectionHeader.setText(sectionName);

        return dividerView;
    }

    private View createNoContentAvailableView(String desiredContent) {

        String message = "There are no " + desiredContent + " for this movie.";

        View noContentView = LayoutInflater.from(getActivity()).inflate(R.layout.no_content_view, mContainer, false);

        TextView textView = (TextView)noContentView.findViewById(R.id.no_content_textview);

        textView.setText(message);

        return noContentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mMovie != null) {
            outState.putParcelable("movie", mMovie);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View videoView) {
        String videoId = (String)videoView.getTag();
        // Log.v(LOG_TAG, "item clicked: " + videoId);

        Uri videoUri = Utility.getVideoUri(videoId);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(videoUri);
        startActivity(intent);
    }
}

package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.android.popularmovies.data.MovieContract;
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
    static final String MOVIE_URI_KEY = "movieURI";
    static final String MOVIE_PARCELABLE_KEY = "movieParcelableKey";

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

        if(savedInstanceState == null || savedInstanceState.getParcelable(MOVIE_PARCELABLE_KEY) == null) {

            Log.d(LOG_TAG, "savedInstanceState was null or did not contain a movie object");

            Bundle arguments = getArguments();
            if(arguments!=null && arguments.getParcelable(MOVIE_PARCELABLE_KEY) != null) {
                Log.d(LOG_TAG, "bundle contains a parceled movie object. easy");
                mMovie = arguments.getParcelable(MOVIE_PARCELABLE_KEY);

            } else if (arguments != null && arguments.getParcelable(MOVIE_URI_KEY) != null ) {
                Log.d(LOG_TAG, "bundle contained the URI for a favorited movie");
                Log.d(LOG_TAG, "creating a movie object from info obtained via db query");

                final String[] movieProjection = {
                        MovieContract.FavoritesEntry.COLUMN_TMDB_ID,
                        MovieContract.FavoritesEntry.COLUMN_TITLE,
                        MovieContract.FavoritesEntry.COLUMN_PLOT_SYNOPSIS,
                        MovieContract.FavoritesEntry.COLUMN_POSTER_PATH,
                        MovieContract.FavoritesEntry.COLUMN_RATING,
                        MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE,
                };
                final int TMDB_ID_IDX = 0;
                final int TITLE_IDX = 1;
                final int SYNOPSIS_IDX = 2;
                final int POSTER_PATH_IDX = 3;
                final int RATING_IDX = 4;
                final int RELEASE_DATE_IDX = 5;

                Uri movieUri = arguments.getParcelable(MOVIE_URI_KEY);
                long favoriteMovie_id = MovieContract.FavoritesEntry.getMovieIdFromUri(movieUri);
                Cursor movieCursor = getActivity().getContentResolver().query(
                        MovieContract.FavoritesEntry.CONTENT_URI,
                        movieProjection,
                        MovieContract.FavoritesEntry._ID + "=?",
                        new String[]{String.valueOf(favoriteMovie_id)},
                        null);
                if (movieCursor.moveToFirst()) {
                    //create the movie object and set it's basic values
                    mMovie = new Movie();
                    mMovie.setMovieTitle(movieCursor.getString(TITLE_IDX));
                    mMovie.setMoviePosterPath(movieCursor.getString(POSTER_PATH_IDX));
                    mMovie.setMovieReleaseDate(movieCursor.getString(RELEASE_DATE_IDX));
                    mMovie.setMovieSynopsis(movieCursor.getString(SYNOPSIS_IDX));
                    mMovie.setTmdbId(movieCursor.getInt(TMDB_ID_IDX));
                    mMovie.setMovieUserRating(movieCursor.getDouble(RATING_IDX));

                    movieCursor.close();

                    // get videos and reviews from db if they exist and add them to the movie
                    final String[] videoProjection = new String[] {
                            MovieContract.VideoEntry.COLUMN_MOVIE_KEY,
                            MovieContract.VideoEntry.COLUMN_YOUTUBE_KEY,
                            MovieContract.VideoEntry.COLUMN_NAME
                    };
                    final int MOVIE_KEY_IDX = 0;
                    final int YOUTUBE_KEY_IDX = 1;
                    final int NAME_IDX = 2;

                    Cursor videosCursor = getActivity().getContentResolver().query(
                            MovieContract.VideoEntry.CONTENT_URI,
                            videoProjection,
                            MovieContract.VideoEntry.COLUMN_MOVIE_KEY +"=?",
                            new String[]{String.valueOf(mMovie.getTmdbId())},
                            null
                    );
                    if (videosCursor.moveToFirst()) {
                        Log.d(LOG_TAG, "found a video for this favorite movie in the db");
                        mMovie.addVideo(new String[]{
                                videosCursor.getString(YOUTUBE_KEY_IDX),
                                videosCursor.getString(NAME_IDX)
                        });
                        while (videosCursor.moveToNext()) {
                            mMovie.addVideo(new String[]{
                                    videosCursor.getString(YOUTUBE_KEY_IDX),
                                    videosCursor.getString(NAME_IDX)
                            });
                        }
                    } else {
                        Log.d(LOG_TAG, "did not find a video for this favorite movie in the db");
                    }
                    videosCursor.close();

                    final String[] reviewProjection = new String[] {
                            MovieContract.ReviewEntry.COLUMN_MOVIE_KEY,
                            MovieContract.ReviewEntry.COLUMN_AUTHOR,
                            MovieContract.ReviewEntry.COLUMN_CONTENT
                    };

                    final int AUTHOR_IDX = 1;
                    final int CONTENT_IDX = 2;

                    Cursor reviewsCursor = getActivity().getContentResolver().query(
                            MovieContract.ReviewEntry.CONTENT_URI,
                            reviewProjection,
                            MovieContract.ReviewEntry.COLUMN_MOVIE_KEY +"=?",
                            new String[]{String.valueOf(mMovie.getTmdbId())},
                            null
                    );
                    if (reviewsCursor.moveToFirst()) {
                        Log.d(LOG_TAG, "did not find a review for this favorite movie in the db");
                        mMovie.addReview(new String[]{
                                reviewsCursor.getString(AUTHOR_IDX),
                                reviewsCursor.getString(CONTENT_IDX)
                        });
                        while (reviewsCursor.moveToNext()) {
                            mMovie.addReview(new String[]{
                                    reviewsCursor.getString(AUTHOR_IDX),
                                    reviewsCursor.getString(CONTENT_IDX)
                            });
                        }
                    } else {
                        Log.d(LOG_TAG, "did not find a review for this favorite movie in the db");
                    }
                    reviewsCursor.close();

                } else {
                    Log.e(LOG_TAG, "Could not find this movie in the favorites table...");
                }
                Log.d(LOG_TAG, "After adding videos to mMovie from DB, mMovie.hasVideos(): " +
                        String.valueOf(mMovie.hasVideos()));
                Log.d(LOG_TAG, "After adding reviews to mMovie from DB, mMovie.hasReviews(): " +
                        String.valueOf(mMovie.hasReviews()));

            } else {
                Log.e(LOG_TAG, "No Movie object or Uri found when launching fragment");
                return null;
            }
        } else {

            Log.d(LOG_TAG, "savedInstanceState existed and contained a movie object. Using that one as mMovie.");
            mMovie = savedInstanceState.getParcelable(MOVIE_PARCELABLE_KEY);
        }

        mContainer = container;
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        mLinearLayout = (LinearLayout) rootView.findViewById(R.id.movie_detail_linear_layout);

        ((TextView) (rootView.findViewById(R.id.movie_title))).setText(mMovie.getMovieTitle());

        ToggleButton toggle = (ToggleButton) rootView.findViewById(R.id.favorite_toggle_button);

        Cursor cursor = getActivity().getContentResolver().query(
                MovieContract.FavoritesEntry.CONTENT_URI,
                new String[]{MovieContract.FavoritesEntry._ID},
                MovieContract.FavoritesEntry.COLUMN_TMDB_ID + "= ?",
                new String[]{String.valueOf(mMovie.getTmdbId())},
                null,
                null);

        if (cursor.moveToFirst()) {
            toggle.setChecked(true);
        } else {
            toggle.setChecked(false);
        }

        cursor.close();

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    mMovie.addToFavorites(getActivity());

                    Log.d(LOG_TAG, "Movie is marked as favorite: " + mMovie.isFavorite());

                    Cursor cursor = getActivity().getContentResolver().query(
                            MovieContract.FavoritesEntry.CONTENT_URI,
                            new String[]{MovieContract.FavoritesEntry.COLUMN_TITLE},
                            null,
                            null,
                            null);
                    String favoriteTitles = "";
                    if (cursor.moveToFirst()) {
                        favoriteTitles = cursor.getString(0);
                    }
                    while (cursor.moveToNext()) {
                        favoriteTitles += " " + cursor.getString(0);
                    }
                    cursor.close();
                    Log.d(LOG_TAG, "Favorite titles: " + favoriteTitles);

                } else {
                    mMovie.removeFromFavorites(getActivity());

                    Cursor cursor = getActivity().getContentResolver().query(
                            MovieContract.FavoritesEntry.CONTENT_URI,
                            new String[]{MovieContract.FavoritesEntry.COLUMN_TITLE},
                            null,
                            null,
                            null);
                    String favoriteTitles = "";
                    if (cursor.moveToFirst()) {
                        favoriteTitles = cursor.getString(0);
                    }
                    while (cursor.moveToNext()) {
                        favoriteTitles += " " + cursor.getString(0);
                    }
                    cursor.close();
                    Log.d(LOG_TAG, "Favorite titles: " + favoriteTitles);
                }
            }
        });

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
            if (mShareActionProvider != null) {
                mShareVideoUrlStr = (Utility.getVideoUri(videos.get(0)[0])).toString();
                mShareActionProvider.setShareIntent(createShareVideoIntent());
            }
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

    private View createNoContentAvailableView(String desiredContentType) {

        String message = "There are no " + desiredContentType + " for this movie.";

        View noContentView = LayoutInflater.from(getActivity()).inflate(R.layout.no_content_view, mContainer, false);

        TextView textView = (TextView)noContentView.findViewById(R.id.no_content_textview);

        textView.setText(message);

        return noContentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mMovie != null) {
            outState.putParcelable(MOVIE_PARCELABLE_KEY, mMovie);
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

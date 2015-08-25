package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;
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

    public static final String MOVIE_URI_KEY = "movieURI";
    public static final String MOVIE_PARCELABLE_KEY = "movieParcelableKey";

    private LinearLayout mLinearLayout;
    private ViewGroup mContainer;
    private Movie mMovie;
    private Drawable mPosterDrawable;
    private TextView mCastTextView;

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

        if(savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_PARCELABLE_KEY)) {

            Log.d(LOG_TAG, "savedInstanceState was null or did not contain a movie object");

            Bundle arguments = getArguments();

            if(arguments!=null && arguments.containsKey(MOVIE_PARCELABLE_KEY)) {
                Log.d(LOG_TAG, "bundle contains a parceled movie object. easy");
                mMovie = arguments.getParcelable(MOVIE_PARCELABLE_KEY);


                // query the favorites table to see if this is a favorite movie
                Cursor favoriteCursor = getActivity().getContentResolver().query(
                        MovieContract.FavoritesEntry.CONTENT_URI,
                        new String[]{MovieContract.FavoritesEntry.COLUMN_TMDB_ID},
                        MovieContract.FavoritesEntry.COLUMN_TMDB_ID + "=?",
                        new String[]{String.valueOf(mMovie.getTmdbId())},
                        null
                );

                //if it is
                if (favoriteCursor.moveToFirst()) {
                    //mark it so
                    mMovie.setIsFavorite(true);
                    setMovieObjectCastMembersFromDb();
                    setMovieObjectVideosFromDb();
                    setMovieObjectReviewsFromDb();
                } else {
                    mMovie.setIsFavorite(false);
                }
                favoriteCursor.close();


            } else if (arguments != null && arguments.containsKey(MOVIE_URI_KEY)) {

                Log.d(LOG_TAG, "bundle contained the URI for a favorited movie");
                Log.d(LOG_TAG, "creating a movie object from info obtained via db query");
                Uri movieUri = arguments.getParcelable(MOVIE_URI_KEY);

                createMovieObjectFromFavoritesTable(movieUri);

            } else {
                Log.v(LOG_TAG, "No Movie object or Uri found when launching fragment");
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

        if (mMovie.isFavorite()) {
            toggle.setChecked(true);
        } else {
            toggle.setChecked(false);
        }

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    // mMovie.addToFavorites(getActivity());
                    if (mPosterDrawable != null) {
                        mMovie.addToFavorites(getActivity(), mPosterDrawable);
                    }

                    // in case we weren't able to mark this movie as favorite
                    if (!mMovie.isFavorite()) {
                        buttonView.setChecked(false);
                        Toast toast = Toast.makeText(getActivity(), "Sorry, this movie could not be added to favorites at this time.", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(getActivity(), "Movie saved to Favorites", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else {
                    mMovie.removeFromFavorites(getActivity());
                    Toast toast = Toast.makeText(getActivity(), "Movie removed from Favorites", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        ImageView imageView = (ImageView) (rootView.findViewById(R.id.movie_thumbnail));
        Picasso.with(getActivity()).load(mMovie.getPosterURLStr()).into(imageView);
        mPosterDrawable = imageView.getDrawable();

        ((TextView) (rootView.findViewById(R.id.movie_release_date))).setText(
                "Release Date: " + mMovie.getMovieReleaseDate());

        ((TextView) (rootView.findViewById(R.id.movie_rating))).setText(
                "Viewer Rating: " + String.valueOf(mMovie.getMovieUserRating()) + "/10");

        ((TextView) (rootView.findViewById(R.id.vote_count))).setText(
                "Ratings: " + String.valueOf(mMovie.getVoteCount()));

        mCastTextView = ((TextView) (rootView.findViewById(R.id.cast)));

        ((TextView) (rootView.findViewById(R.id.movie_synopsis))).setText(mMovie.getMovieSynopsis());

        // Run AsyncTask to query API for videos and reviews if the movie doesn't already have them,
        // and populate views for videos and reviews into fragment.
        new FetchMovieCast_Videos_Reviews().execute();

        return rootView;
    }

    public class FetchMovieCast_Videos_Reviews extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMovieCast_Videos_Reviews.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {

            if (Utility.isOnline(getActivity())) {
                if (mMovie.getCastArray()==null || mMovie.getCastArray().length==0){
                    URL castQueryUrl = Utility.getCastQueryUrl(getActivity(),mMovie.getTmdbId());
                    try {
                        Utility.saveMovieCastInfo(mMovie, castQueryUrl);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
                if (!mMovie.hasVideos()) {
                    URL videoQueryUrl = Utility.getVideoQueryUrl(getActivity(), mMovie.getTmdbId());
                    try {
                        Utility.saveMovieVideoInfo(mMovie, videoQueryUrl);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }

                if (!mMovie.hasReviews()) {
                    URL reviewQueryUrl = Utility.getReviewQueryUrl(getActivity(), mMovie.getTmdbId());
                    try {
                        Utility.saveMovieReviews(mMovie, reviewQueryUrl);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            } else {
                Log.d(LOG_TAG, "Internet connection not available. Did not query for cast, videos and reviews");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            createCastTextView();
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

    private void createCastTextView(){
        if (mMovie.getCastArray() != null && mMovie.getCastArray().length>0){
            String castText = "Cast: " + mMovie.getCastArray()[0];
            for (int i = 1; i<mMovie.getCastArray().length; i++) {
                castText = castText + ", " + mMovie.getCastArray()[i];
            }
            mCastTextView.setText(castText);
        }
    }

    private View createVideoView(String[] video) {

        View videoView = LayoutInflater.from(getActivity()).inflate(R.layout.movie_video_item, mContainer, false);

        ImageView videoIconBackground = (ImageView) videoView.findViewById(R.id.video_icon_background);
        TextView videoName = (TextView) videoView.findViewById(R.id.video_name);

        Picasso.with(getActivity()).load(mMovie.getPosterURLStr()).into(videoIconBackground);
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

    public void setMovieObjectCastMembersFromDb() {
        // query the cast table to see if this movie has a cast associated with it
        Cursor castCursor = getActivity().getContentResolver().query(
                MovieContract.CastEntry.CONTENT_URI,
                null,
                MovieContract.CastEntry.COLUMN_MOVIE_KEY + "=?",
                new String[]{String.valueOf(mMovie.getTmdbId())},
                null
        );
        // if it does
        if (castCursor.moveToFirst()) {
            //add them all to the movie object
            String[] castMembers = new String[castCursor.getCount()];
            int i = 0;
            do {
                castMembers[i] = castCursor.getString(castCursor.getColumnIndex(MovieContract.CastEntry.COLUMN_CAST_MEMBER));
                i++;
            } while (castCursor.moveToNext());
            mMovie.setCastArray(castMembers);
        }
        castCursor.close();
    }

    public void setMovieObjectVideosFromDb(){
        // query the videos table to see if this favorite movie has any videos
        Cursor videosCursor = getActivity().getContentResolver().query(
                MovieContract.VideoEntry.CONTENT_URI,
                null,
                MovieContract.VideoEntry.COLUMN_MOVIE_KEY + "=?",
                new String[]{String.valueOf(mMovie.getTmdbId())},
                null
        );
        //if it does
        if (videosCursor.moveToFirst()) {
            // add them all to the movie object
            do {
                String[] video =
                        {videosCursor.getString(videosCursor.getColumnIndex(MovieContract.VideoEntry.COLUMN_YOUTUBE_KEY)),
                                videosCursor.getString(videosCursor.getColumnIndex(MovieContract.VideoEntry.COLUMN_NAME))
                        };
                mMovie.addVideo(video);

            } while (videosCursor.moveToNext());
        }
        videosCursor.close();
    }

    public void setMovieObjectReviewsFromDb(){
        //query the reviews table to see if this favorite movie has any reviews
        Cursor reviewsCursor = getActivity().getContentResolver().query(
                MovieContract.ReviewEntry.CONTENT_URI,
                null,
                MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=?",
                new String[]{String.valueOf(mMovie.getTmdbId())},
                null
        );

        // if it does
        if (reviewsCursor.moveToFirst()) {
            //add them all to the movie object
            do {
                String[] review =
                        {reviewsCursor.getString(reviewsCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR)),
                                reviewsCursor.getString(reviewsCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT))
                        };
                mMovie.addReview(review);
            } while (reviewsCursor.moveToNext());
        }
        reviewsCursor.close();
    }

    public void createMovieObjectFromFavoritesTable(Uri movieUri) {
        final String[] movieProjection = {
                MovieContract.FavoritesEntry.COLUMN_TMDB_ID,
                MovieContract.FavoritesEntry.COLUMN_TITLE,
                MovieContract.FavoritesEntry.COLUMN_PLOT_SYNOPSIS,
                MovieContract.FavoritesEntry.COLUMN_TMDB_POSTER_PATH,
                MovieContract.FavoritesEntry.COLUMN_RATING,
                MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE,
                MovieContract.FavoritesEntry.COLUMN_VOTE_COUNT,
                MovieContract.FavoritesEntry.COLUMN_POSTER_FILE_ON_DISK_URL,
        };
        final int TMDB_ID_IDX = 0;
        final int TITLE_IDX = 1;
        final int SYNOPSIS_IDX = 2;
        final int POSTER_PATH_IDX = 3;
        final int RATING_IDX = 4;
        final int RELEASE_DATE_IDX = 5;
        final int VOTE_COUNT_IDX = 6;
        final int POSTER_ON_DISK_URL_IDX = 7;

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
            mMovie.setVoteCount(movieCursor.getInt(VOTE_COUNT_IDX));
            mMovie.setPosterOnDiskUrlStr(movieCursor.getString(POSTER_ON_DISK_URL_IDX));
            mMovie.setIsFavorite(true);

            movieCursor.close();

            setMovieObjectCastMembersFromDb();
            setMovieObjectVideosFromDb();
            setMovieObjectReviewsFromDb();

        } else {
            Log.e(LOG_TAG, "Could not find this movie in the favorites table...");
        }
    }

}

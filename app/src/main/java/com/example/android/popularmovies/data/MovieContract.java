package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by ranjeevmahtani on 8/11/15.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVORITES = "favorites";

    public static final String PATH_VIDEOS = "videos";

    public static final String PATH_REVIEWS = "reviews";

    public static final String PATH_MOVIES = "movies";


    // Table to store favorite movies
    public static final class FavoritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        //Table name
        public static final String TABLE_NAME = "favorites";

        public static final String COLUMN_TMDB_ID = "id";

        public static final String COLUMN_TITLE = "original_title";

        public static final String COLUMN_TMDB_POSTER_PATH = "tmdb_poster_path";

        public static final String COLUMN_PLOT_SYNOPSIS = "overview";

        public static final String COLUMN_RATING = "vote_average";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_POSTER_FILE_ON_DISK_URL = "poster_file_on_disk_url";

        public static Uri buildFavoriteMovieUri(long id) {
            Log.d("MovieContract Class", "Uri buildFavoriteMovieUri(long id) = " + ContentUris.withAppendedId(CONTENT_URI, id));
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getMovieIdFromUri(Uri movieUri) {
            return Long.parseLong(movieUri.getPathSegments().get(1));
        }

    }

    // Table to store video info for favorite movies
    public static final class VideoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEOS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;

        //Table name
        public static final String TABLE_NAME = "videos";

        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_YOUTUBE_KEY = "key";

        public static final String COLUMN_NAME = "name";

        public static Uri buildVideoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // Table to store reviews for favorite movies
    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        //Table name
        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_CONTENT = "content";

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // Table to store movies being discovered
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        //Table name
        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_TMDB_ID = "id";

        public static final String COLUMN_TITLE = "original_title";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_PLOT_SYNOPSIS = "overview";

        public static final String COLUMN_RATING = "vote_average";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}

package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by ranjeevmahtani on 8/11/15.
 */
public class MovieProvider extends ContentProvider{

    // The URI Matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int VIDEOS = 200;
    static final int REVIEWS = 300;

    private static final SQLiteQueryBuilder sMovieVideoQueryBuilder;

    static {
        sMovieVideoQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //videos INNER JOIN movies ON videos.movie_key = movies._id
        sMovieVideoQueryBuilder.setTables(
                MovieContract.VideoEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.VideoEntry.TABLE_NAME +
                        "." + MovieContract.VideoEntry.COLUMN_MOVIE_KEY +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);
    }

    private static final SQLiteQueryBuilder sMovieReviewQueryBuilder;

    static {
        sMovieReviewQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //videos INNER JOIN movies ON videos.movie_key = movies._id
        sMovieReviewQueryBuilder.setTables(
                MovieContract.ReviewEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.ReviewEntry.TABLE_NAME +
                        "." + MovieContract.ReviewEntry.COLUMN_MOVIE_KEY +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority,MovieContract.PATH_VIDEOS, VIDEOS);
        matcher.addURI(authority,MovieContract.PATH_REVIEWS, REVIEWS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES: return MovieContract.MovieEntry.CONTENT_TYPE;
            case VIDEOS: return MovieContract.VideoEntry.CONTENT_TYPE;
            case REVIEWS: return MovieContract.ReviewEntry.CONTENT_TYPE;
            default: throw new UnsupportedOperationException("Unknown uri: "+ uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case VIDEOS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case REVIEWS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default: throw new UnsupportedOperationException("Unkown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case MOVIES: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME,null,contentValues);
                if (_id>0) returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else throw new android.database.SQLException("Failed to insert row into movies table");
                break;
            }

            case VIDEOS: {
                long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME,null,contentValues);
                if (_id>0) returnUri = MovieContract.VideoEntry.buildVideoUri(_id);
                else throw new android.database.SQLException("Failed to insert row into videos table");
                break;
            }

            case REVIEWS: {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME,null,contentValues);
                if (_id>0) returnUri = MovieContract.ReviewEntry.buildReviewUri(_id);
                else throw new android.database.SQLException("Failed to insert row into reviews table");
                break;
            }
            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if ( selection == null ) selection = "1";

        switch (match) {

            case MOVIES: {
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case VIDEOS: {
                rowsDeleted = db.delete(
                        MovieContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case REVIEWS: {
                rowsDeleted = db.delete(
                        MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch(match) {
            case MOVIES: {
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, contentValues, selection,
                        selectionArgs);
                break;
            }
            case VIDEOS: {
                rowsUpdated = db.update(MovieContract.VideoEntry.TABLE_NAME, contentValues, selection,
                        selectionArgs);
                break;
            }
            case REVIEWS: {
                rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME, contentValues, selection,
                        selectionArgs);
                break;
            }
            default: throw new UnsupportedOperationException("unknown uri: " + uri);
        }

        if (rowsUpdated !=0) getContext().getContentResolver().notifyChange(uri,null);

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}

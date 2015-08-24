package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ranjeevmahtani on 8/11/15.
 */
public class MovieDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
         final String SQL_CREATE_FAVORITE_MOVIES_TABLE =
                 "CREATE TABLE " + MovieContract.FavoritesEntry.TABLE_NAME + " (" +
                         MovieContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         MovieContract.FavoritesEntry.COLUMN_TMDB_ID + " INTEGER NOT NULL, " +
                         MovieContract.FavoritesEntry.COLUMN_TITLE + " STRING NOT NULL, " +
                         MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE + " STRING NOT NULL, " +
                         MovieContract.FavoritesEntry.COLUMN_RATING + " REAL NOT NULL, " +
                         MovieContract.FavoritesEntry.COLUMN_PLOT_SYNOPSIS + " STRING NOT NULL, " +
                         MovieContract.FavoritesEntry.COLUMN_TMDB_POSTER_PATH + " STRING NOT NULL," +
                         MovieContract.FavoritesEntry.COLUMN_POSTER_FILE_ON_DISK_URL + " STRING NOT NULL" +
                         ");";

        final String SQL_CREATE_VIDEOS_TABLE =
                "CREATE TABLE " + MovieContract.VideoEntry.TABLE_NAME + " (" +
                        MovieContract.VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.VideoEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                        MovieContract.VideoEntry.COLUMN_NAME + " STRING NOT NULL, " +
                        MovieContract.VideoEntry.COLUMN_YOUTUBE_KEY + " STRING NOT NULL, " +
                        " FOREIGN KEY (" + MovieContract.VideoEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                        MovieContract.FavoritesEntry.TABLE_NAME + " (" + MovieContract.FavoritesEntry.COLUMN_TMDB_ID + ") " +
                        " );";

        final String SQL_CREATE_REVIEWS_TABLE =
                "CREATE TABLE " + MovieContract.ReviewEntry.TABLE_NAME + " (" +
                        MovieContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                        MovieContract.ReviewEntry.COLUMN_AUTHOR + " STRING NOT NULL, " +
                        MovieContract.ReviewEntry.COLUMN_CONTENT + " STRING NOT NULL, " +
                        " FOREIGN KEY (" + MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                        MovieContract.FavoritesEntry.TABLE_NAME + " (" + MovieContract.FavoritesEntry.COLUMN_TMDB_ID + ") " +
                        " );";

        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                        MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.MovieEntry.COLUMN_TMDB_ID + " INTEGER NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_TITLE + " STRING NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " STRING NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS + " STRING NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_POSTER_PATH + " STRING NOT NULL" +
                        ");";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        // TODO: Sort this out for offline data storage. Read up on "ALTER TABLE" to figure this out.

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.FavoritesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.VideoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}

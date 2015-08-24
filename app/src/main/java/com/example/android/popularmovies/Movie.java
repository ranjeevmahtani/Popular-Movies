package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by ranjeevmahtani on 7/10/15.
 */
public class Movie implements Parcelable{

    final String LOG_TAG = Movie.class.getSimpleName();

    private long tmdbId;
    private String title;
    private String tmdbPosterPath;
    private String mPosterOnDiskUrlStr;
    private String synopsis;
    private double userRating;
    private String releaseDate;

    private boolean hasVideos;
    private boolean hasReviews;

    private boolean isFavorite;

    private ArrayList<String[]> videos;
    private ArrayList<String[]> reviews;

    public Movie(){

        this.tmdbId = 0;
        this.title = "unavailable";
        this.tmdbPosterPath = "unavailable";
        this.synopsis = "unavailable";
        this.userRating = 0;
        this.releaseDate = "unavailable";
        this.videos = new ArrayList<String[]>();
        this.reviews = new ArrayList<String[]>();
        setNoVideos();
        setNoReviews();
        this.isFavorite = false;

    }

    public void setTmdbId(int tmdbId){
        this.tmdbId = tmdbId;
    }

    public long getMovieID(){
        return tmdbId;
    }

    public void setMovieTitle(String movieTitle){
        this.title = movieTitle;
    }

    public String getMovieTitle(){
        return title;
    }

    public void setMoviePosterPath(String posterPath){
        this.tmdbPosterPath = posterPath;
    }

    public String getMoviePosterPath(){
        return tmdbPosterPath;
    }

    public void setMovieSynopsis(String plotSynopsis){
        this.synopsis = plotSynopsis;
    }

    public String getMovieSynopsis(){
        return synopsis;
    }

    public void setMovieUserRating(double userRating){
        this.userRating = userRating;
    }

    public double getMovieUserRating(){
        return userRating;
    }

    public void setMovieReleaseDate(String movieReleaseDate){
        this.releaseDate = movieReleaseDate;
    }

    public long getTmdbId() {
        return tmdbId;
    }

    public String getMovieReleaseDate(){
        return releaseDate;
    }

    public boolean hasVideos() {
        return hasVideos;
    }

    public boolean hasReviews() {
        return hasReviews;
    }

    public boolean isFavorite() {return isFavorite;}

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getPosterOnDiskUrlStr() {
        return mPosterOnDiskUrlStr;
    }

    public void setPosterOnDiskUrlStr(String posterOnDiskUrlStr) {
        mPosterOnDiskUrlStr = posterOnDiskUrlStr;
    }

    public void addToFavorites(Context context, Drawable posterDrawable) {

        // First, check if a movie with this TMDB ID exists in the db
        Cursor movieCursor = context.getContentResolver().query(
                MovieContract.FavoritesEntry.CONTENT_URI,
                new String[]{MovieContract.FavoritesEntry._ID},
                MovieContract.FavoritesEntry.COLUMN_TMDB_ID + " = ?",
                new String[]{String.valueOf(this.tmdbId)},
                null);

        if (movieCursor.moveToFirst()) {
            this.isFavorite = true;
        } else {

            mPosterOnDiskUrlStr = savePosterToDisk(context, posterDrawable);
            Log.d(LOG_TAG, "mPosterOnDiskUrlStr = " + mPosterOnDiskUrlStr);


//            MyTarget target = new MyTarget(context, getTmdbId(), posterDrawable);
//            Picasso.with(context)
//                    .load(getPosterURLStr())
//                    .error(R.drawable.tough_android)
//                    .into(target);
//            target.savePosterToDisk();
//            mPosterOnDiskUrlStr = target.getPosterFileUrlStr();
//            Log.d(LOG_TAG, "mPosterOnDiskUrlStr = " + mPosterOnDiskUrlStr);

            if (mPosterOnDiskUrlStr != null) {
                ContentValues movieValues = new ContentValues();

                // Then add the data, along with the corresponding name of the data type,
                // so the content provider knows what kind of value is being inserted.
                movieValues.put(MovieContract.FavoritesEntry.COLUMN_TMDB_ID, tmdbId);
                movieValues.put(MovieContract.FavoritesEntry.COLUMN_TITLE, title);
                movieValues.put(MovieContract.FavoritesEntry.COLUMN_TMDB_POSTER_PATH, tmdbPosterPath);
                movieValues.put(MovieContract.FavoritesEntry.COLUMN_PLOT_SYNOPSIS, synopsis);
                movieValues.put(MovieContract.FavoritesEntry.COLUMN_RATING, userRating);
                movieValues.put(MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieContract.FavoritesEntry.COLUMN_POSTER_FILE_ON_DISK_URL, mPosterOnDiskUrlStr);

                // Finally, insert movie data into the database.
                context.getContentResolver().insert(
                        MovieContract.FavoritesEntry.CONTENT_URI,
                        movieValues
                );

                // add the videos to the videos table
                Vector<ContentValues> videoCvVector = new Vector<ContentValues>(videos.size());

                for (String[] video : videos) {
                    ContentValues videoCv = new ContentValues();
                    videoCv.put(MovieContract.VideoEntry.COLUMN_MOVIE_KEY, this.tmdbId);
                    videoCv.put(MovieContract.VideoEntry.COLUMN_YOUTUBE_KEY, video[0]);
                    videoCv.put(MovieContract.VideoEntry.COLUMN_NAME, video[1]);

                    videoCvVector.add(videoCv);
                }

                if (videoCvVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[videoCvVector.size()];
                    videoCvVector.toArray(cvArray);
                    int rowsInserted = context.getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI,cvArray);
                    Log.v(LOG_TAG, "rows inserted into videos table: " + rowsInserted);
                }

                // add the reviews to the reviews table
                Vector<ContentValues> reviewCvVector = new Vector<ContentValues>(reviews.size());

                for (String[] review : reviews) {
                    ContentValues reviewCv = new ContentValues();
                    reviewCv.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, this.tmdbId);
                    reviewCv.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review[0]);
                    reviewCv.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review[1]);

                    reviewCvVector.add(reviewCv);
                }

                if (reviewCvVector.size() > 0) {
                    ContentValues[] reviewCvArray = new ContentValues[reviewCvVector.size()];
                    reviewCvVector.toArray(reviewCvArray);
                    int rowsInserted = context.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI,reviewCvArray);
                    Log.v(LOG_TAG, "rows inserted into reviews table: " + rowsInserted);
                }

                this.isFavorite = true;

            } else {
                Log.e(LOG_TAG, "movie was not added to favorites");
            }
            movieCursor.close();
        }
    }

    public void removeFromFavorites(Context context) {

        Cursor cursor = context.getContentResolver().query(
                MovieContract.FavoritesEntry.CONTENT_URI,
                new String[]{MovieContract.FavoritesEntry.COLUMN_TMDB_ID},
                MovieContract.FavoritesEntry.COLUMN_TMDB_ID + "=?",
                new String[]{String.valueOf(this.tmdbId)},
                null);

        if (cursor.moveToFirst()) { //if the movie exists in the movies table
            //delete the movie from the movies table
            context.getContentResolver().delete(
                    MovieContract.FavoritesEntry.CONTENT_URI,
                    MovieContract.FavoritesEntry.COLUMN_TMDB_ID + "=?",
                    new String[]{String.valueOf(this.tmdbId)});

            String movieTmdbId = cursor.getString(0);

            //delete any saved videos for this movie from the videos table
            int videosDeletedCount = context.getContentResolver().delete(
                    MovieContract.VideoEntry.CONTENT_URI,
                    MovieContract.VideoEntry.COLUMN_MOVIE_KEY + "=?",
                    new String[]{movieTmdbId});

            Log.d(LOG_TAG, videosDeletedCount + " videos deleted for this movie");

            //delete any saved reviews for this movie from the videos table
            context.getContentResolver().delete(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=?",
                    new String[]{movieTmdbId});
        }

        // Delete the poster file saved on the disk.
        if (mPosterOnDiskUrlStr != null) {
            File posterFileOnDisk = new File(mPosterOnDiskUrlStr);
            posterFileOnDisk.delete();
        }

        this.isFavorite=false;
        this.mPosterOnDiskUrlStr = null;

        cursor.close();
    }

    public String getPosterURLStr(){

        if(mPosterOnDiskUrlStr != null) {
            return mPosterOnDiskUrlStr;

        } else {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("image.tmdb.org")
                    .appendPath("t")
                    .appendPath("p")
                    .appendPath("w185");

            return builder.build().toString() + tmdbPosterPath;
        }
    }

    /* Store information pertaining to videos related to this movie.
     * Each video is represented by a 2-element String array where:
     * the 1st element is the video Id and the 2nd element is the video name.
     * For each video, the Id corresponds to the video's youTube Id
     * the video's name is a simple descriptive name of the video
     * The String arrays representing relevant videos are stored in an ArrayList<String[]> called videos
     */
    public void addVideo(String[] videoIdNamePair) {
        if (videoIdNamePair != null && videoIdNamePair.length == 2){
            if (this.videos == null) {
                this.videos = new ArrayList<String[]>();
            }
            this.videos.add(videoIdNamePair);
            //if (!hasVideos()) {
                this.hasVideos = true;
            //}
        }
        else {
            Log.e(LOG_TAG, "videoNamePair array was not of length 2");
        }
    }

    public ArrayList<String[]> getVideos() {
        return videos;
    }

    /* Store information pertaining to reviews related to this movie.
     * Each review is represented by a 2-element String array where:
     * the 1st element is the review author and the 2nd element is the review content.
     * The String arrays representing relevant reviews are stored in an ArrayList<String[]> called reviews
     */
    public void addReview(String[] review) {
        if (review != null && review.length == 2){
            if (this.reviews == null) {
                this.reviews = new ArrayList<String[]>();
            }
            this.reviews.add(review);
            //if (!hasReviews()) {
                this.hasReviews = true;
            //}
        } else {
            Log.e(LOG_TAG, "did not supply a 2-element array for the review");
        }
    }

    public ArrayList<String[]> getReviews() {
        return reviews;
    }

    public void setNoVideos() {
        this.videos.clear();
        this.hasVideos = false;
    }

    public void setNoReviews() {
        this.reviews.clear();
        this.hasReviews = false;
    }

    public String savePosterToDisk(Context context, Drawable posterDrawable) {

        Bitmap posterBmp = ((BitmapDrawable)posterDrawable).getBitmap();

        if (posterBmp != null) {
            File directory = context.getFilesDir();
            String fileName = String.valueOf(this.tmdbId)+"_poster";
            File posterPngFile = new File(directory, fileName);
            OutputStream outStream = null;

            try {
                outStream = new BufferedOutputStream(new FileOutputStream(posterPngFile));
                posterBmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                return posterPngFile.toURI().toURL().toString();
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (outStream != null) {
                        outStream.close();
                    }
                } catch (java.io.IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(LOG_TAG, "Unable to save poster to disk.");
        }
        return null;
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeLong(tmdbId);
        out.writeString(title);
        out.writeString(tmdbPosterPath);
        out.writeString(mPosterOnDiskUrlStr);
        out.writeString(synopsis);
        out.writeDouble(userRating);
        out.writeString(releaseDate);
        out.writeByte((byte) (hasVideos ? 1 : 0));
        out.writeByte((byte) (hasReviews ? 1 : 0));
        out.writeByte((byte) (isFavorite? 1 : 0));
        if (videos == null) {
            out.writeByte((byte) (0));
        } else {
            out.writeByte((byte) (1));
            out.writeList(videos);
        }
        if (reviews == null) {
            out.writeByte((byte) (0));
        } else {
            out.writeByte((byte) (1));
            out.writeList(reviews);
        }
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size){
            return new Movie[size];
        }
    };

    private Movie(Parcel in){
        tmdbId = in.readLong();
        title = in.readString();
        tmdbPosterPath = in.readString();
        mPosterOnDiskUrlStr = in.readString();
        synopsis = in.readString();
        userRating = in.readDouble();
        releaseDate = in.readString();
        hasVideos = in.readByte() != 0;
        hasReviews = in.readByte() != 0;
        isFavorite = in.readByte() != 0;
        if (in.readByte() == 1) {
            videos = new ArrayList<String[]>();
            in.readList(videos, String[].class.getClassLoader());
        } else {
            videos = null;
        }
        if (in.readByte() == 1) {
            reviews = new ArrayList<String[]>();
            in.readList(reviews, String[].class.getClassLoader());
        } else {
            reviews = null;
        }
    }
}

//class MyTarget implements com.squareup.picasso.Target{
//
//    private final String LOG_TAG = MyTarget.class.getSimpleName();
//
//    Context mContext;
//    Long mTmdbId;
//    Drawable mPosterDrawable;
//    Bitmap mBitmap;
//    File mPosterPngFile;
//
//    public MyTarget(Context context, Long tmdbId, Drawable posterDrawable){
//        mContext = context;
//        mTmdbId = tmdbId;
//        mPosterDrawable = posterDrawable;
//    }
//
//    @Override
//    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
//        mBitmap = bitmap;
//    }
//
//    @Override
//    public void onBitmapFailed(Drawable drawable) {
//        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.tough_android);
//        Log.e(LOG_TAG, "Picasso failed to load the bitmap");
//    }
//
//    @Override
//    public void onPrepareLoad(Drawable drawable) {
//    }
//
//    public void savePosterToDisk() {
//
//        if (mBitmap != null) {
//            File directory = mContext.getFilesDir();
//            String fileName = String.valueOf(mTmdbId)+"_poster";
//            mPosterPngFile = new File(directory, fileName);
//            OutputStream outStream = null;
//
//            try {
//                outStream = new BufferedOutputStream(new FileOutputStream(mPosterPngFile));
//                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//            } catch (FileNotFoundException e) {
//                Log.e(LOG_TAG, e.getMessage());
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (outStream != null) {
//                        outStream.close();
//                    }
//                } catch (java.io.IOException e) {
//                    Log.e(LOG_TAG, e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            Log.e(LOG_TAG, "Unable to save poster to disk.");
//        }
//    }
//
//    // returns a String of the URL representing the poster PNG file saved on the disk
//    public String getPosterFileUrlStr() {
//
//        if (mPosterPngFile != null) {
//            String posterUrlStr = null;
//            try {
//                posterUrlStr =  mPosterPngFile.toURI().toURL().toString();
//            } catch (MalformedURLException e) {
//                Log.e(LOG_TAG, e.getMessage());
//                e.printStackTrace();
//            }
//            if (posterUrlStr != null){
//                return posterUrlStr;
//            } else {
//                Log.e(LOG_TAG, "posterUrlStr was null");
//                return null;
//            }
//        } else {
//            Log.e(LOG_TAG, "Unable to get Url for poster file on disk.");
//        }
//        return null;
//    }
//}
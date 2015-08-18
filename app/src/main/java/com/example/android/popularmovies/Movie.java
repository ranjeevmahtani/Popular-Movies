package com.example.android.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by ranjeevmahtani on 7/10/15.
 */
public class Movie implements Parcelable{

    final String LOG_TAG = Movie.class.getSimpleName();

    private long tmdbId;
    private String title;
    private String posterPath;
    private String synopsis;
    private double userRating;
    private String releaseDate;

    private boolean hasVideos;
    private boolean hasReviews;

    private boolean isFavorite;
    private long favoriteId;

    private ArrayList<String[]> videos;
    private ArrayList<String[]> reviews;

    public Movie(){

        this.tmdbId = 0;
        this.title = "unavailable";
        this.posterPath = "unavailable";
        this.synopsis = "unavailable";
        this.userRating = 0;
        this.releaseDate = "unavailable";
        this.videos = new ArrayList<String[]>();
        this.reviews = new ArrayList<String[]>();
        setNoVideos();
        setNoReviews();
        this.isFavorite = false;

    }

    public void setMovieId(int movieID){
        this.tmdbId = movieID;
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
        this.posterPath = posterPath;
    }

    public String getMoviePosterPath(){
        return posterPath;
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

    public void addToFavorites(Context context) {

        // First, check if the location with this city name exists in the db
        Cursor movieCursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID},
                MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                new String[]{String.valueOf(this.tmdbId)},
                null);

        if (movieCursor.moveToFirst()) {
            int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry._ID);
            this.favoriteId = movieCursor.getLong(movieIdIndex);
        } else {
            ContentValues movieValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            movieValues.put(MovieContract.MovieEntry.COLUMN_TMDB_ID, tmdbId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
            movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, synopsis);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, userRating);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

            // Finally, insert movie data into the database.
            Uri insertedUri = context.getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            // The resulting URI contains the ID for the row.  Extract the favoriteId from the Uri.
            this.favoriteId = ContentUris.parseId(insertedUri);

            Vector<ContentValues> videoCvVector = new Vector<ContentValues>(videos.size());

            for (String[] video : videos) {
                ContentValues videoCv = new ContentValues();
                videoCv.put(MovieContract.VideoEntry.COLUMN_MOVIE_KEY, favoriteId);
                videoCv.put(MovieContract.VideoEntry.COLUMN_YOUTUBE_KEY, video[0]);
                videoCv.put(MovieContract.VideoEntry.COLUMN_NAME, video[1]);

                videoCvVector.add(videoCv);
            }

            if (videoCvVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[videoCvVector.size()];
                videoCvVector.toArray(cvArray);
                 context.getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI,cvArray);
            }

            Vector<ContentValues> reviewCvVector = new Vector<ContentValues>(reviews.size());

            for (String[] review : reviews) {
                ContentValues reviewCv = new ContentValues();
                reviewCv.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, favoriteId);
                reviewCv.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review[0]);
                reviewCv.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review[1]);

                reviewCvVector.add(reviewCv);
            }

            if (reviewCvVector.size() > 0) {
                ContentValues[] reviewCvArray = new ContentValues[reviewCvVector.size()];
                reviewCvVector.toArray(reviewCvArray);
                context.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI,reviewCvArray);
            }
        }

        movieCursor.close();

        this.isFavorite = true;
    }

    public void removeFromFavorites(Context context) {

        Cursor cursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID},
                MovieContract.MovieEntry.COLUMN_TMDB_ID + "=?",
                new String[]{String.valueOf(this.tmdbId)},
                null);

        if (cursor.moveToFirst()) { //if the movie exists in the movies table
            //delete the movie from the movies table
            context.getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_TMDB_ID + "=?",
                    new String[]{String.valueOf(this.tmdbId)});

            this.isFavorite=false;

            String movieId = cursor.getString(0);

            //delete any saved videos for this movie from the videos table
            context.getContentResolver().delete(
                    MovieContract.VideoEntry.CONTENT_URI,
                    MovieContract.VideoEntry.COLUMN_MOVIE_KEY + "=?",
                    new String[]{movieId});

            //delete any saved reviews for this movie from the videos table
            context.getContentResolver().delete(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=?",
                    new String[]{movieId});
        }

        cursor.close();
    }

    public String getPosterURL(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185");

        return builder.build().toString() + posterPath;
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
            if (!hasVideos()) {
                this.hasVideos = true;
            }
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
            if (!hasReviews()) {
                this.hasReviews = true;
            }
        } else {
            Log.e(LOG_TAG, "did not supply a 2-element array for the review");
        }
    }

    public ArrayList<String[]> getReviews() {
        return reviews;
    }

    public void setNoVideos() {
        this.videos.clear();
        this.hasReviews = false;
    }

    public void setNoReviews() {
        this.reviews.clear();
        this.hasReviews = false;
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeLong(tmdbId);
        out.writeString(title);
        out.writeString(posterPath);
        out.writeString(synopsis);
        out.writeDouble(userRating);
        out.writeString(releaseDate);
        out.writeByte((byte) (hasVideos ? 1 : 0));
        out.writeByte((byte) (hasReviews ? 1 : 0));
        out.writeByte((byte) (isFavorite? 1 : 0));
        out.writeLong(favoriteId);
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
        posterPath = in.readString();
        synopsis = in.readString();
        userRating = in.readDouble();
        releaseDate = in.readString();
        hasVideos = in.readByte() != 0;
        hasReviews = in.readByte() != 0;
        isFavorite = in.readByte() != 0;
        favoriteId = in.readLong();
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

package com.example.android.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ranjeevmahtani on 7/10/15.
 */
public class Movie implements Parcelable{

    final String LOG_TAG = Movie.class.getSimpleName();

    private int id;
    private String title;
    private String posterPath;
    private String synopsis;
    private double userRating;
    private String releaseDate;

    private boolean hasVideos;
    private boolean hasReviews;

    private ArrayList<String[]> videos;
    private ArrayList<String[]> reviews;

    public Movie(){

        this.id = 0;
        this.title = "unavailable";
        this.posterPath = "unavailable";
        this.synopsis = "unavailable";
        this.userRating = 0;
        this.releaseDate = "unavailable";
        this.videos = new ArrayList<String[]>();
        this.reviews = new ArrayList<String[]>();
        this.hasVideos = false;
        this.hasReviews = false;

    }

    public void setMovieId(int movieID){
        this.id = movieID;
    }

    public int getMovieID(){
        return id;
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

    public String getMovieReleaseDate(){
        return releaseDate;
    }

    public boolean hasVideos() {
        return hasVideos;
    }

    public boolean hasReviews() {
        return hasReviews;
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
        out.writeInt(id);
        out.writeString(title);
        out.writeString(posterPath);
        out.writeString(synopsis);
        out.writeDouble(userRating);
        out.writeString(releaseDate);
        out.writeList(videos);
        out.writeList(reviews);
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
        id = in.readInt();
        title = in.readString();
        posterPath = in.readString();
        synopsis = in.readString();
        userRating = in.readDouble();
        releaseDate = in.readString();
        videos = (ArrayList<String[]>)in.readArrayList(null);
        reviews = (ArrayList<String[]>)in.readArrayList(null);
    }

}

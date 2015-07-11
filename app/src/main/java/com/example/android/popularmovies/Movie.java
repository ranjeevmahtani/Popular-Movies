package com.example.android.popularmovies;

import android.net.Uri;

/**
 * Created by ranjeevmahtani on 7/10/15.
 */
public class Movie {

    private int id;
    private String title;
    private String posterPath;
    private String synopsis;
    private double userRating;
    private String releaseDate;

    public Movie(){

        this.id = 0;
        this.title = "unavailable";
        this.posterPath = "unavailable";
        this.synopsis = "unavailable";
        this.userRating = 0;
        this.releaseDate = "unavailable";

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

    public String getPosterURL(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185")
                ;

        return builder.build().toString() + posterPath;
    }

}

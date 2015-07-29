//package com.example.android.popularmovies;
//
//import android.app.Activity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.squareup.picasso.Picasso;
//
//import java.util.List;
//
///**
// * Created by ranjeevmahtani on 7/27/15.
// */
//public class MovieVideosAdapter extends ArrayAdapter<String[]> {
//
//    private static final String LOG_TAG = MovieVideosAdapter.class.getSimpleName();
//
//    public MovieVideosAdapter(Activity context, List<String[]> videos) {
//
//        super(context, 0, videos);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        // Gets the video String[] from the ArrayAdapter at the appropriate position
//        String[] video = getItem(position);
//
//        // Adapters recycle views to AdapterViews.
//        // If this is a new View object we're getting, then inflate the layout.
//        // If not, this view already has the layout inflated from a previous call to getView,
//        // and we modify the View widgets as usual.
//        if (convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_video_item, parent, false);
//        }
//
//        ImageView videoIconBackground = (ImageView) convertView.findViewById(R.id.video_icon_background);
//        Picasso.with(getContext()).load(MovieDetailFragment.sPosterUrlStr).into(videoIconBackground);
//
//        TextView videoName = (TextView) convertView.findViewById(R.id.video_name);
//        videoName.setText(video[1]);
//
//        // Log.v(LOG_TAG, movie.getMovieTitle() + ", " + movie.getVideos());
//
//        return convertView;
//    }
//}

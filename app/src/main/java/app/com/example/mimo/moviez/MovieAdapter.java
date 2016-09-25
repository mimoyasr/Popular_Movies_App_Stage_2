package app.com.example.mimo.moviez;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;


public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        Movie movie = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.movies_gridview_item, viewGroup, false);
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.movie_gridview_item_holder);
        Picasso.with(getContext())
                .load("http://image.tmdb.org/t/p/w342/" + movie.getPosterImgURL())
                .into(imageView);
        return view;
    }
}

package app.com.example.mimo.moviez;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TrailerAdapter extends ArrayAdapter<Trailer> {
    public TrailerAdapter(Context context, List<Trailer> trailers) {
        super(context, 0, trailers);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        Trailer trailer = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.movie_trailer_item,
                    viewGroup, false);
        }

        TextView trailerName;
        ImageView youtubeLogo;
        trailerName = (TextView) view.findViewById(R.id.movie_trailer_name);
        youtubeLogo = (ImageView) view.findViewById(R.id.youtube_logo);
        trailerName.setText(trailer.getName());
        Picasso.with(getContext())
        .load("http://img.youtube.com/vi/"+trailer.getKey()+"/0.jpg")
        .into(youtubeLogo);
        return view;
    }
}

package app.com.example.mimo.moviez;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class ReviewAdapter extends ArrayAdapter<Review> {

    public ReviewAdapter(Context context, List<Review> reviews) {
        super(context, 0, reviews);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Review review = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.movie_review_item,
                    viewGroup, false);
        }
        TextView reviewAuthor = (TextView) view.findViewById(R.id.review_author);
        reviewAuthor.setText(review.getAuthor());
        TextView reviewContent = (TextView) view.findViewById(R.id.review_content);
        reviewContent.setText(review.getContent());
        return view;
    }
}

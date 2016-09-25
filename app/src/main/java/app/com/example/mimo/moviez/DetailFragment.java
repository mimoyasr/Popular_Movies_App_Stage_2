package app.com.example.mimo.moviez;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {

    public static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_MOVIE = "DETAIL_MOVIE";
    private Movie movie;
    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;
    private DBHelper db;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null)
            movie = (Movie) intent.getParcelableExtra(Intent.EXTRA_TEXT);

        Bundle arguments = getArguments();
        if (arguments != null) {
            movie = arguments.getParcelable(DetailFragment.DETAIL_MOVIE);
        }
        getActivity().setTitle(movie.getTitle());
        trailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());
        reviewAdapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
        if (movie != null) {

            ImageView imageView = (ImageView) view.findViewById(R.id.backdrop_img);
            TextView releaseDateTextView = (TextView) view.findViewById(R.id.detail_release_date);
            TextView voteRateTextView = (TextView) view.findViewById(R.id.detail_vote_average);
            TextView overviewTextView = (TextView) view.findViewById(R.id.overview_text);

            NonScrollListView reviews = (NonScrollListView) view.findViewById(R.id.movie_reviews_list);
            NonScrollListView trailers = (NonScrollListView) view.findViewById(R.id.movie_videos_list);

            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w500/" + movie.getPosterImgURL()).into(imageView);
            releaseDateTextView.setText(movie.getReleaseYear());
            voteRateTextView.setText(movie.getVoteAverage());
            overviewTextView.setText(movie.getOverview());
            reviews.setAdapter(reviewAdapter);
            trailers.setAdapter(trailerAdapter);


            trailers.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Trailer trailer = trailerAdapter.getItem(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
                    startActivity(intent);
                }

            });

            reviews.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Review review = reviewAdapter.getItem(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(review.getReviewUrl()));
                    startActivity(intent);
                }

            });
            final ImageView img = (ImageView) view.findViewById(R.id.add_favorite);
            db = new DBHelper(getActivity());
            if (db.Check(movie.getId())) {
                img.setImageResource(R.drawable.selected);

            } else {
                img.setImageResource(R.drawable.select);
            }
            img.setClickable(true);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (db.Check(movie.getId())) {
                        img.setImageResource(R.drawable.select);
                        db.deleteMovie(movie.getId());
                        Toast.makeText(getContext(), movie.getTitle() + "Removed from Favourites", Toast.LENGTH_SHORT).show();


                    } else {
                        img.setImageResource(R.drawable.selected);
                        db.insertMovie(movie.getId(), movie.getTitle(), movie.getPosterImgURL(), movie.getOverview(), movie.getReleaseYear(), movie.getVoteAverage());
                        Toast.makeText(getContext(), movie.getTitle() + "Added to Favourites", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } else {
            TextView noVideosView = (TextView) view.findViewById(R.id.no_trailers);
            noVideosView.setEnabled(false);
            noVideosView.setVisibility(View.GONE);
            TextView noReviewsView = (TextView) view.findViewById(R.id.no_reviews);
            noReviewsView.setEnabled(false);
            noReviewsView.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (movie != null) {
            new FetchTrailersTask().execute(movie.getId());
            new FetchReviewsTask().execute(movie.getId());
        } else {
            Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                movie.getTitle() + "#MovieZ");
        return shareIntent;
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        @Override
        protected List<Review> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null) {
                if (reviews.size() > 0) {
                    if (reviewAdapter != null) {
                        reviewAdapter.clear();
                        for (Review review : reviews) {
                            reviewAdapter.add(review);
                        }
                    }
                } else {
                    if (reviewAdapter != null) {
                        reviewAdapter.clear();
                        reviewAdapter.add(new Review("There is no reviews", null, null));
                    }
                }

            }
        }

        private List<Review> getReviewsDataFromJson(String jsonStr) throws JSONException {
            JSONObject reviewJson = new JSONObject(jsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray("results");

            List<Review> results = new ArrayList<>();

            for (int i = 0; i < reviewArray.length(); i++) {
                JSONObject review = reviewArray.getJSONObject(i);
                String author = review.getString("author");
                String content = review.getString("content");
                String reviewUrl = review.getString("url");
                results.add(new Review(author, content, reviewUrl));
            }

            return results;
        }


    }

    public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        @Override
        protected List<Trailer> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null) {
                if (trailers.size() > 0) {
                    if (trailerAdapter != null) {
                        trailerAdapter.clear();
                        for (Trailer trailer : trailers) {
                            trailerAdapter.add(trailer);
                        }
                    }
                }
            }
        }

        private List<Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject trailerJson = new JSONObject(jsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray("results");

            List<Trailer> results = new ArrayList<>();

            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                if (trailer.getString("site").equals("YouTube")) {
                    Trailer trailerModel = new Trailer(
                            trailer.getString("key"),
                            trailer.getString("name"));
                    results.add(trailerModel);
                }
            }
            return results;
        }
    }
}

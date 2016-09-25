package app.com.example.mimo.moviez;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment {

    private GridView gridView;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null ||
                !savedInstanceState.containsKey("movieList")) {
            movieList = new ArrayList<>();
        } else {
            movieList = savedInstanceState.getParcelableArrayList("movieList");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) view.findViewById(R.id.movies_gridview);
        movieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
        gridView.setAdapter(movieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movieAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(movie);
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movieList", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_favourite) {
            DBHelper db = new DBHelper(getActivity());
            ArrayList<Movie> movies_list = db.getMovies();
            if (movies_list.isEmpty()) {
                Toast.makeText(getContext(), "Favourites is Empty", Toast.LENGTH_SHORT).show();
            } else {
                movieAdapter.clear();
                for (final Movie movie : movies_list) {
                    movieAdapter.add(movie);
                }
            }
        }
        movieAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        FetchMovieData fetchMovieData = new FetchMovieData();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_by = prefs.getString(getString(R.string.sort_by_list_key),
                "popular");
        fetchMovieData.execute(sort_by);
    }

    public interface Callback {
        void onItemSelected(Movie movie);
    }

    public class FetchMovieData extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieData.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            final String API_KEY = getResources().getString(R.string.api_key);
            final String SORT_BY = params[0];

            try {
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/"
                        + SORT_BY
                        + "?api_key=";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL + API_KEY).buildUpon().build();
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
                movieJsonStr = buffer.toString();
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
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if (movies != null) {
                movieAdapter.clear();
                for (Movie movie : movies) {
                    movieAdapter.add(movie);
                }
                movieList.addAll(movies);
            } else {
                Toast.makeText(getActivity(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

        }

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException {

            ArrayList<Movie> moviesData = new ArrayList<Movie>();

            //list
            final String OWN_RESULTS = "results";

            //data
            final String OWN_ID = "id";
            final String OWN_POSTER = "poster_path";
            final String OVERVIEW = "overview";
            final String OWN_REL_DATE = "release_date";
            final String own_TITLE = "title";
            final String OWN_VOTE_AVG = "vote_average";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWN_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {

                String movieId = movieArray.getJSONObject(i).getString(OWN_ID);
                String movieTitle = movieArray.getJSONObject(i).getString(own_TITLE);
                String moviePoster = movieArray.getJSONObject(i).getString(OWN_POSTER);
                String movieOverview = movieArray.getJSONObject(i).getString(OVERVIEW);
                String movieRelDate = movieArray.getJSONObject(i).getString(OWN_REL_DATE);
                String movieVoteAvg = movieArray.getJSONObject(i).getString(OWN_VOTE_AVG);

                //Movie(String id, String title, String posterImgURL, String overview, String releaseYear, String voteAverage)
                Movie movie = new Movie(movieId, movieTitle, moviePoster, movieOverview, movieRelDate, movieVoteAvg);

                moviesData.add(i, movie);
            }

            return moviesData;
        }

    }

}

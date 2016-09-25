package app.com.example.mimo.moviez;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    public static final String TABLE_MOVIES = "movie";
    public static final String MOVIE_ID = "id";
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_POSTER = "poster_url";
    public static final String MOVIE_OVERVIEW = "overview";
    public static final String MOVIE_VOTE_AVG = "vote_ave";
    public static final String MOVIE_RELEASE_DATE = "release_date";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table movie " + "( MOVIE_ID text,MOVIE_TITLE text,MOVIE_POSTER text,MOVIE_OVERVIEW text ,MOVIE_RELEASE_DATE text ,MOVIE_VOTE_AVG text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS movie");
        onCreate(db);
    }

    public boolean insertMovie(String id, String title, String poster_url, String overview, String release_date, String vote_ave) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("MOVIE_ID", id);
        contentValues.put("MOVIE_TITLE", title);
        contentValues.put("MOVIE_POSTER", poster_url);
        contentValues.put("MOVIE_OVERVIEW", overview);
        contentValues.put("MOVIE_RELEASE_DATE", release_date);
        contentValues.put("MOVIE_VOTE_AVG", vote_ave);
        db.insert("movie", null, contentValues);
        return true;
    }

    public Integer deleteMovie(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MOVIES, " MOVIE_ID = ? ", new String[]{id});
    }

    public boolean Check(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "Select * from " + TABLE_MOVIES + " where   MOVIE_ID" + " = " + id;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public ArrayList<Movie> getMovies() {
        ArrayList<Movie> movies_list = new ArrayList<Movie>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_MOVIES, null);
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            String MovieId = cursor.getString(0);
            String movieTitle = cursor.getString(1);
            String moviePoster = cursor.getString(2);
            String movieOverview = cursor.getString(3);
            String releaseYear = cursor.getString(4);
            String movieVoteAvg = cursor.getString(5);
            Movie movie = new Movie(MovieId, movieTitle, moviePoster, movieOverview, releaseYear, movieVoteAvg);
            movies_list.add(movie);
            cursor.moveToNext();
        }
        return movies_list;
    }
}

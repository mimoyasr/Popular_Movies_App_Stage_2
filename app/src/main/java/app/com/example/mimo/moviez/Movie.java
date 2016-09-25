package app.com.example.mimo.moviez;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };
    private String id;
    private String title;
    private String posterImgURL;
    private String overview;
    private String voteAverage;
    private String releaseYear;

    public Movie(String id, String title, String posterImgURL, String overview, String releaseYear, String voteAverage) {

        setId(id);
        setTitle(title);
        setPosterImgURL(posterImgURL);
        setOverview(overview);
        setReleaseYear(releaseYear);
        setVoteAverage(voteAverage);

    }

    private Movie(Parcel in) {

        this.id = in.readString();
        this.title = in.readString();
        this.posterImgURL = in.readString();
        this.overview = in.readString();
        this.releaseYear = in.readString();
        this.voteAverage = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.title);
        parcel.writeString(this.posterImgURL);
        parcel.writeString(this.overview);
        parcel.writeString(this.releaseYear);
        parcel.writeString(this.voteAverage);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterImgURL() {
        return posterImgURL;
    }

    public void setPosterImgURL(String posterImgURL) {
        this.posterImgURL = posterImgURL;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        String[] splited = releaseYear.split("-");
        this.releaseYear = splited[0];
    }

}

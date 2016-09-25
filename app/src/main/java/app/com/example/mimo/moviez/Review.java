package app.com.example.mimo.moviez;


public class Review {

    private String author;
    private String content;

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    private String reviewUrl;

    public Review(String author, String content , String reviewUrl  ){
        setAuthor(author);
        setContent(content);
        setReviewUrl(reviewUrl);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

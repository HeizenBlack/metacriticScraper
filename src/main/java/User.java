import java.util.List;

public class User {

    private String username;
    private List<Review> reviews;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public User(String username, List<Review> reviews) {
        this.username = username;
        this.reviews = reviews;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }
}

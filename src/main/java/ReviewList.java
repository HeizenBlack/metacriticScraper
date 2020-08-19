import java.util.ArrayList;
import java.util.List;

public class ReviewList {
    private List<Review> list;

    public ReviewList(){
        list = new ArrayList<Review>();
    }

    public void add(Review r){
        list.add(r);
    }

    public int size(){
        return list.size();
    }

    public List<Review> getReviews(){
        return list;
    }
}

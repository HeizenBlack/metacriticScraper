import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.thoughtworks.xstream.XStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class main {

    private static final int GOT_number_pages=79;
    private static final String GOT_USER_REVIEWS_URL="https://www.metacritic.com/game/playstation-4/ghost-of-tsushima/user-reviews";
    private static final String USER_URL="https://www.metacritic.com/user/";


    private static final Set<String> tlou_first_3days_dates= Set.of("Jun 19, 2020","Jun 20, 2020","Jun 21, 2020");
    private static final Set<String> got_first_3days_dates= Set.of("Jul 18, 2020","Jul 19, 2020","Jul 20, 2020");


    public static void main(String[] args) throws IOException {

        //--------------------------------------------------------------------------------------------------------------
        //fetchUsers();

        //--------------------------------------------------------------------------------------------------------------
        //fillUserReviews();

        //--------------------------------------------------------------------------------------------------------------
        //exportReviews();

        //--------------------------------------------------------------------------------------------------------------
        dataAnalysis();
    }

    private static void fetchUsers() throws IOException {
        List<User> users=new ArrayList<>();
        for(int i=0;i<GOT_number_pages;i++)
        {
            OkHttpClient okHttp = new OkHttpClient();
            Request request = new Request.Builder().url(GOT_USER_REVIEWS_URL+"?page="+i).get().build();
            System.out.println((i+1)+"/"+GOT_number_pages+" ::: "+GOT_USER_REVIEWS_URL+"?page="+i);
            Document doc = Jsoup.parse(okHttp.newCall(request).execute().body().string());

            for(Element row : doc.select("div.name a"))
            {
                users.add(new User(row.getElementsByTag("a").text()));
            }

        }
        FileWriter usersFile = new FileWriter("users.txt");
        usersFile.write(users.size()+"");
        for(User user:users)
        {
            usersFile.write("\n"+user.getUsername());
        }
        usersFile.close();

    }

    private static void fillUserReviews() throws IOException {
        UserList users=new UserList();

        Scanner scanner = new Scanner(new File("users.txt"));
        int i=0;

        String totalUsers="";
        while (scanner.hasNextLine())
        {
            if(i==0)
            {
                totalUsers=scanner.nextLine();
            }
            else{
                String username =scanner.nextLine();
                OkHttpClient okHttp = new OkHttpClient();
                Request request = new Request.Builder().url(USER_URL+username).get().build();
                System.out.println(i+"/"+totalUsers+" ::: "+USER_URL+username);

                Document doc = Jsoup.parse(okHttp.newCall(request).execute().body().string());

                ReviewList reviews=new ReviewList();
                for(Element row : doc.select("div.review_section.review_data"))
                {
                    Elements review_stats=row.select("div.review_stats");
                    Elements review_body=row.select("div.review_body");

                    String game=review_stats.select("div.review_product").select("div.product_title").text();
                    String commentary=review_body.select("span.blurb.blurb_expanded").text();
                    String date=review_stats.select("div.review_product").select("div.date").text();;
                    String score=row.select("div.review_score").text();

                    Review review=new Review(game,commentary,date,score);
                    reviews.add(review);
                }
                User user=new User(username,reviews.getReviews());
                users.add(user);
            }
            i++;
        }
        scanner.close();

        XStream xstream = new XStream();
        xstream.alias("user", User.class);
        xstream.alias("users", UserList.class);
        xstream.alias("review", Review.class);
        xstream.alias("reviews", ReviewList.class);

        xstream.addImplicitCollection(UserList.class, "list");
        xstream.addImplicitCollection(ReviewList.class, "list");

        String xml = xstream.toXML(users);
        FileWriter usersXML = new FileWriter("users.xml");
        usersXML.write(xml);
        usersXML.close();
    }

    private static void exportReviews() throws IOException {
        FileWriter Got_Reviews_file = new FileWriter("GOT_Reviews.txt");
        FileWriter TLOU_reviews_file = new FileWriter("TLOU_Reviews.txt");
        FileWriter TLOU_3days_reviews_file = new FileWriter("TLOU_3days_Reviews.txt");

        UserList users = getUsersFromXML();

        for(User user:users.getUsers()){
            for(Review review:user.getReviews())
            {
                if(review.getGameTitle().equals("Ghost of Tsushima")) {
                    Got_Reviews_file.write(review.getCommentary()+"\n");
                }
                if(review.getGameTitle().equals("The Last of Us Part II")) {
                    if(tlou_first_3days_dates.contains(review.getDate()))
                    {
                        TLOU_3days_reviews_file.write(review.getCommentary());
                    }
                    TLOU_reviews_file.write(review.getCommentary()+"\n");
                }
            }
        }

        Got_Reviews_file.close();
        TLOU_reviews_file.close();
        TLOU_3days_reviews_file.close();
    }

    private static void dataAnalysis() throws IOException {

        UserList users = getUsersFromXML();

        int[][] all_scores_matrix=new int[11][11];
        int[][] first_3days_scores_matrix=new int[11][11];

        for(User user:users.getUsers()) {
            List<Review> reviews = user.getReviews();
            Review tlou_review=null;
            Review got_review=null;
            for(Review review:reviews) {
                if(review.getGameTitle().equals("Ghost of Tsushima")) {
                    got_review=review;
                }
                if(review.getGameTitle().equals("The Last of Us Part II")) {
                    tlou_review=review;
                }
            }
            if(tlou_review!=null && got_review!=null){
                all_scores_matrix[Integer.parseInt(got_review.getScore())][Integer.parseInt(tlou_review.getScore())]++;

                if(tlou_first_3days_dates.contains(tlou_review.getDate()) && got_first_3days_dates.contains(got_review.getDate()))
                {
                    first_3days_scores_matrix[Integer.parseInt(got_review.getScore())][Integer.parseInt(tlou_review.getScore())]++;
                }
            }
        }

        drawMatrix(all_scores_matrix);
        drawMatrix(first_3days_scores_matrix);
        System.out.println();

        //--------Average score given to each game based on only written reviews
        printAverageScore(all_scores_matrix,"Based on only written reviews");
        System.out.println();

        //--------Average score for each game for the first 3 days
        printAverageScore(first_3days_scores_matrix,"Based on only written reviews in the first 3 days");
    }

    private static void drawMatrix(int[][] m)
    {
        for(int[] row:m) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static void printAverageScore(int[][] m, String info)
    {
        float GOT_score=0;
        int Got_total_reviewers=0;

        float TLOU_score=0;
        int TLOU_total_reviewers=0;

        for(int i=0;i<11;i++)
        {
            GOT_score+=sumColumn(m,i)*i;
            Got_total_reviewers+=sumColumn(m,i);
        }
        for(int j=0;j<11;j++)
        {
            TLOU_score+=sumRow(m,j)*j;
            TLOU_total_reviewers+=sumRow(m,j);
        }

        GOT_score=GOT_score/Got_total_reviewers;
        TLOU_score=TLOU_score/TLOU_total_reviewers;

        System.out.println("Ghost of Tsushima score ("+info+") : "+GOT_score);
        System.out.println("The Last of Us Part II score ("+info+") : "+TLOU_score);
    }

    private static int sumColumn(int[][] m,int col) {
        int sum=0;
        for(int n:m[col])
        {
            sum+=n;
        }
        return sum;
    }
    private static int sumRow(int[][] m,int row) {
        int sum=0;
        for(int[] col:m)
        {
            sum+=col[row];
        }
        return sum;
    }

    private static UserList getUsersFromXML() throws IOException {
        String xml = Files.readString(Paths.get("users.xml"));

        XStream xstream = new XStream();
        xstream.alias("user", User.class);
        xstream.alias("users", UserList.class);
        xstream.alias("review", Review.class);
        xstream.alias("reviews", ReviewList.class);

        xstream.addImplicitCollection(UserList.class, "list");
        xstream.addImplicitCollection(ReviewList.class, "list");

        return (UserList) xstream.fromXML(xml);
    }
}

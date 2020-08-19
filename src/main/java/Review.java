public class Review {
    private String gameTitle;
    private String commentary;
    private String date;
    private String score;

    public Review() {
    }

    public Review(String gameTitle, String commentary, String date, String score) {
        this.gameTitle = gameTitle;
        this.commentary = commentary;
        this.date = date;
        this.score = score;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}

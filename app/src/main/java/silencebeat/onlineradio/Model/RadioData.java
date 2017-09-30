package silencebeat.onlineradio.Model;

/**
 * Created by Candra Triyadi on 29/05/2017.
 */

public class RadioData {

    String title;
    String url;
    String genres;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getGenres() {
        return genres;
    }
}

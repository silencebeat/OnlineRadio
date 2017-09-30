package silencebeat.onlineradio.Presenter;

import android.os.AsyncTask;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by Candra Triyadi on 30/09/2017.
 */

public class ParseImagePresenter extends Observable{

    String userAgent;
    ParseImage parseImage;
    String query = null;

    public ParseImagePresenter(){

        userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
    }

    public void getImage(String query){
        if (parseImage != null){
            parseImage.cancel(true);
            parseImage = null;
        }
        this.query = query;
        parseImage = new ParseImage();
        parseImage.execute(query);
    }

    class ParseImage extends AsyncTask<String, Integer, Document> {
        ArrayList<String> resultUrls = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... params) {
            Document doc = null;
            try {
                doc = Jsoup.connect("https://www.google.com/search?site=imghp&tbm=isch&source=hp&q="+query+"&gws_rd=cr&tbm=isch").userAgent(userAgent).referrer("https://www.google.com/").get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {
            super.onPostExecute(doc);
            try{
                Elements elements = doc.select("div.rg_meta");
                JSONObject jsonObject;
                for (Element element : elements) {
                    if (element.childNodeSize() > 0) {
                        jsonObject = (JSONObject) new JSONParser().parse(element.childNode(0).toString());
                        resultUrls.add((String) jsonObject.get("ou"));
                        break;
                    }
                }

                if (resultUrls.size() > 0){
                    String imageUrl = resultUrls.get(0);
                    setChanged();
                    notifyObservers(imageUrl);
                }
            }catch ( ParseException e) {
                e.printStackTrace();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}

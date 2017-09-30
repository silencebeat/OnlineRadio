package silencebeat.onlineradio.Support;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import silencebeat.onlineradio.Model.RadioData;
import silencebeat.onlineradio.Model.RadioDatas;

/**
 * Created by Candra Triyadi on 29/05/2017.
 */

public class TextReader {

    Context context;
    SimpleDB simpleDB;
    ReadTextListener readTextListener;

    public interface ReadTextListener{
        void onFinishReadingText();
        void onErrorReadingText();
    }

    public TextReader(Context context, ReadTextListener readTextListener){
        this.context = context;
        simpleDB = new SimpleDB(context);
        this.readTextListener = readTextListener;
        new loadUrls().execute();
    }

    private class loadUrls extends AsyncTask<Void, Void, RadioDatas>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected RadioDatas doInBackground(Void... params) {
            BufferedReader reader = null;
            ArrayList<RadioData> radioDatas = new ArrayList<>();
            RadioDatas datas = new RadioDatas();
            try {
                reader = new BufferedReader(
                        new InputStreamReader(context.getAssets().open("url.txt"), "Unicode"));

                String mLine;
                while ((mLine = reader.readLine()) != null) {
                    RadioData radioData = new RadioData();
                    String[] meta = mLine.split(";");
                    radioData.setUrl(meta[0]);
                    radioData.setTitle(meta[1]);
                    radioData.setGenres(meta[2]);
                    radioDatas.add(radioData);
                }
            } catch (IOException e) {
                //log the exception
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //log the exception
                    }
                }
            }

            datas.setRadioDatas(radioDatas);
            return datas;
        }

        @Override
        protected void onPostExecute(RadioDatas datas) {
            if (datas == null){
                readTextListener.onErrorReadingText();
                return;
            }
            simpleDB.putObject(StaticVariable.URLs, datas);
            readTextListener.onFinishReadingText();
        }
    }
}

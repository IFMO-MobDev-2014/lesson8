package ru.eugene.weather.downloadWeather;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ru.eugene.weather.ContentProviders.WeatherProvider;
import ru.eugene.weather.database.WeatherItem;

/**
 * Created by eugene on 12/16/14.
 */
public class ServiceDownload extends IntentService {
    public static final String NOTIFICATION = "ru.eugene.weather.downloadWeather";
    public static final String URL_ADDRESS = "URL";
    public static final String RESULT = "result";

    public ServiceDownload() {
        super("ServiceDownload");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // params comes from the execute() call: params[0] is the url.
        try {
            InputStream result = downloadUrl(intent.getStringExtra(URL_ADDRESS));
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            HandlerRSS handlerRSS = new HandlerRSS();

            Reader isr = new InputStreamReader(result);
            InputSource is = new InputSource();
            is.setCharacterStream(isr);

            saxParser.parse(is, handlerRSS);
            Intent resultIntent = new Intent(NOTIFICATION);
            resultIntent.putExtra(RESULT, handlerRSS.getWeatherItems());
            Log.i("LOG", "in handlerRSS: " + handlerRSS.getWeatherItems().get(0).getCode());

//            for (WeatherItem it : handlerRSS.getWeatherItems()) {
//                ContentValues values = WeatherProvider.generateContentValuesFromWeatherItem(it);
//                getContentResolver().insert(WeatherProvider.CONTENT_URI_WEATHER_INFO, values);
//            }

            sendBroadcast(resultIntent, null);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Intent resultIntent = new Intent(NOTIFICATION);
            resultIntent.putExtra(RESULT, (Serializable) null);
            sendBroadcast(resultIntent, null);
            return;
        }
    }

    public static InputStream downloadUrl(String nameUrl) throws IOException {
        InputStream is;

        URL url = new URL(nameUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        is = conn.getInputStream();

        return is;
    }


}

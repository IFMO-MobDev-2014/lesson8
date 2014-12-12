package ru.ifmo.md.lesson8.service;

import android.os.AsyncTask;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by flyingleafe on 09.12.14.
 */
public class CitySearchTask extends AsyncTask<String, Void, ForecastParser.LocationResult> {

    AsyncResponseReceiver<ForecastParser.LocationResult> receiver;

    public CitySearchTask(AsyncResponseReceiver<ForecastParser.LocationResult> r) {
        receiver = r;
    }

    public static final String CITY_SEARCH_URI =
            "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20city%3D%22{{ cityName }}%22";

    @Override
    protected ForecastParser.LocationResult doInBackground(String... params) {
        String name = params[0];
        String searchUrl = CITY_SEARCH_URI.replace("{{ cityName }}", name);
        try {
            InputStream response = new URL(searchUrl).openStream();
            return new ForecastParser().parseLocation(response);
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ForecastParser.LocationResult locationResult) {
        super.onPostExecute(locationResult);
        receiver.processFinish(locationResult);
    }
}

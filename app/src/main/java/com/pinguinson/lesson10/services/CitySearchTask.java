package com.pinguinson.lesson10.services;

import android.os.AsyncTask;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by pinguinson.
 */
public class CitySearchTask extends AsyncTask<String, Void, ForecastParser.LocationResult> {

    public static final String CITY_SEARCH_URI =
            "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20city%3D%22  *cityName*  %22";
    AsyncResponseReceiver<ForecastParser.LocationResult> receiver;

    public CitySearchTask(AsyncResponseReceiver<ForecastParser.LocationResult> r) {
        receiver = r;
    }

    @Override
    protected ForecastParser.LocationResult doInBackground(String... params) {
        String name = params[0];
        String searchUrl = CITY_SEARCH_URI.replace("  *cityName*  ", name);
        InputStream inputStream = null;
        try {
            inputStream = new URL(searchUrl).openStream();
            return new ForecastParser().parseLocation(inputStream);
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                //can't do anything about it
            }
        }
        //this try/catch/finally structure looks bulky
    }

    @Override
    protected void onPostExecute(ForecastParser.LocationResult locationResult) {
        super.onPostExecute(locationResult);
        receiver.processFinish(locationResult);
    }
}

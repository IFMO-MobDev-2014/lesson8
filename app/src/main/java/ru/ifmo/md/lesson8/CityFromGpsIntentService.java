package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Mikhail on 16.01.15.
 */
public class CityFromGpsIntentService extends IntentService{
    public CityFromGpsIntentService() {
        super("CityFromGpsIntentService");
    }
    private String lat;
    private String lon;
    private String update;


    @Override
    protected void onHandleIntent(Intent intent) {
        lat = intent.getStringExtra("lat");
        lon = intent.getStringExtra("lon");
        update = intent.getStringExtra("update");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");

        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20text%3D%22" + lat + "%2C" + lon + "%22%20and%20gflags%3D%22R%22");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();

            SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
            SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
            XMLReader myXMLReader = mySAXParser.getXMLReader();
            CityFromGpsHandler myCityFromGpsHandler = new CityFromGpsHandler();
            myXMLReader.setContentHandler(myCityFromGpsHandler);
            InputSource myInputSource = new InputSource(new StringReader(content.toString()));
            myXMLReader.parse(myInputSource);

            City city = new City(myCityFromGpsHandler.getCity());
            String lastCityName = city.getCityName();
            city.setCityName("Current location:" + "\n" + lastCityName);
            Bundle bundle = new Bundle();
            bundle.putString("cityName", city.getCityName());
            bundle.putString("woeid", city.getWoeid());

            if (update.equals("delete and add")) {
                receiver.send(AppResultsReceiver.STATUS_GPS_FINISHED_DELETE_AND_ADD, bundle);
            } else {
                receiver.send(AppResultsReceiver.STATUS_GPS_FINISHED_ADD, bundle);
            }
        } catch (SAXException | ParserConfigurationException e) {
            receiver.send(AppResultsReceiver.STATUS_PARSE_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        } catch (IOException e) {
            receiver.send(AppResultsReceiver.STATUS_GPS_INTERNET_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        }
    }
}

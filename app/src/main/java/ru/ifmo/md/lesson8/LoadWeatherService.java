package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class LoadWeatherService extends IntentService {
    private long cityId;
    private Uri weathersUri;
    private ResultReceiver receiver;

    public LoadWeatherService() {
        super("LoadWeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        receiver = intent.getParcelableExtra("receiver");
        final Uri uri = WeatherContentProvider.CONTENT_URI_CITIES;
        String[] projection = {"_id", "woeid", "name"};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);


        try {
            while (cursor.moveToNext()) {
                long woeid = cursor.getLong(cursor.getColumnIndexOrThrow("woeid"));
                cityId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                String weatherUrl = "http://weather.yahooapis.com/forecastrss?u=c&w=" + woeid;
                weathersUri = Uri.withAppendedPath(WeatherContentProvider.CONTENT_URI_WEATHERS, "" + cityId);

                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                SAXParser saxParser = saxParserFactory.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                XMLHandler xmlHandler = new XMLHandler();
                xmlReader.setContentHandler(xmlHandler);
                InputSource inputSource = new InputSource(new URL(weatherUrl).openStream());
                xmlReader.parse(inputSource);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            ReportException(e.toString());
        }
    }

    private void ReportException(String s) {
        Bundle b = new Bundle();
        b.putString("error", s);
        receiver.send(0, b);
    }

    private class XMLHandler extends DefaultHandler {

        @Override
        public void startDocument() {
            getContentResolver().delete(weathersUri, null, null);
        }


        @Override
        public void startElement(String string, String localName, String qName, Attributes attrs) throws SAXException {
            if (qName.equals("yweather:forecast")) {
                ContentValues values = new ContentValues();
                values.put("city_id", cityId);
                values.put("weekday", attrs.getValue("day").toUpperCase());
                values.put("temp", attrs.getValue("low"));
                values.put("weather_type", attrs.getValue("text"));
                values.put("weather_code", attrs.getValue("code"));

                getContentResolver().insert(weathersUri, values);
            }
        }
    }
}

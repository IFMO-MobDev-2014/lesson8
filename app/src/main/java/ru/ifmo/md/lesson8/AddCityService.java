package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class AddCityService extends IntentService {
    private ResultReceiver receiver;

    public AddCityService() {
        super("AddCityService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String cityName = intent.getStringExtra("city_name");
        receiver = intent.getParcelableExtra("receiver");
        try {
            String encodedCityName = URLEncoder.encode(cityName, "utf-8");
            String weatherUrl = "http://where.yahooapis.com/v1/places.q('" + encodedCityName + "')?appid=dj0yJmk9VDRsTU02amJhbEF0JmQ9WVdrOWNqSm1NRmwyTjJVbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD1hZQ--";

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            XMLHandler xmlHandler = new XMLHandler();
            xmlReader.setContentHandler(xmlHandler);
            InputSource inputSource = new InputSource(new URL(weatherUrl).openStream());
            xmlReader.parse(inputSource);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            reportException(e.toString());
        }

        startService(new Intent(getApplicationContext(), LoadWeatherService.class).putExtra("receiver", receiver));
    }

    private void reportException(String s) {
        Bundle b = new Bundle();
        b.putString("error", s);
        receiver.send(0, b);
    }

    private class XMLHandler extends DefaultHandler {
        private boolean saveText;
        private boolean stop;
        private String currentText = "";
        private ContentValues values = new ContentValues();

        @Override
        public void startElement(String string, String localName, String qName, Attributes attrs) throws SAXException {
            if (!stop) {
                if (qName.equals("woeid") || qName.equals("name")) {
                    saveText = true;
                    currentText = "";
                }
            }
        }

        @Override
        public void endElement(String string, String localName, String qName) throws SAXException {
            saveText = false;
            if (qName.equals("woeid")) {
                values.put("woeid", currentText.trim());
            } else if (qName.equals("name")) {
                values.put("name", currentText);
                getContentResolver().insert(WeatherContentProvider.CONTENT_URI_CITIES, values);
                stop = true;
            }
            if (qName.equals("places") && values.size() == 0) {
                reportException("City not found");
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String strCharacters = new String(ch, start, length);
            if (saveText) currentText += strCharacters;
        }
    }
}

package ru.ifmo.md.lesson8;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class MyLoaderIntentService extends IntentService {

    public Uri forecastUriCurrCity;
    public Uri currCityUri;
    public ResultReceiver rs;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public MyLoaderIntentService() {
        super("MyLoaderIntentService");
    }

    private String getXmlFromUrl(String urlString) {
        StringBuffer output = new StringBuffer("");
        try {
            InputStream stream;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return output.toString();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onHandleIntent(Intent intent)  {

        int mode = intent.getIntExtra("mode", 1);

        String link = intent.getStringExtra("link");
        String name = intent.getStringExtra("city_name");
        int city_id = intent.getIntExtra("city_id", 0);
        rs = intent.getParcelableExtra("receiver");

        if (mode == 2) {
            Cursor c = getContentResolver().query(MyContentProvider.TABLE_CITIES_URI, null, MyContentProvider.COLUMN_CITY_NAME + " = '" + name +"'", null, null, null);
            if (c.getCount() == 0) {
                ContentValues cv = new ContentValues();
                cv.put(MyContentProvider.COLUMN_CITY_NAME, name);
                cv.put(MyContentProvider.COLUMN_WEATHER, "good");
                cv.put(MyContentProvider.COLUMN_WIND, "strom");
                cv.put(MyContentProvider.COLUMN_WIND_SPEED, "100");
                cv.put(MyContentProvider.COLUMN_TEMP, "25");
                currCityUri = getContentResolver().insert(MyContentProvider.TABLE_CITIES_URI, cv);
            }
            else {
                currCityUri = Uri.withAppendedPath(MyContentProvider.TABLE_CITIES_URI, Long.toString(city_id));
            }
            c.close();
        }
        else if (mode == 1) {
            forecastUriCurrCity = Uri.withAppendedPath(MyContentProvider.TABLE_FORECAST_URI, Long.toString(city_id));
        }

        String xml;

        try {
            xml = getXmlFromUrl(link);
            if (xml.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Service is unreachanble", Toast.LENGTH_LONG);
                Log.d("Internet", "service is unreachable");
                return;
            }
            InputStream stream = new ByteArrayInputStream(xml.getBytes());
            (new MySAXParser(mode, city_id)).parse(stream);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.i("BAGUETTE", e.toString());
        }
    }

    public class MySAXParser {
        private int mode, city_id;

        MySAXParser(int mode, int c) {
            this.mode = mode;
            this.city_id = c;
        }

        public void parse(InputStream is) throws IOException, SAXException, ParserConfigurationException {
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

            DefaultHandler saxHandler = new DefaultHandler();

            switch (mode) {
                case 1: {
                    getContentResolver().delete(forecastUriCurrCity, null, null);
                    Log.d("parse", "deleted");
                    saxHandler = new SAXForecastParserHandler();
                    break;
                }
                case 2: {
                    saxHandler = new SAXCurrentWeatherParserHandler();
                    break;
                }
                case 3: {
                    saxHandler = new SAXCoordParserHandler();
                }
            }

            xmlReader.setContentHandler(saxHandler);
            xmlReader.parse(new InputSource(is));
        }
    }


    public class SAXForecastParserHandler extends DefaultHandler {

        private ContentValues node;


        @Override
        public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
            if (qName.equals("time")) {
                node = new ContentValues();
                Log.d("NewDate", attributes.getValue("day"));
                node.put(MyContentProvider.COLUMN_DATE, attributes.getValue("day"));
            }
            if (node != null) {
                if (qName.equals("symbol")) {
                    node.put(MyContentProvider.COLUMN_WEATHER, attributes.getValue("name"));
                    node.put(MyContentProvider.COLUMN_WEATHER_ICON, attributes.getValue("var"));
                    Log.d("Icons got", attributes.getValue("var"));
                } else if (qName.equals("windSpeed")) {
                    node.put(MyContentProvider.COLUMN_WIND, attributes.getValue("name"));
                    node.put(MyContentProvider.COLUMN_WIND_SPEED, attributes.getValue("mps"));
                } else if (qName.equals("temperature")) {
                    node.put(MyContentProvider.COLUMN_TEMP_MORN, "Morn: " + (int)Double.parseDouble(attributes.getValue("morn"))+ " C, ");
                    node.put(MyContentProvider.COLUMN_TEMP_DAY, "Day: " + (int)Double.parseDouble(attributes.getValue("day")) + " C, ");
                    node.put(MyContentProvider.COLUMN_TEMP_EVE, "Eve: " + (int)Double.parseDouble(attributes.getValue("eve")) + " C, ");
                    node.put(MyContentProvider.COLUMN_TEMP_NIGHT, "Night: " + (int)Double.parseDouble(attributes.getValue("night"))+ " C ");
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

        }

        @Override
        public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
            if (node != null) {
                if (qName.equals("time")) {
                    getContentResolver().insert(forecastUriCurrCity, node);
                    node = null;
                }
            }
        }

    }



    public class SAXCoordParserHandler extends DefaultHandler {

        private ContentValues node;
        String cityName;

        @Override
        public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
            if (qName.equals("current")) {
                node = new ContentValues();
            }
            if (node != null) {
                if (qName.equals("city")) {
                    node.put(MyContentProvider.COLUMN_CITY_NAME, attributes.getValue("name"));
                    cityName = attributes.getValue("name");
                } else if (qName.equals("speed")) {
                    node.put(MyContentProvider.COLUMN_WIND, attributes.getValue("name"));
                    node.put(MyContentProvider.COLUMN_WIND_SPEED, attributes.getValue("value") + " mps");
                } else if (qName.equals("temperature")) {
                    node.put(MyContentProvider.COLUMN_TEMP, Integer.toString((int)(Double.parseDouble(attributes.getValue("value"))-273.16)) + " C ");
                }
                else if (qName.equals("weather")) {
                    node.put(MyContentProvider.COLUMN_WEATHER, attributes.getValue("value"));
                    node.put(MyContentProvider.COLUMN_WEATHER_ICON, attributes.getValue("icon"));
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
            if (node != null) {
                if (qName.equals("current")) {
                    Cursor c = getContentResolver().query(MyContentProvider.TABLE_CITIES_URI, null, MyContentProvider.COLUMN_CITY_NAME + " = '" + cityName + "'", null, null, null);
                    if (c.getCount() == 0) {
                        getContentResolver().insert(MyContentProvider.TABLE_CITIES_URI, node);
                        Bundle b = new Bundle();
                        b.putString("city", cityName);
                        rs.send(0,b);
                        Log.d("GPS", "sent");
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "City already added", Toast.LENGTH_LONG).show();
                    }
                    c.close();
                    node = null;
                }
            }
        }

    }


    public class SAXCurrentWeatherParserHandler extends DefaultHandler {

        private ContentValues node;

        @Override
        public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
            if (qName.equals("current")) {
                node = new ContentValues();
            }
            if (node != null) {
                if (qName.equals("city")) {
                    node.put(MyContentProvider.COLUMN_CITY_NAME, attributes.getValue("name"));
                } else if (qName.equals("speed")) {
                    node.put(MyContentProvider.COLUMN_WIND, attributes.getValue("name"));
                    node.put(MyContentProvider.COLUMN_WIND_SPEED, attributes.getValue("value") + " mps");
                } else if (qName.equals("temperature")) {
                    node.put(MyContentProvider.COLUMN_TEMP, Integer.toString((int)(Double.parseDouble(attributes.getValue("value"))-273.16)) + " C ");
                }
                else if (qName.equals("weather")) {
                    node.put(MyContentProvider.COLUMN_WEATHER, attributes.getValue("value"));
                    node.put(MyContentProvider.COLUMN_WEATHER_ICON, attributes.getValue("icon"));
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

        }

        @Override
        public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
            if (node != null) {
                if (qName.equals("current")) {
                    int y = getContentResolver().update(currCityUri, node, null, null);
              //      Log.d("UPDATED", Integer.toString(y));
                    node = null;

                }
            }
        }

    }

}

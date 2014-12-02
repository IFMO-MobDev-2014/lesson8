package ru.ifmo.md.lesson8.data;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static ru.ifmo.md.lesson8.data.WeatherContentProvider.CITY_URI;
import static ru.ifmo.md.lesson8.data.WeatherContentProvider.WEATHER_URI;

/**
 * Created by mariashka on 11/28/14.
 */
public class WeatherService extends IntentService {

    private String a1 = "http://api.worldweatheronline.com/free/v2/weather.ashx?q=";
    private String a2 = "&format=xml&num_of_days=4&lang=en&key=074fdf086f7d38d448f8c3f44b353";

    public static final String ACTION = "RESPONSE";

    public WeatherService() {
        super("net");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String flag = intent.getStringExtra("FLAG");

        if (flag.equals("all"))
            updateAll();
        else
            addCity(flag);

    }


    void addCity(String s) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("name", s);
            getContentResolver().insert(CITY_URI, cv);
            Cursor cursor = getContentResolver().query(CITY_URI, null, null, null, null);
            int n = cursor.getCount();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            WeatherHandler handler = new WeatherHandler();
            URL url = new URL(a1 + s + a2);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream in = connection.getInputStream();
            saxParser.parse(in, handler);
            connection.disconnect();

            List<WeatherItem> items = handler.getItems();
            WeatherItem curr = items.get(0);
            cv.put("cond", curr.getCondition());
            cv.put("curr_t", curr.getCurrT());
            cv.put("date_t", curr.getDate());
            cv.put("feel", curr.getFeels());

            for (int i = 1; i < items.size(); i++) {
                cv.put("next_min" + Integer.toString(i), items.get(i).getMin());
                cv.put("next_max" + Integer.toString(i), items.get(i).getMax());
                cv.put("next_c" + Integer.toString(i), items.get(i).getHourlyC().get(4));
            }

            List<Integer> hT = curr.getHourlyT();
            List<String> hC = curr.getHourlyC();
            for (int i = 0; i < hT.size() / 2; i++) {
                cv.put("hour_t" + Integer.toString(i + 1), hT.get(2*i));
                cv.put("hour_c" + Integer.toString(i + 1), hC.get(2*i));
            }
            getContentResolver().insert(WEATHER_URI, cv);
            response(true);
        } catch (ParserConfigurationException e) {
            response(false);
            e.printStackTrace();
        } catch (SAXException e) {
            response(false);
            e.printStackTrace();
        } catch (IOException e) {
            response(false);
            e.printStackTrace();
        }
    }

    void updateAll() {

        List<WeatherItem> list = new ArrayList<>();
        Cursor subs = getContentResolver().query(CITY_URI, null, null, null, null);
        if (subs != null) {
            subs.moveToFirst();
            if (subs.isAfterLast()) {
                ContentValues cv = new ContentValues();
                cv.put("name", "Saint-Petersburg");
                getContentResolver().insert(CITY_URI, cv);
                cv = new ContentValues();
                cv.put("name", "Moscow");
                getContentResolver().insert(CITY_URI, cv);
                cv = new ContentValues();
                cv.put("name", "London");
                getContentResolver().insert(CITY_URI, cv);
                cv = new ContentValues();

                subs = getContentResolver().query(CITY_URI, null, null, null, null);
                subs.moveToFirst();
            }
            try {
                do {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    WeatherHandler handler = new WeatherHandler();

                    String city = subs.getString(1);
                    URL url = new URL(a1 + city + a2);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream in = connection.getInputStream();
                    saxParser.parse(in, handler);

                    List<WeatherItem> items = handler.getItems();
                    WeatherItem curr = items.get(0);
                    curr.setName(city);
                    for (int i = 1; i < items.size(); i++) {
                        items.get(i).setCondition(items.get(i).getHourlyC().get(4));
                        curr.addNext(items.get(i));
                    }
                    list.add(curr);
                    connection.disconnect();
                } while (subs.moveToNext());
            } catch (ParserConfigurationException e) {
                response(false);
                e.printStackTrace();
            } catch (SAXException e) {
                response(false);
                e.printStackTrace();
            } catch (IOException e) {
                response(false);
                e.printStackTrace();
            }
        }

        Cursor cursor = getContentResolver().query(WEATHER_URI, null, null, null, null);
        cursor.moveToFirst();
        int n = cursor.getCount();
        for (int i = 0; i < n; i++) {
            Uri uri = ContentUris.withAppendedId(WEATHER_URI, cursor.getInt(0));
            getContentResolver().delete(uri, null, null);
            cursor = getContentResolver().query(WEATHER_URI, null, null, null, null);
            cursor.moveToFirst();
        }
        for (WeatherItem aCurr : list) {
            cursor = getContentResolver().query(WEATHER_URI, null, null, null, null);
            n = cursor.getCount();
            ContentValues cv = new ContentValues();
            cv.put("name", aCurr.getName());
            cv.put("cond", aCurr.getCondition());
            cv.put("curr_t", aCurr.getCurrT());
            cv.put("date_t", aCurr.getDate());
            cv.put("feel", aCurr.getFeels());

            List<Integer> hT = aCurr.getHourlyT();
            List<String> hC = aCurr.getHourlyC();
            for (int i = 0; i < hT.size() / 2; i ++) {
                cv.put("hour_t" + Integer.toString(i + 1), hT.get(2 * i));
                cv.put("hour_c" + Integer.toString(i + 1), hC.get(2 * i));
            }

            List<WeatherItem> next = aCurr.getNext();
            for (int i = 0; i < next.size(); i++) {
                cv.put("next_min" + Integer.toString(i + 1), next.get(i).getMin());
                cv.put("next_max" + Integer.toString(i + 1), next.get(i).getMax());
                cv.put("next_c" + Integer.toString(i + 1), next.get(i).getCondition());
            }
            getContentResolver().insert(WEATHER_URI, cv);
        }
        response(true);
    }





    private void response(boolean f) {
        Intent intentResponse = new Intent();
		intentResponse.setAction(ACTION);
		intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
		intentResponse.putExtra("response", f);
		sendBroadcast(intentResponse);
    }
}

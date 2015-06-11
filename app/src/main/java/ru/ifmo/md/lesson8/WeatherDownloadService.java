package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WeatherDownloadService extends IntentService {
    public static final String ACTION_RESPONSE = "ru.ifmo.md.lesson8.weatherDownloadService.RESPONSE";
    public static final String URL_TAG = "URLTAG";
    public static final String CITY_TAG = "CITYTAG";

    public WeatherDownloadService() {
        super("weatherDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Weather weather = new Weather();
        String url = intent.getStringExtra(URL_TAG);
        weather.city = intent.getStringExtra(CITY_TAG);
        Log.d("debug1", "WeatherDownloadService started for city " + weather.city);

        try {
            XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
            InputStream stream = new URL(url).openStream();
            xpp.setInput(stream, null);

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("localtime")) {
                    xpp.next();
                    weather.date = xpp.getText();
                }
                if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("current_condition")) {
                    while (!(xpp.getEventType() == XmlPullParser.END_TAG && xpp.getName().equals("current_condition"))) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("temp_C")) {
                            xpp.next();
                            weather.tempr = xpp.getText();
                        }
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("humidity")) {
                            xpp.next();
                            weather.humidity = xpp.getText();
                        }
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("windspeedKmph")) {
                            xpp.next();
                            weather.wind = xpp.getText();
                        }
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("weatherCode")) {
                            xpp.next();
                            weather.type = xpp.getText();
                        }
                        xpp.next();
                    }
                }
                if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("weather")) {
                    Weather5Days w5d = new Weather5Days();
                    w5d.city = weather.city;
                    while (!(xpp.getEventType() == XmlPullParser.END_TAG && xpp.getName().equals("weather"))) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("date")) {
                            xpp.next();
                            w5d.date = xpp.getText();
                        }
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("mintempC")) {
                            xpp.next();
                            w5d.mi = xpp.getText();
                        }
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("maxtempC")) {
                            xpp.next();
                            w5d.ma = xpp.getText();
                        }
                        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("weatherCode")) {
                            xpp.next();
                            w5d.type = xpp.getText();
                        }
                        xpp.next();
                    }
                    weather.weather5Days.add(w5d);
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getContentResolver().delete(WeatherContentProvider.WEATHER1_URI, DBWeather.CITY1 + " = \"" + weather.city + "\"", null);
        getContentResolver().delete(WeatherContentProvider.WEATHER2_URI, DBWeather.CITY2 + " = \"" + weather.city + "\"", null);
        ContentValues cv = new ContentValues();
        cv.put(DBWeather.CITY1, weather.city);
        cv.put(DBWeather.DATE1, weather.date);
        cv.put(DBWeather.HUMIDITY1, weather.humidity);
        cv.put(DBWeather.TEMPR1, weather.tempr);
        cv.put(DBWeather.WIND1, weather.wind);
        cv.put(DBWeather.WEATHER_TYPE1, weather.type);
        getContentResolver().insert(WeatherContentProvider.WEATHER1_URI, cv);
        for (int i = 0; i < weather.weather5Days.size(); i++) {
            ContentValues cv2 = new ContentValues();
            cv2.put(DBWeather.CITY2, weather.weather5Days.get(i).city);
            cv2.put(DBWeather.DATE2, weather.weather5Days.get(i).date);
            cv2.put(DBWeather.TEMPR_MIN2, weather.weather5Days.get(i).mi);
            cv2.put(DBWeather.TEMPR_MAX2, weather.weather5Days.get(i).ma);
            cv2.put(DBWeather.WEATHER_TYPE2, weather.weather5Days.get(i).type);
            getContentResolver().insert(WeatherContentProvider.WEATHER2_URI, cv2);
        }
        Log.d("debug1", "WeatherDownloadService finished for city " + weather.city);
        Intent response = new Intent();
        response.setAction(ACTION_RESPONSE);
        response.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(response);
    }
}

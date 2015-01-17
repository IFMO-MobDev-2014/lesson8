package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.db.WeatherDBHelper;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class WeatherIntentService extends IntentService {
    public WeatherIntentService() {
        super("WeatherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        receiver.send(AppResultsReceiver.STATUS_RUNNING, Bundle.EMPTY);

        String cityName = intent.getStringExtra("cityName");
        String woeid = intent.getStringExtra("woeid");
        String update = intent.getStringExtra("update");
        boolean all = intent.getBooleanExtra("all", false);

        if (all) {
            Cursor cursor = getContentResolver().query(WeatherContentProvider.CITY_CONTENT_URL, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex("city_name"));
                    String id = cursor.getString(cursor.getColumnIndex("woeid"));
                    updateCity(name, id, "update", receiver);
                } while(cursor.moveToNext());
            }
        } else {
            updateCity(cityName, woeid, update, receiver);
        }
    }

    public void updateCity(String cityName, String woeid, String update, ResultReceiver receiver) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL("http://weather.yahooapis.com/forecastrss?w=" + woeid + "&u=c");
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
            WeatherHandler myWeatherHandler = new WeatherHandler();
            myXMLReader.setContentHandler(myWeatherHandler);
            InputSource myInputSource = new InputSource(new StringReader(content.toString()));
            myXMLReader.parse(myInputSource);

            Weather weather = new Weather(myWeatherHandler.getWeather());

            ContentValues nowValues = new ContentValues();
            ContentValues firstDayValues = new ContentValues();
            ContentValues secondDayValues = new ContentValues();
            ContentValues thirdDayValues = new ContentValues();
            ContentValues fourthDayValues = new ContentValues();
            ContentValues fifthDayValues = new ContentValues();

            nowValues.put(WeatherDBHelper.COLUMN_NAME_TEMP, weather.getNow().getTemp());
            nowValues.put(WeatherDBHelper.COLUMN_NAME_CONDITION, weather.getNow().getCondition());
            nowValues.put(WeatherDBHelper.COLUMN_NAME_WIND_SPEED, weather.getNow().getWindSpeed());
            nowValues.put(WeatherDBHelper.COLUMN_NAME_HUMIDITY, weather.getNow().getHumidity());
            nowValues.put(WeatherDBHelper.COLUMN_NAME_CONDITION_CODE, weather.getNow().getConditionCode());
            nowValues.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);

            firstDayValues.put(WeatherDBHelper.COLUMN_NAME_DAY_NAME, weather.getFirstDay().getName());
            firstDayValues.put(WeatherDBHelper.COLUMN_NAME_CONDITION_CODE, weather.getFirstDay().getConditionCode());
            firstDayValues.put(WeatherDBHelper.COLUMN_NAME_FORECAST, weather.getFirstDay().getForecast());
            firstDayValues.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);

            secondDayValues.put(WeatherDBHelper.COLUMN_NAME_DAY_NAME, weather.getSecondDay().getName());
            secondDayValues.put(WeatherDBHelper.COLUMN_NAME_CONDITION_CODE, weather.getSecondDay().getConditionCode());
            secondDayValues.put(WeatherDBHelper.COLUMN_NAME_FORECAST, weather.getSecondDay().getForecast());
            secondDayValues.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);

            thirdDayValues.put(WeatherDBHelper.COLUMN_NAME_DAY_NAME, weather.getThirdDay().getName());
            thirdDayValues.put(WeatherDBHelper.COLUMN_NAME_CONDITION_CODE, weather.getThirdDay().getConditionCode());
            thirdDayValues.put(WeatherDBHelper.COLUMN_NAME_FORECAST, weather.getThirdDay().getForecast());
            thirdDayValues.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);

            fourthDayValues.put(WeatherDBHelper.COLUMN_NAME_DAY_NAME, weather.getFourthDay().getName());
            fourthDayValues.put(WeatherDBHelper.COLUMN_NAME_CONDITION_CODE, weather.getFourthDay().getConditionCode());
            fourthDayValues.put(WeatherDBHelper.COLUMN_NAME_FORECAST, weather.getFourthDay().getForecast());
            fourthDayValues.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);

            fifthDayValues.put(WeatherDBHelper.COLUMN_NAME_DAY_NAME, weather.getFifthDay().getName());
            fifthDayValues.put(WeatherDBHelper.COLUMN_NAME_CONDITION_CODE, weather.getFifthDay().getConditionCode());
            fifthDayValues.put(WeatherDBHelper.COLUMN_NAME_FORECAST, weather.getFifthDay().getForecast());
            fifthDayValues.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);

            boolean exists = false;

            if (getContentResolver().query(WeatherContentProvider.FORECAST_CONTENT_URL, null,
                    WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {woeid}, null).getCount() > 0) {
                exists = true;
            }

            if (update.equals("update")) {
                getContentResolver().delete(WeatherContentProvider.NOW_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {woeid});
                getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {woeid});
                getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {woeid});
                getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {woeid});
                getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {woeid});
                getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {woeid});

                getContentResolver().insert(WeatherContentProvider.NOW_CONTENT_URL, nowValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, firstDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, secondDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, thirdDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, fourthDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, fifthDayValues);

            } else if (update.equals("add") || update.equals("add_gps")) {
                if (!exists) {
                    ContentValues values = new ContentValues();
                    values.put(WeatherDBHelper.COLUMN_NAME_CITY_NAME, cityName);
                    values.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);
                    if (update.equals("add_gps")) {
                        values.put(WeatherDBHelper.COLUMN_NAME_GPS, "gps");
                    }
                    getContentResolver().insert(WeatherContentProvider.CITY_CONTENT_URL, values);

                    getContentResolver().insert(WeatherContentProvider.NOW_CONTENT_URL, nowValues);
                    getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, firstDayValues);
                    getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, secondDayValues);
                    getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, thirdDayValues);
                    getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, fourthDayValues);
                    getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, fifthDayValues);

                } else {
                    receiver.send(AppResultsReceiver.STATUS_ALREADY_ADDED, Bundle.EMPTY);
                }
            } else if (update.equals("delete and add")) {
                String currentGpsWoeid;
                Cursor cursor = getContentResolver().query(WeatherContentProvider.CITY_CONTENT_URL, null, WeatherDBHelper.COLUMN_NAME_GPS + "=?", new String[]{"gps"}, null);
                if (cursor.moveToFirst()) {
                    currentGpsWoeid = cursor.getString(cursor.getColumnIndex("woeid"));

                    getContentResolver().delete(WeatherContentProvider.CITY_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{currentGpsWoeid});
                    getContentResolver().delete(WeatherContentProvider.NOW_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{currentGpsWoeid});
                    getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{currentGpsWoeid});
                    getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{currentGpsWoeid});
                    getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{currentGpsWoeid});
                    getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{currentGpsWoeid});
                    getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{currentGpsWoeid});

                    cursor.close();
                }
                ContentValues values = new ContentValues();
                values.put(WeatherDBHelper.COLUMN_NAME_CITY_NAME, cityName);
                values.put(WeatherDBHelper.COLUMN_NAME_WOEID, woeid);
                values.put(WeatherDBHelper.COLUMN_NAME_GPS, "gps");
                getContentResolver().insert(WeatherContentProvider.CITY_CONTENT_URL, values);

                getContentResolver().insert(WeatherContentProvider.NOW_CONTENT_URL, nowValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, firstDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, secondDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, thirdDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, fourthDayValues);
                getContentResolver().insert(WeatherContentProvider.FORECAST_CONTENT_URL, fifthDayValues);
            }

            if (update.equals("update")) {
                receiver.send(AppResultsReceiver.STATUS_REFRESHED, Bundle.EMPTY);
            } else if (update.equals("delete and add")) {
                receiver.send(AppResultsReceiver.STATUS_DELETE_AND_ADD_REFRESHED, Bundle.EMPTY);
            } else {
                receiver.send(AppResultsReceiver.STATUS_ADDED, Bundle.EMPTY);
            }
        } catch (SAXException | ParserConfigurationException e) {
            receiver.send(AppResultsReceiver.STATUS_PARSE_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        } catch (IOException e) {
            receiver.send(AppResultsReceiver.STATUS_INTERNET_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        }
    }
}

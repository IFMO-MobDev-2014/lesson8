package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import net.aksingh.java.api.owm.CurrentWeatherData;
import net.aksingh.java.api.owm.DailyForecastData;
import net.aksingh.java.api.owm.OpenWeatherMap;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ifmo.md.lesson8.provider.WeatherDatabaseHelper;
import ru.ifmo.md.lesson8.provider.WeatherProvider;

/**
 * Created by pva701 on 24.11.14.
 */
public class NetworkLoaderService extends IntentService {
    public static final float T = (float)ShortWeatherData.T;
    public static final int INTERVAL_BETWEEN_UPDATES = 20 * 60;
    public static final byte NUMBER_DAYS_OF_FORECAST = 10;
    public static final int DATABASE_NOT_UPDATED = 0;
    public static final int DATABASE_UPDATED = 1;
    public static final int ALREADY_UPDATED = 2;
    public static final int UPDATING_STARTED = 3;
    public static final int GET_CITY_NAME = 4;

    public static final String TAG = "NetworkLoaderService";

    public NetworkLoaderService() {
        super(TAG);
    }
    public static final String CITY_NAME = "city_name";
    public static final String ALL_CITIES = "ALL_CITIES";
    public static final String GET_CITY = "GET_CITY";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String LOAD_INTERVAL = "load_interval";

    public static OpenWeatherMap owmClient;
    public static final String APP_ID = "1ae017d145aacb33811eef8f8911e804";
    public static ArrayList <Handler> handlers = new ArrayList<Handler>();
    private static ArrayList <String> queue = new ArrayList<String>();

    public static void setServiceAlarm(Context context, boolean isOn, Bundle bundle) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            int pollInterval = bundle.getInt(LOAD_INTERVAL);
            Intent intent = new Intent(context, NetworkLoaderService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + pollInterval, pollInterval, pi);
        } else if (isServiceAlarmOn(context)) {
            Intent intent = new Intent(context, NetworkLoaderService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent intent = new Intent(context, NetworkLoaderService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public static void loadCity(Context context, String name) {
        context.startService(new Intent(context, NetworkLoaderService.class).putExtra(CITY_NAME, name));
    }

    public static void getCityNameByCoord(Context context, double lon, double lat) {
        context.startService(new Intent(context, NetworkLoaderService.class).
                putExtra(CITY_NAME, GET_CITY).
                putExtra(LAT, lat).
                putExtra(LON, lon));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        String cityName = intent.getStringExtra(CITY_NAME);
        for (String e : queue)
            if (e.equals(cityName) || e.equals(ALL_CITIES))
                return;
        queue.add(cityName);
        super.onStart(intent, startId);
    }

    public static boolean isLoading(String cityName) {
        for (String e : queue)
            if (e.equals(cityName) || e.equals(ALL_CITIES))
                return true;
        return false;
    }

    public static void addHandler(Handler h) {
        handlers.add(h);
    }

    public static void removeHandler(Handler h) {
        for (int i = 0; i < handlers.size(); ++i)
            if (handlers.get(i).equals(h)) {
                handlers.remove(i);
                break;
            }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String city = intent.getStringExtra(CITY_NAME);
        if (owmClient == null)
            owmClient = new OpenWeatherMap(OpenWeatherMap.OWM_URL.PARAMETER_UNITS_VALUE_METRIC, APP_ID);
        if (city.equals(GET_CITY)) {
            double lat = intent.getDoubleExtra(LAT, 0);
            double lon = intent.getDoubleExtra(LON, 0);
            //Log.i("netwLoadServ", "get city : lon = " + lon + " lat = ")
            try {
                CurrentWeatherData current = owmClient.currentWeatherByCoordinates((float) lat, (float)lon);
                cityNameSend(current.getCityName());
            } catch (Exception e) {
                Log.i("Network", "Exception get city");
            }

        } else if (city.equals(ALL_CITIES)) {
            Cursor allCities = getApplication().getContentResolver().
                    query(WeatherProvider.CITY_CONTENT_URI, null, null, null, null);
            while (allCities.moveToNext()) {
                City c = WeatherDatabaseHelper.CityCursor.getCity(allCities);
                try {
                    updatingStartedSend(c.getId());
                    ArrayList<ShortWeatherData> daily = loadOneCity(c);
                    boolean updated = updateDatabase(c, daily);
                    updatingFinishedSend(c.getId(), updated);
                } catch (Exception e) {}
            }
            queue.remove(0);
        } else {
            try {
                Cursor cursor = getApplicationContext().getContentResolver().
                        query(WeatherProvider.CITY_CONTENT_URI, null,
                        WeatherDatabaseHelper.CITY_NAME + " = '" + city + "'", null, null);
                cursor.moveToNext();
                if (cursor.isAfterLast()) {
                    queue.remove(0);
                    return;
                }
                City c = WeatherDatabaseHelper.CityCursor.getCity(cursor);
                if (isAlreadyUpdated(c)) {
                    alreadyUpdatedSend(c.getId());
                    queue.remove(0);
                    return;
                }
                updatingStartedSend(c.getId());
                ArrayList <ShortWeatherData> daily = loadOneCity(c);
                boolean updated = updateDatabase(c, daily);
                updatingFinishedSend(c.getId(), updated);
                } catch (IOException e) {
                Log.i("NetworkLoader", "IOException");
            } catch (JSONException e) {
                Log.i("NetworkLoader", "JSONException");
            }
        }
        queue.remove(0);
    }

    private void cityNameSend(String name) {
        for (Handler handler : handlers) {
            Message message = handler.obtainMessage(GET_CITY_NAME);
            message.obj = name;
            message.sendToTarget();
        }
    }

    private void updatingStartedSend(int id) {
        for (Handler handler : handlers) {
            Message message = handler.obtainMessage(UPDATING_STARTED);
            message.arg1 = id;
            message.sendToTarget();
        }
    }

    private void updatingFinishedSend(int id, boolean upd) {
        for (Handler handler : handlers) {
            Message message = handler.obtainMessage((upd ? DATABASE_UPDATED : DATABASE_NOT_UPDATED));
            message.arg1 = id;
            message.sendToTarget();
        }
    }

    private void alreadyUpdatedSend(int id) {
        for (Handler handler : handlers) {
            Message message = handler.obtainMessage(ALREADY_UPDATED);
            message.arg1 = id;
            message.sendToTarget();
        }
    }

    private boolean isAlreadyUpdated(City c) {
        Date curDate = new Date();
        return curDate.getTime() / 1000 <= 1L * (c.getLastUpdate() + INTERVAL_BETWEEN_UPDATES);
    }

    private ArrayList<ShortWeatherData> loadOneCity(City city) throws IOException, JSONException {
        String name = city.getName();
        int cityId = city.getId();
        if (!isAlreadyUpdated(city)) {
            CurrentWeatherData current = owmClient.currentWeatherByCityName(name);
            ArrayList <ShortWeatherData> newForecast = new ArrayList<ShortWeatherData>();
            DailyForecastData dailyForecastData = owmClient.dailyForecastByCityName(name, NUMBER_DAYS_OF_FORECAST);
            List<DailyForecastData.Forecast> list = dailyForecastData.getForecast_List();
            DailyForecastData.Forecast forecastToday = list.get(0);
            newForecast.add(new ShortWeatherData(
                    (int)(current.getMainData_Object().getMinTemperature() - T),
                    (int)(forecastToday.getTemperature_Object().getMaximumTemperature() - T),
                    (int)(current.getMainData_Object().getTemperature() - T),
                    (int)current.getWind_Object().getWindSpeed(),
                    (int)current.getMainData_Object().getHumidity(),
                    current.getWeather_List().get(0).getWeatherName(),
                    current.getWeather_List().get(0).getWeatherDescription(),
                    current.getWeather_List().get(0).getWeatherIconName(),
                    (int)(current.getDateTime().getTime() / 1000),
                    current.getWeather_List().get(0).getWeatherCode(),
                    cityId));
            for (int i = 1; i < list.size(); ++i)
                newForecast.add(new ShortWeatherData(list.get(i), cityId));
            return newForecast;
        }
        return null;
    }

    private boolean updateDatabase(City c, ArrayList <ShortWeatherData> daily) {
        if (daily == null || daily.size() == 0)
            return false;
        getContentResolver().delete(WeatherProvider.FORECAST_CONTENT_URI, WeatherDatabaseHelper.FORECAST_CITY_ID + " = " + c.getId(), null);
        for (int i = 0; i < daily.size(); ++i) {
            ContentValues cv = new ContentValues();
            ShortWeatherData cur = daily.get(i);
            cv.put(WeatherDatabaseHelper.FORECAST_WEATHER_MAIN, cur.getWeatherMain());
            cv.put(WeatherDatabaseHelper.FORECAST_WEATHER_DESCRIPTION, cur.getWeatherDescription());
            cv.put(WeatherDatabaseHelper.FORECAST_ICON, cur.getIcon());
            cv.put(WeatherDatabaseHelper.FORECAST_TEMP, cur.getTemp());
            cv.put(WeatherDatabaseHelper.FORECAST_TEMP_MIN, cur.getTempMin());
            cv.put(WeatherDatabaseHelper.FORECAST_TEMP_MAX, cur.getTempMax());
            cv.put(WeatherDatabaseHelper.FORECAST_CITY_ID, cur.getCityId());
            cv.put(WeatherDatabaseHelper.FORECAST_CONDITION_CODE, cur.getConditionCode());
            cv.put(WeatherDatabaseHelper.FORECAST_WIND_SPEED, cur.getWindSpeed());
            cv.put(WeatherDatabaseHelper.FORECAST_HUMIDITY, cur.getHumidity());
            cv.put(WeatherDatabaseHelper.FORECAST_DATE, (int)(cur.getDate().getTime() / 1000));
            getContentResolver().insert(WeatherProvider.FORECAST_CONTENT_URI, cv);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherDatabaseHelper.CITY_LAST_UPDATE, (int)(new Date().getTime() / 1000));
        getContentResolver().update(WeatherProvider.CITY_CONTENT_URI, contentValues, WeatherDatabaseHelper.CITY_ID + " = " + c.getId(), null);
        return true;
    }
}

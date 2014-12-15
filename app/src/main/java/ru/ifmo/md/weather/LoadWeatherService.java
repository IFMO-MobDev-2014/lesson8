package ru.ifmo.md.weather;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import ru.ifmo.md.weather.db.WeatherContentProvider;
import ru.ifmo.md.weather.db.model.City;
import ru.ifmo.md.weather.db.model.CityTable;
import ru.ifmo.md.weather.db.model.Weather;
import ru.ifmo.md.weather.db.model.WeatherTable;


/**
 * Created by Kirill on 01.12.2014.
 */
public class LoadWeatherService extends IntentService {

    public static final String REQUEST_TYPE = "type";
    public static final String FORCE_UPDATE = "forceUpdate";
    public static final String COUNTRY_INDEX = "index";
    public static final int FORECAST_REQUEST = 0;
    public static final int WEATHER_REQUEST = 1;
    public static final int UPDATE_ALL_REQUEST = 2;
    public static final String URLS = "urls";
    public static final String RESULT = "result";
    public static final String ERROR_MSG = "errorMessage";
    public static final String NOTIFICATION = "ru.ifmo.md.weather";

    Context context = null;

    public LoadWeatherService() {
        super("LoadWeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.i("start ", "onHandleIntent");
            int type = intent.getIntExtra(REQUEST_TYPE, -1);
            boolean force = intent.getBooleanExtra(FORCE_UPDATE, false);
            Log.i("force", force+"");
            SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(this);
            if (!force) {
                long lastUpdate = Long.parseLong(settingsPref.getString(getResources().getString(R.string.settings_last_update), "0"));
                long interval = Long.parseLong(settingsPref.getString(getResources().getString(R.string.settings_update_interval), "1800"));
                long now = System.currentTimeMillis() / 1000L;
                Log.i("lastUpdate", lastUpdate+"");
                Log.i("interval", interval+"");
                Log.i("now", now+"");
                if (now - lastUpdate < interval) {
                    publishResults("Weather is up to date", 1);
                    return ;
                }
            }
            HashMap<Long, String> cities = getCitiesFromContentProvider();
            System.out.println("Cities:");
            for (Entry<Long, String> i : cities.entrySet()) {
                System.out.println(i.getKey() + " : " + i.getValue());
            }
            boolean isOnLine = WeatherDownloader.isOnline(this);
            Log.i("isOnLine:", Boolean.toString(isOnLine));
            if (isOnLine) {
                if (type == WEATHER_REQUEST) {
                    HashMap<Long, City> loadedWeather = loadWeather(cities);
                    updateWeather(loadedWeather);
                } else if (type == FORECAST_REQUEST) {
                    ArrayList<Weather> loadedForecast = loadForecast(cities);
                    updateForecast(loadedForecast);
                } else if (type == UPDATE_ALL_REQUEST) {
                    HashMap<Long, City> loadedWeather = loadWeather(cities);
                    updateWeather(loadedWeather);
                    ArrayList<Weather> loadedForecast = loadForecast(cities);
                    updateForecast(loadedForecast);

                    String time = String.valueOf((System.currentTimeMillis()/1000L));
                    SharedPreferences.Editor editor = settingsPref.edit();
                    editor.putString(getResources().getString(R.string.settings_last_update), time);
                    editor.apply();
                }
            } else {
                publishResults("Internet connection error", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            publishResults(e.getMessage(), 0);
        }
        publishResults("Download finished", 1);
    }

    private ArrayList<Weather> loadForecast(HashMap<Long, String> cities) {
        ArrayList<Weather> forecast = new ArrayList<>();
        for(Entry<Long, String> entry : cities.entrySet()) {
            Long cityId = entry.getKey();
            String cityName = entry.getValue();
            String data = WeatherDownloader.loadForecastForFiveDays(cityName);
            try {
                JSONObject root = new JSONObject(data);
                int code = root.getInt("cod");
                if (code != 200) {
                    publishResults("Download error", 0);
                    return null;
                } else {
                    int cnt = root.getInt("cnt");
                    JSONArray array = root.getJSONArray("list");
                    for (int j = 0; j < cnt; j++) {
                        JSONObject obj = array.getJSONObject(j);
                        Weather curr = getWeatherFromJSON(obj);
                        curr.setCityId(cityId);
                        forecast.add(curr);
                    }
                    Log.i("get : ", forecast.size() + " forecasts");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                publishResults("JSON parsing error", 0);
                return null;
            }
        }
        return forecast;
    }

    private Long updateForecast(ArrayList<Weather> forecast) {
        ContentResolver cr = getContentResolver();
        Uri uri = Uri.parse(WeatherContentProvider.CONTENT_URI_WEATHER + "");
        int r = cr.delete(uri, null, null);
        Log.i("", "from table Weather, delete " + r + " lines");
        for (Weather w : forecast) {
            if (Long.parseLong(w.getReceivingTime()) >= System.currentTimeMillis() / 1000L) {
                cr.insert(WeatherContentProvider.CONTENT_URI_WEATHER, getWeatherValues(w));
            }
        }

        return 1L;//Long.parseLong(uri.getLastPathSegment());
    }

    private HashMap<Long, City> loadWeather(HashMap<Long, String> cities) {
        HashMap<Long, City> rv = new HashMap<Long, City>();

        for(Entry<Long, String> entry : cities.entrySet()) {
            Long cityId = entry.getKey();
            String cityName = entry.getValue();
            String data = WeatherDownloader.loadWeatherForNow(cityName);
            Log.i("data", data);
            try {
                JSONObject root = new JSONObject(data);
                int code = root.getInt("cod");
                if (code != 200) {
                    publishResults("Download error", 0);
                    return null;
                }

                /*JSONArray array = root.getJSONArray("list");
                if (array.length() != 1) {
                    publishResults("Parsing Error(more than one result found", 0);
                    return null;
                }*/
                Weather weather = getWeatherFromJSON(root);

                City currentCity = new City();
                currentCity.setName(cityName);
                currentCity.setId(root.getString("id"));
                currentCity.setWeatherNow(weather);

                rv.put(cityId, currentCity);
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("JSON error");
                publishResults("JSON parsing error", 0);
                return null;
            }

        }
        return rv;
    }

    private Long updateWeather(HashMap<Long, City> cities) {
        Uri uri = null;
        Log.i("updateWeather", "start");
        ContentResolver cr = getContentResolver();
        //Cursor cursor = cr.query(WeatherContentProvider.CONTENT_URI_CITIES, null, null, null, null);
        for(Entry<Long, City> entry : cities.entrySet()) {
            City currCity = entry.getValue();
            long cityId = entry.getKey();
            Log.i("city:", currCity.toString());
            uri = Uri.withAppendedPath(WeatherContentProvider.CONTENT_URI_CITIES, cityId+"");
            getContentResolver().update(uri, getCityValues(currCity), null, null);
        }
        if (uri == null)
            return 0L;
        else
            return Long.parseLong(uri.getLastPathSegment());
    }

    private HashMap<Long, String> getCitiesFromContentProvider() {
        HashMap<Long, String> rv = new HashMap<>();
        Uri uri = Uri.parse(WeatherContentProvider.CONTENT_URI_CITIES + "");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(CityTable._ID));
            String name = cursor.getString(cursor.getColumnIndex(CityTable.NAME_COLUMN));
            rv.put(id, name);
        }
        return rv;
    }

    private ContentValues getWeatherValues(Weather weather) {
        ContentValues values = new ContentValues();
        values.put(WeatherTable.CITY_NAME_COLUMN, weather.getCityName());
        values.put(WeatherTable.WEATHER_TIME_COLUMN, weather.getReceivingTime());
        values.put(WeatherTable.TEMP_COLUMN, weather.getTemp());
        values.put(WeatherTable.TEMP_MIN_COLUMN, weather.getTempMin());
        values.put(WeatherTable.TEMP_MAX_COLUMN, weather.getTempMax());
        values.put(WeatherTable.PRESSURE_COLUMN, weather.getPressure());
        values.put(WeatherTable.HUMIDITY_COLUMN, weather.getHumidity());
        values.put(WeatherTable.WIND_SPEED_COLUMN, weather.getWindSpeed());
        values.put(WeatherTable.ICON_NAME_COLUMN, weather.getIconName());
        values.put(WeatherTable.DESCRIPTION_COLUMN, weather.getDescription());
        values.put(WeatherTable.CITY_ID_COLUMN, weather.getCityId());
        return values;
    }

    private ContentValues getCityValues(City city) {
        ContentValues values = getWeatherValues(city.getWeatherNow());
        values.put(CityTable.NAME_COLUMN, city.getName());
        values.put(CityTable.ID_COLUMN, city.getId());
        values.put(CityTable.COUNTRY_COLUMN, city.getCountry());
        values.put(CityTable.LAT_COLUMN, city.getLat());
        values.put(CityTable.LON_COLUMN, city.getLon());
        return values;
    }

    private Weather getWeatherFromJSON(JSONObject JSONObj) throws JSONException{
        Weather rv = new Weather();
        JSONObject main = JSONObj.getJSONObject("main");
        rv.setReceivingTime(Integer.toString(JSONObj.getInt("dt")));
        rv.setTemp(main.getInt("temp"));
        rv.setTempMin(main.getInt("temp_min"));
        rv.setTempMax(main.getInt("temp_max"));
        rv.setPressure(main.getDouble("pressure"));
        rv.setHumidity(main.getDouble("humidity"));
        JSONObject wind = JSONObj.getJSONObject("wind");
        rv.setWindSpeed(wind.getDouble("speed"));
        JSONArray humanWeatherArray = JSONObj.getJSONArray("weather");
        JSONObject humanInf =  humanWeatherArray.getJSONObject(0);
        rv.setIconName(humanInf.getString("icon"));
        rv.setDescription(humanInf.getString("description"));
        return rv;
    }

    private void publishResults(String errorMsg, int result) {
        //Toast.makeText(this, "finish downloading", Toast.LENGTH_SHORT).show();
        Log.i("finish downloading, result:", errorMsg);
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        intent.putExtra(ERROR_MSG, errorMsg);
        sendBroadcast(intent);
    }
}

package ru.ifmo.md.weather;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.ifmo.md.weather.db.*;
import ru.ifmo.md.weather.db.model.*;


/**
 * Created by Kirill on 01.12.2014.
 */
public class LoadWeatherService extends IntentService {

    public static final String REQUEST_TYPE = "type";
    public static final String COUNTRY_INDEX = "index";
    public static final int FORECAST_REQUEST = 1;
    public static final int WEATHER_REQUEST = 0;
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
            int type = intent.getIntExtra(REQUEST_TYPE, -1);
            //countryIndex = intent.getIntExtra(COUNTRY_INDEX, -1);
            ArrayList<String> cities = intent.getStringArrayListExtra(URLS);
            if (WeatherDownloader.isOnline(this)) {
                if (type == FORECAST_REQUEST) {

                } else if (type == WEATHER_REQUEST) {

                }
            } else {
                publishResults("Internet connection error", 0);
            }
        } catch (Exception e) {
            publishResults(e.getMessage(), 0);
        }
    }

    private boolean loadForecast(ArrayList<City> cities) {

        for (int i = 0; i < cities.size(); i++) {
            City currentCity = cities.get(i);
            String cityName = currentCity.getName();
            String data = WeatherDownloader.loadForecastForFiveDays(cityName);
            try {
                JSONObject root = new JSONObject(data);
                int code = root.getInt("cod");
                if (code != 200) {
                    publishResults("Download error", 0);
                    return false;
                } else {
                    int cnt = root.getInt("cnt");
                    JSONArray array = root.getJSONArray("list");
                    ArrayList<Weather> forecast = new ArrayList<Weather>();
                    for (int j = 0; j < cnt; j++) {
                        JSONObject obj = array.getJSONObject(j);
                        forecast.add(getWeatherFromJSON(obj));
                    }
                    System.out.println("get :" + forecast.size() + " forecasts");

                    for (Weather w : forecast) {
                        ContentValues values = addWeatherValues(w);
                        Uri id = getContentResolver().insert(WeatherContentProvider.CONTENT_URI_WEATHER, values);
                    }
                    System.out.println("load done");
                    publishResults("", 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("JSON error");
                publishResults("JSON parsing error", 0);
                return false;
            }

        }
        return true;
    }

    private boolean loadWeather(ArrayList<City> cities) {

        for (int i = 0; i < cities.size(); i++) {
            City currentCity = cities.get(i);
            String cityName = currentCity.getName();
            String data = WeatherDownloader.loadWeatherForNow(cityName);
            try {
                JSONObject root = new JSONObject(data);
                int code = root.getInt("cod");
                if (code != 200) {
                    publishResults("Download error", 0);
                    return false;
                }
                int cnt = root.getInt("count");
                JSONArray array = root.getJSONArray("list");
                if (array.length() != 1) {
                    publishResults("Parsing Error(more than one result found", 0);
                    return false;
                }
                Weather weather = getWeatherFromJSON(array.getJSONObject(0));
                currentCity.setId(array.getJSONObject(0).getString("id"));
                currentCity.setWeatherNow(weather);
                cities.set(i,currentCity);
                ContentValues values = addWeatherValues(weather);
                values.put(CityTable.COUNTRY_COLUMN, currentCity.getCountry());
                values.put(CityTable.LAT_COLUMN, currentCity.getLat());
                values.put(CityTable.LON_COLUMN, currentCity.getLon());
                values.put(CityTable.NAME_COLUMN, currentCity.getName());
                values.put(CityTable.ID_COLUMN, currentCity.getId());

                Uri uri = Uri.parse(WeatherContentProvider.CONTENT_URI_CITIES + "/" + currentCity.getDbId());
                int r = getContentResolver().delete(uri, null, null);
                System.out.println("delete "+r+ " lines");
                Uri id = getContentResolver().insert(WeatherContentProvider.CONTENT_URI_CITIES, values);


            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("JSON error");
                publishResults("JSON parsing error", 0);
                return false;
            }

        }
        return true;
    }

    private ContentValues addWeatherValues(Weather weather) {
        ContentValues values = new ContentValues();
        values.put(WeatherTable.CITY_NAME_COLUMN, weather.getCityName());
        values.put(WeatherTable.TEMP_COLUMN, weather.getTemp());
        values.put(WeatherTable.TEMP_MIN_COLUMN, weather.getTempMin());
        values.put(WeatherTable.TEMP_MAX_COLUMN, weather.getTempMax());
        values.put(WeatherTable.PRESSURE_COLUMN, weather.getPressure());
        values.put(WeatherTable.HUMIDITY_COLUMN, weather.getHumidity());
        values.put(WeatherTable.WIND_SPEED_COLUMN, weather.getWindSpeed());
        values.put(WeatherTable.ICON_NAME_COLUMN, weather.getIconName());
        values.put(WeatherTable.DESCRIPTION_COLUMN, weather.getDescription());
        values.put(WeatherTable.CITY_ID_COLUMN, 0);
        return values;
    }

    private Weather getWeatherFromJSON(JSONObject JSONObj) throws JSONException{
        Weather rv = new Weather();
        JSONObject main = JSONObj.getJSONObject("main");
        rv.setReceivingTime(Integer.toString(JSONObj.getInt("dt")));
        rv.setTemp(main.getDouble("temp"));
        rv.setTempMin(main.getDouble("temp_min"));
        rv.setTempMax(main.getDouble("temp_max"));
        rv.setPressure(main.getDouble("pressure"));
        rv.setHumidity(main.getDouble("humidity"));
        JSONObject wind = JSONObj.getJSONObject("wind");
        rv.setWindSpeed(wind.getDouble("speed"));
        JSONObject humanInf = JSONObj.getJSONObject("weather");
        rv.setIconName(humanInf.getString("icon"));
        rv.setDescription(humanInf.getString("description"));
        return rv;
    }

    private void publishResults(String errorMsg, int result) {
        Toast.makeText(this, "finish downloading", Toast.LENGTH_SHORT).show();
        System.out.println("finish downloading");
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        intent.putExtra(ERROR_MSG, errorMsg);
        sendBroadcast(intent);
    }
}

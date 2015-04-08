package ru.ifmo.md.lesson8;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchWeatherTask {
    public static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    public static final String OWM_LIST = "list";
    public static final String OWM_WEATHER = "weather";
    public static final String OWM_TEMPERATURE = "temp";
    public static final String OWM_MAX = "max";
    public static final String OWM_MIN = "min";
    public static final String OWM_DAY = "day";
    public static final String OWM_PRESSURE = "pressure";
    public static final String OWM_HUMIDITY = "humidity";
    public static final String OWM_SPEED = "speed";
    public static final String OWM_DATETIME = "dt";
    public static final String OWM_WEATHER_MAIN = "main";
    public static final String OWM_CITY = "city";
    public static final String OWM_CITY_NAME = "name";

    private static WeatherData[] getWeatherDataFromJson(String forecastJsonStr, City city) throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        WeatherData[] weatherData = new WeatherData[weatherArray.length() - 1];
        for (int i = 1; i < weatherArray.length(); i++) {
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            long dateTime = dayForecast.getLong(OWM_DATETIME);

            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            String weatherMain = weatherObject.getString(OWM_WEATHER_MAIN);
            Log.d(LOG_TAG, weatherMain);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            int max = (int) Math.round(temperatureObject.getDouble(OWM_MAX));
            int min = (int) Math.round(temperatureObject.getDouble(OWM_MIN));
            int day = (int) Math.round(temperatureObject.getDouble(OWM_DAY));
            int pressure = dayForecast.getInt(OWM_PRESSURE);
            int humidity = dayForecast.getInt(OWM_HUMIDITY);
            int speed = dayForecast.getInt(OWM_SPEED);

            weatherData[i - 1] = new WeatherData(min, max, day, speed, humidity, pressure,
                    dateTime * 1000L, city.getId(), weatherMain);
        }
        return weatherData;
    }

    public static WeatherData[] fetch(City city, int numDays, String apiKey) throws JSONException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJSONStr = null;
        String format = "json";
        String units = "metric";
        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APP_ID_PARAM = "APPID";
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, city.getName()).appendQueryParameter(FORMAT_PARAM, format).appendQueryParameter(UNITS_PARAM, units).appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).appendQueryParameter(APP_ID_PARAM, apiKey).build();
            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            forecastJSONStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            forecastJSONStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return getWeatherDataFromJson(forecastJSONStr, city);
    }

    private static City getCityFromJSON(String json) throws JSONException {
        JSONObject forecastJson = new JSONObject(json);
        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        return new City(-1, cityJson.getString(OWM_CITY_NAME), 0);
    }

    public static City fetchCity(double lat, double lon, String apiKey) throws JSONException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJSONStr = null;
        String format = "json";
        String units = "metric";
        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String LAT_PARAM = "lat";
            final String LON_PARAM = "lon";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String APP_ID_PARAM = "APPID";
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(LAT_PARAM, Double.toString(lat))
                    .appendQueryParameter(LON_PARAM, Double.toString(lon))
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(APP_ID_PARAM, apiKey).build();
            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            forecastJSONStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            forecastJSONStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return getCityFromJSON(forecastJSONStr);
    }
}
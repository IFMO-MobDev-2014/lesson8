package com.alex700.lesson9;

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

/**
 * Created by Алексей on 01.12.2014.
 */
public class WeatherFetcher {

    public static final String LOG_TAG = "weather fetcher";

    private static WeatherData[] getWeatherDataFromJson(String forecastJsonStr, City city) throws JSONException {
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DAY = "day";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_SPEED = "speed";
        final String OWM_DATETIME = "dt";
        final String OWM_WEATHER_MAIN = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        WeatherData[] weatherData = new WeatherData[weatherArray.length()];
        for (int i = 0; i < weatherArray.length(); i++) {
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

            weatherData[i] = new WeatherData(min, max, day, speed, humidity, pressure,
                    dateTime * 1000L, city.getId(), weatherMain);
        }

        return weatherData;
    }

    public static String getCityNameFromJson(String cityJson) throws JSONException {
        JSONObject json = (new JSONObject(cityJson)).getJSONArray("list").getJSONObject(0);
        return json.getString("name");
    }


    public static String fetchCity(double latitude, double longitude, String apiKey) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJSONStr = null;
        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/find?";
            final String LONGITUDE_PARAM = "lon";
            final String LATITUDE_PARAM = "lat";
            final String COUNT_PARAM = "cnt";
            final String ID_PARAM = "APPID";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(LONGITUDE_PARAM, String.valueOf(longitude))
                    .appendQueryParameter(LATITUDE_PARAM, String.valueOf(latitude))
                    .appendQueryParameter(COUNT_PARAM, "1")
                    .appendQueryParameter(ID_PARAM, apiKey)
                    .build();
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
        Log.d("fetcher", forecastJSONStr);
        try {
            return getCityNameFromJson(forecastJSONStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    public static WeatherData[] fetch(City city, int numDays, String apiKey) {
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
        Log.d("fetcher", forecastJSONStr);
        try {
            return getWeatherDataFromJson(forecastJSONStr, city);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }
}

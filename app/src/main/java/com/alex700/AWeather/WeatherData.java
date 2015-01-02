package com.alex700.AWeather;

import android.content.ContentValues;

/**
 * Created by Алексей on 30.11.2014.
 */
public class WeatherData {
    private int tMin;
    private int tMax;
    private int t;
    private int windSpeed;
    private int humidity;
    private int pressure;
    private long date;
    private int cityId;
    private WeatherInfo weatherInfo;

    public WeatherData(int tMin, int tMax, int t, int windSpeed, int humidity, int pressure, long date, int cityId, String weatherMain) {
        this.tMin = tMin;
        this.tMax = tMax;
        this.t = t;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.pressure = pressure;
        this.date = date;
        this.cityId = cityId;
        this.weatherInfo = WeatherInfo.getWeatherInfo(weatherMain);
    }

    public int gettMin() {
        return tMin;
    }

    public int gettMax() {
        return tMax;
    }

    public int getT() {
        return t;
    }

    public String getTString() {
        if (t > 0) {
            return "+" + t + "°C";
        } else {
            return t + "°C";
        }
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public long getDate() {
        return date;
    }

    public int getCityId() {
        return cityId;
    }

    public WeatherInfo getWeatherInfo() {
        return weatherInfo;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WeatherDatabaseHelper.WEATHER_MAIN, weatherInfo.getMain());
        cv.put(WeatherDatabaseHelper.WEATHER_T, t);
        cv.put(WeatherDatabaseHelper.WEATHER_T_MIN, tMin);
        cv.put(WeatherDatabaseHelper.WEATHER_T_MAX, tMax);
        cv.put(WeatherDatabaseHelper.WEATHER_CITY_ID, cityId);
        cv.put(WeatherDatabaseHelper.WEATHER_DATE, date);
        cv.put(WeatherDatabaseHelper.WEATHER_HUMIDITY, humidity);
        cv.put(WeatherDatabaseHelper.WEATHER_PRESSURE, pressure);
        cv.put(WeatherDatabaseHelper.WEATHER_WIND_SPEED, windSpeed);
        return cv;
    }
}

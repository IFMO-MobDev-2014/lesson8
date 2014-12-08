package ru.ifmo.md.lesson8;

import android.content.ContentValues;

public class WeatherData {
    private int temperatureMin;
    private int temperatureMax;
    private int temperature;
    private int windSpeed;
    private int humidity;
    private int pressure;
    private long date;
    private int cityId;
    private WeatherInfo weatherInfo;

    public WeatherData(int temperatureMin, int temperatureMax, int temperature, int windSpeed, int humidity, int pressure, long date, int cityId, String weatherInfo) {
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.pressure = pressure;
        this.date = date;
        this.cityId = cityId;
        this.weatherInfo = WeatherInfo.getWeatherInfo(weatherInfo);
    }

    public int getTemperatureMin() {
        return temperatureMin;
    }

    public int getTemperatureMax() {
        return temperatureMax;
    }

    public int getTemperature() {
        return temperature;
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
        cv.put(WeatherDatabaseHelper.WEATHER_WEATHER_MAIN, weatherInfo.getMain());
        cv.put(WeatherDatabaseHelper.WEATHER_TEMPERATURE, temperature);
        cv.put(WeatherDatabaseHelper.WEATHER_TEMPERATURE_MIN, temperatureMin);
        cv.put(WeatherDatabaseHelper.WEATHER_TEMPERATURE_MAX, temperatureMax);
        cv.put(WeatherDatabaseHelper.WEATHER_CITY_ID, cityId);
        cv.put(WeatherDatabaseHelper.WEATHER_WIND_SPEED, windSpeed);
        cv.put(WeatherDatabaseHelper.WEATHER_HUMIDITY, humidity);
        cv.put(WeatherDatabaseHelper.WEATHER_PRESSURE, pressure);
        cv.put(WeatherDatabaseHelper.WEATHER_DATE, date);
        return cv;
    }
}

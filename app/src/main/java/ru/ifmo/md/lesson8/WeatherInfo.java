package ru.ifmo.md.lesson8;

import java.util.ArrayList;

public class WeatherInfo {
    private String mWeatherDescription;
    private String mIcon;
    private String mCityName;
    private String mLastUpdate;
    private int mCityId;
    private int mTemperature;
    private int mHumidity;
    private double mWindSpeed;
    private ArrayList<WeatherDay> mForecast;
    public String getCityName() {
        return mCityName;
    }
    public void setCityName(String cityName) {
        mCityName = cityName;
    }
    public int getCityId() {
        return mCityId;
    }
    public void setCityId(int cityId) {
        mCityId = cityId;
    }
    public void setTemperature(int temperature) {
        mTemperature = temperature;
    }
    public int getHumidity() {
        return mHumidity;
    }
    public void setHumidity(int humidity) {
        mHumidity = humidity;
    }
    public double getWindSpeed() {
        return mWindSpeed;
    }
    public WeatherInfo() {
        mForecast = new ArrayList<WeatherDay>();
    }
    public void setForecast(ArrayList<WeatherDay> forecast) {
        mForecast = forecast;
    }
    public ArrayList<WeatherDay> getForecast() {
        return mForecast;
    }
    public String getLastUpdate() {
        return mLastUpdate;
    }
    public void setLastUpdate(String lastUpdate) {
        mLastUpdate = lastUpdate;
    }
    public int getTemperature() {
        return mTemperature;
    }
    public void setWindSpeed(double windSpeed) {
        mWindSpeed = windSpeed;
    }
    public String getWeatherDescription() {
        return mWeatherDescription;
    }
    public void setWeatherDescription(String weatherDescription) {
        mWeatherDescription = weatherDescription;
    }
    public String getIcon() {
        return mIcon;
    }
    public void setIcon(String icon) {
        mIcon = icon;
    }
}

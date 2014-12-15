package ru.ifmo.md.weather.db.model;

/**
 * Created by Kirill on 15.12.2014.
 */
public class Settings {
    String lastUpdateWeather;
    String lastUpdateForecast;
    String updateInterval;

    public Settings(String lastUpdateWeather, String lastUpdateForecast, String updateInterval) {
        this.lastUpdateWeather = lastUpdateWeather;
        this.lastUpdateForecast = lastUpdateForecast;
        this.updateInterval = updateInterval;
    }

    public String getLastUpdateWeather() {
        return lastUpdateWeather;
    }

    public void setLastUpdateWeather(String lastUpdateWeather) {
        this.lastUpdateWeather = lastUpdateWeather;
    }

    public String getLastUpdateForecast() {
        return lastUpdateForecast;
    }

    public void setLastUpdateForecast(String lastUpdateForecast) {
        this.lastUpdateForecast = lastUpdateForecast;
    }

    public String getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(String updateInterval) {
        this.updateInterval = updateInterval;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "lastUpdateWeather='" + lastUpdateWeather + '\'' +
                ", lastUpdateForecast='" + lastUpdateForecast + '\'' +
                ", updateInterval='" + updateInterval + '\'' +
                '}';
    }
}

package com.alex700.lesson9;

/**
 * Created by Алексей on 30.11.2014.
 */
public enum WeatherInfo {
    Clear("Clear"), Snow("Snow"), Clouds("Clouds"), Thunderstorm("Thunderstorm"),
    Drizzle("Drizzle"), Rain("Rain"), Mist("Mist");

    private String main;
    private String iconName;

    WeatherInfo(String main) {
        this.main = main;
        iconName = main.toLowerCase() + ".png";
    }

    public static WeatherInfo getWeatherInfo(String s) {
        for (WeatherInfo weatherInfo : WeatherInfo.values()) {
            if (weatherInfo.getMain().equals(s)) {
                return weatherInfo;
            }
        }
        return null;
    }

    public String getIconName() {
        return iconName;
    }

    public String getMain() {
        return main;
    }
}

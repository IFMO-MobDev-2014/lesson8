package ru.ifmo.md.lesson8;

public enum WeatherInfo {
    CLEAR("Clear"), SNOW("Snow"), CLOUDS("Clouds"), RAIN("Rain"), THUNDERSTORM("Thunderstorm"), MIST("Mist");

    private String main;
    private String iconName;

    WeatherInfo(String main) {
        this.main = main;
        iconName = main.toLowerCase() + ".png";
    }

    public static WeatherInfo getWeatherInfo(String s) {
        for (WeatherInfo weatherInfo : WeatherInfo.values()) {
            if (weatherInfo.getMain().toLowerCase().equals(s.toLowerCase())) {
                return weatherInfo;
            }
        }
        return null;
    }

    public String getMain() {
        return main;
    }

    public String getIconName() {
        return iconName;
    }
}

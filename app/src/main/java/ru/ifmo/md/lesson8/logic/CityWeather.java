package ru.ifmo.md.lesson8.logic;

import java.util.ArrayList;

/**
 * Created by sergey on 30.11.14.
 */

public class CityWeather {

    public String imageUrl;

    public Condition condition = new Condition();
    public Wind wind = new Wind();
    public Atmosphere atmosphere = new Atmosphere();
    public Location location = new Location();
    public Astronomy astronomy = new Astronomy();
    public Units units = new Units();
    public ArrayList<Forecast> forecasts = new ArrayList<>();

    public String lastUpdate;

    public class Condition {
        public String description;
        public int code;
        public String date;
        public int temp;
    }

    public class Forecast {
        public String day;
        public String date;
        public String description;
        public int tempMin;
        public int tempMax;
        public int code;
    }

    public void addForecast(String day, String date, String description, int tempMin, int tempMax, int code) {
        Forecast forecast = new Forecast();
        forecast.day = day;
        forecast.date = date;
        forecast.description = description;
        forecast.tempMin = tempMin;
        forecast.tempMax = tempMax;
        forecast.code = code;
        forecasts.add(forecast);
    }

    public static class Atmosphere {
        public int humidity;
        public float visibility;
        public float pressure;
        public int rising;
    }

    public class Wind {
        public int chill;
        public int direction;
        public int speed;
    }

    public class Units {
        public String speed;
        public String distance;
        public String pressure;
        public String temperature;
    }

    public class Location {
        public String name;
        public String region;
        public String country;
    }

    public class Astronomy {
        public String sunRise;
        public String sunSet;
    }

}

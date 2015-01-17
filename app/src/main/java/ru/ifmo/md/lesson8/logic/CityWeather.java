package ru.ifmo.md.lesson8.logic;

import android.content.ContentValues;

import java.util.ArrayList;

import ru.ifmo.md.lesson8.database.WeatherTable;

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

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherTable.COLUMN_LASTUPD, lastUpdate);

        contentValues.put(WeatherTable.COLUMN_COUNTRY, location.country);
        contentValues.put(WeatherTable.COLUMN_CITY, location.name);

        contentValues.put(WeatherTable.COLUMN_CONDITION_DESCRIPTION, condition.description);
        contentValues.put(WeatherTable.COLUMN_CONDITION_TEMP, condition.temp);
        contentValues.put(WeatherTable.COLUMN_CONDITION_CODE, condition.code);
        contentValues.put(WeatherTable.COLUMN_CONDITION_DATE, condition.date);

        contentValues.put(WeatherTable.COLUMN_ATMOSPHERE_PRESSURE, atmosphere.pressure);
        contentValues.put(WeatherTable.COLUMN_ATMOSPHERE_HUMIDITY, atmosphere.humidity);

        contentValues.put(WeatherTable.COLUMN_WIND_DIRECTION, wind.direction);
        contentValues.put(WeatherTable.COLUMN_WIND_SPEED, wind.speed);

        StringBuilder builder = new StringBuilder();
        for (CityWeather.Forecast forecast : forecasts) {
            builder.append(forecast.day).append("|");
            builder.append(forecast.date).append("|");
            builder.append(forecast.description).append("|");
            builder.append(forecast.tempMin).append("|");
            builder.append(forecast.tempMax).append("|");
            builder.append(forecast.code).append("|");
        }

        contentValues.put(WeatherTable.COLUMN_FORECAST, builder.toString());
        return contentValues;
    }

}

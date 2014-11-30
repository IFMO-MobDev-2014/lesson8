package ru.ifmo.md.lesson8;

import android.provider.BaseColumns;

/**
 * Created by dimatomp on 30.11.14.
 */
public interface WeatherColumns extends BaseColumns {
    String CITIES = "Cities";
    String CITY_NAME = "CityName";

    String WEATHER_DATA = "WeatherData";
    String CITY_ID = "CityId";
    String TIME = "Time";
    String IN_BRIEF = "BriefDescription";
    String DESCRIPTION = "Description";
    String TEMP_MIN = "MinTemperature";
    String TEMP_CUR = "CurTemperature";
    String TEMP_MAX = "MaxTemperature";
    String WIND_SPEED = "WindSpeed";
    String WIND_ANGLE = "WindAngle";
    String HUMIDITY = "Humidity";
    String PRESSURE = "Pressure";
}

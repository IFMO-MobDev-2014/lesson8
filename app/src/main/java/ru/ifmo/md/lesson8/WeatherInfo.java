package ru.ifmo.md.lesson8;

import android.database.Cursor;

import ru.ifmo.md.lesson8.json.subcontainers.Main;
import ru.ifmo.md.lesson8.json.subcontainers.Weather;
import ru.ifmo.md.lesson8.json.subcontainers.Wind;

import static ru.ifmo.md.lesson8.WeatherColumns.DESCRIPTION;
import static ru.ifmo.md.lesson8.WeatherColumns.HUMIDITY;
import static ru.ifmo.md.lesson8.WeatherColumns.IN_BRIEF;
import static ru.ifmo.md.lesson8.WeatherColumns.PRESSURE;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_CUR;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_MAX;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_MIN;
import static ru.ifmo.md.lesson8.WeatherColumns.WIND_ANGLE;
import static ru.ifmo.md.lesson8.WeatherColumns.WIND_SPEED;

/**
 * Created by dimatomp on 29.11.14.
 */
public class WeatherInfo {
    public final Main mainInfo;
    public final Wind wind;
    public final Weather description;

    public WeatherInfo(Cursor cursor) {
        this.mainInfo = new Main();
        mainInfo.temp = cursor.getFloat(cursor.getColumnIndex(TEMP_CUR)) - 273.15f;
        mainInfo.tempMin = cursor.getFloat(cursor.getColumnIndex(TEMP_MIN)) - 273.15f;
        mainInfo.tempMax = cursor.getFloat(cursor.getColumnIndex(TEMP_MAX)) - 273.15f;
        mainInfo.humidity = cursor.getFloat(cursor.getColumnIndex(HUMIDITY));
        mainInfo.pressure = cursor.getFloat(cursor.getColumnIndex(PRESSURE));

        this.wind = new Wind();
        wind.speed = cursor.getFloat(cursor.getColumnIndex(WIND_SPEED));
        wind.deg = cursor.getFloat(cursor.getColumnIndex(WIND_ANGLE));

        this.description = new Weather();
        description.icon = cursor.getString(cursor.getColumnIndex(IN_BRIEF));
        description.description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
    }
}

package ru.ifmo.md.lesson8.json.subcontainers;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static ru.ifmo.md.lesson8.WeatherColumns.HUMIDITY;
import static ru.ifmo.md.lesson8.WeatherColumns.PRESSURE;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_CUR;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_MAX;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_MIN;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Main implements WeatherEntry {
    public float temp;
    public float tempMin;
    public float tempMax;
    public float pressure;
    public float humidity;

    @Override
    public void inflate(ContentValues values) {
        values.put(TEMP_MIN, tempMin);
        values.put(TEMP_CUR, temp);
        values.put(TEMP_MAX, tempMax);
        values.put(PRESSURE, pressure);
        values.put(HUMIDITY, humidity);
    }
}

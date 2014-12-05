package ru.ifmo.md.lesson8.json.subcontainers;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static ru.ifmo.md.lesson8.WeatherColumns.WIND_ANGLE;
import static ru.ifmo.md.lesson8.WeatherColumns.WIND_SPEED;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Wind implements WeatherEntry {
    public float speed;
    public float deg;

    @Override
    public void inflate(ContentValues values) {
        values.put(WIND_SPEED, speed);
        values.put(WIND_ANGLE, deg);
    }
}

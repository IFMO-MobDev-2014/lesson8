package ru.ifmo.md.lesson8.json.subcontainers;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import static ru.ifmo.md.lesson8.WeatherColumns.TIME;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ForecastEntry implements WeatherEntry {
    public int dt;
    public Main main;
    public List<Weather> weather;
    public Wind wind;

    @Override
    public void inflate(ContentValues values) {
        values.put(TIME, dt);
        main.inflate(values);
        // TODO There may be more than 1 element
        weather.get(0).inflate(values);
        wind.inflate(values);
    }
}
